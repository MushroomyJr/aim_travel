package com.aim.service.impl;

import com.aim.config.ApiConfig;
import com.aim.dto.TicketSearchRequest;
import com.aim.model.FlightTicket;
import com.aim.service.AmadeusApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmadeusApiServiceImpl implements AmadeusApiService {

    private final ApiConfig apiConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    private String accessToken;
    private long tokenExpiryTime;

    @Override
    public List<FlightTicket> searchRealTickets(TicketSearchRequest searchRequest) {
        if (!isApiAvailable()) {
            log.warn("Amadeus API is not available, returning empty list");
            return new ArrayList<>();
        }

        try {
            // Get access token if needed
            String token = getAccessToken();
            if (token == null) {
                log.error("Failed to obtain access token");
                return new ArrayList<>();
            }

            // Build the search URL
            String searchUrl = buildSearchUrl(searchRequest);
            
            // Make the API call
            String response = webClient.get()
                    .uri(searchUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseFlightResponse(response, searchRequest);
            
        } catch (Exception e) {
            log.error("Error calling Amadeus API", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isApiAvailable() {
        return apiConfig.getAmadeusClientId() != null && 
               apiConfig.getAmadeusClientSecret() != null &&
               !apiConfig.getAmadeusClientId().isEmpty() &&
               !apiConfig.getAmadeusClientSecret().isEmpty();
    }

    private String getAccessToken() {
        // Check if we have a valid token
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }

        try {
            String tokenUrl = apiConfig.getAmadeusBaseUrl().replace("/v2", "/v1") + "/security/oauth2/token";
            
            String requestBody = String.format(
                "grant_type=client_credentials&client_id=%s&client_secret=%s",
                apiConfig.getAmadeusClientId(),
                apiConfig.getAmadeusClientSecret()
            );

            String response = webClient.post()
                    .uri(tokenUrl)
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            accessToken = jsonResponse.get("access_token").asText();
            int expiresIn = jsonResponse.get("expires_in").asInt();
            tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // Expire 1 minute early
            
            log.info("Successfully obtained Amadeus access token");
            return accessToken;
            
        } catch (Exception e) {
            log.error("Failed to obtain Amadeus access token", e);
            return null;
        }
    }

    private String buildSearchUrl(TicketSearchRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String departureDate = request.getDepartureDate().format(formatter);
        
        StringBuilder url = new StringBuilder(apiConfig.getAmadeusBaseUrl())
                .append("/shopping/flight-offers")
                .append("?originLocationCode=").append(request.getOrigin())
                .append("&destinationLocationCode=").append(request.getDestination())
                .append("&departureDate=").append(departureDate)
                .append("&adults=").append(request.getPassengers())
                .append("&max=50"); // Limit results

        if (request.isRoundTrip() && request.getReturnDate() != null) {
            String returnDate = request.getReturnDate().format(formatter);
            url.append("&returnDate=").append(returnDate);
        }

        return url.toString();
    }

    private List<FlightTicket> parseFlightResponse(String response, TicketSearchRequest request) {
        List<FlightTicket> tickets = new ArrayList<>();
        
        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode data = jsonResponse.get("data");
            
            if (data != null && data.isArray()) {
                for (JsonNode offer : data) {
                    FlightTicket ticket = parseFlightOffer(offer, request);
                    if (ticket != null) {
                        tickets.add(ticket);
                    }
                }
            }
            
            log.info("Parsed {} real flight tickets from Amadeus API", tickets.size());
            
        } catch (Exception e) {
            log.error("Error parsing Amadeus API response", e);
        }
        
        return tickets;
    }

    private FlightTicket parseFlightOffer(JsonNode offer, TicketSearchRequest request) {
        try {
            FlightTicket ticket = new FlightTicket();
            
            // Parse basic info
            ticket.setId(offer.get("id").asText());
            ticket.setPrice(new BigDecimal(offer.get("price").get("total").asText()));
            
            // Parse itinerary
            JsonNode itineraries = offer.get("itineraries");
            if (itineraries != null && itineraries.isArray() && itineraries.size() > 0) {
                JsonNode outbound = itineraries.get(0);
                JsonNode segments = outbound.get("segments");
                
                if (segments != null && segments.isArray() && segments.size() > 0) {
                    JsonNode firstSegment = segments.get(0);
                    JsonNode lastSegment = segments.get(segments.size() - 1);
                    
                    // Parse departure and arrival times
                    String departureTimeStr = firstSegment.get("departure").get("at").asText();
                    String arrivalTimeStr = lastSegment.get("arrival").get("at").asText();
                    
                    ticket.setDepartureTime(LocalDateTime.parse(departureTimeStr.replace("Z", "")));
                    ticket.setArrivalTime(LocalDateTime.parse(arrivalTimeStr.replace("Z", "")));
                    
                    // Parse origin and destination
                    ticket.setOrigin(firstSegment.get("departure").get("iataCode").asText());
                    ticket.setDestination(lastSegment.get("arrival").get("iataCode").asText());
                    
                    // Parse airline
                    ticket.setAirline(firstSegment.get("carrierCode").asText());
                    
                    // Parse stops
                    ticket.setStops(segments.size() - 1);
                }
            }
            
            ticket.setRoundTrip(request.isRoundTrip());
            
            // Parse return flight if round trip
            if (request.isRoundTrip() && itineraries != null && itineraries.size() > 1) {
                JsonNode inbound = itineraries.get(1);
                JsonNode segments = inbound.get("segments");
                
                if (segments != null && segments.isArray() && segments.size() > 0) {
                    JsonNode firstSegment = segments.get(0);
                    JsonNode lastSegment = segments.get(segments.size() - 1);
                    
                    String returnDepartureTimeStr = firstSegment.get("departure").get("at").asText();
                    String returnArrivalTimeStr = lastSegment.get("arrival").get("at").asText();
                    
                    ticket.setReturnDepartureTime(LocalDateTime.parse(returnDepartureTimeStr.replace("Z", "")));
                    ticket.setReturnArrivalTime(LocalDateTime.parse(returnArrivalTimeStr.replace("Z", "")));
                }
            }
            
            return ticket;
            
        } catch (Exception e) {
            log.error("Error parsing flight offer", e);
            return null;
        }
    }
} 