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
            log.info("Searching for flights from {} to {} on {}", 
                    searchRequest.getOrigin(), searchRequest.getDestination(), searchRequest.getDepartureDate());
            
            // Get access token if needed
            String token = getAccessToken();
            if (token == null) {
                log.error("Failed to obtain access token");
                return new ArrayList<>();
            }

            // Build the search URL
            String searchUrl = buildSearchUrl(searchRequest);
            
            log.info("Making Amadeus API call to: {}", searchUrl);
            
            // Make the API call
            String response = webClient.get()
                    .uri(searchUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Amadeus API response: {}", response);
            
            return parseFlightResponse(response, searchRequest);
            
        } catch (Exception e) {
            log.error("Error calling Amadeus API", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isApiAvailable() {
        boolean available = apiConfig.getAmadeusClientId() != null && 
               apiConfig.getAmadeusClientSecret() != null &&
               !apiConfig.getAmadeusClientId().isEmpty() &&
               !apiConfig.getAmadeusClientSecret().isEmpty();
        
        if (available) {
            log.info("Amadeus API credentials are configured");
            log.debug("Client ID: {}", apiConfig.getAmadeusClientId());
        } else {
            log.warn("Amadeus API credentials are not properly configured");
        }
        
        return available;
    }

    private String getAccessToken() {
        // Check if we have a valid token
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }

        // Try both test and production endpoints
        String[] tokenUrls = {
            "https://test.api.amadeus.com/v1/security/oauth2/token",
            "https://api.amadeus.com/v1/security/oauth2/token"
        };

        for (String tokenUrl : tokenUrls) {
            try {
                log.info("Requesting Amadeus access token from: {}", tokenUrl);
                
                // Build the request body properly
                String requestBody = String.format(
                    "grant_type=client_credentials&client_id=%s&client_secret=%s",
                    apiConfig.getAmadeusClientId(),
                    apiConfig.getAmadeusClientSecret()
                );

                log.debug("Request body: {}", requestBody);

                String response = webClient.post()
                        .uri(tokenUrl)
                        .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .header(HttpHeaders.ACCEPT, "application/json")
                        .bodyValue(requestBody)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                clientResponse -> {
                                    log.error("Amadeus API error from {}: {} - {}", 
                                             tokenUrl, clientResponse.statusCode(), 
                                             clientResponse.statusCode().value());
                                    return clientResponse.bodyToMono(String.class)
                                            .flatMap(errorBody -> {
                                                log.error("Error response body: {}", errorBody);
                                                return Mono.error(new RuntimeException("Amadeus API error: " + errorBody));
                                            });
                                })
                        .bodyToMono(String.class)
                        .block();

                log.debug("Token response: {}", response);

                JsonNode jsonResponse = objectMapper.readTree(response);
                accessToken = jsonResponse.get("access_token").asText();
                int expiresIn = jsonResponse.get("expires_in").asInt();
                tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // Expire 1 minute early
                
                log.info("Successfully obtained Amadeus access token from: {}", tokenUrl);
                return accessToken;
                
            } catch (Exception e) {
                log.error("Failed to obtain Amadeus access token from: {}", tokenUrl, e);
                // Continue to next URL if this one fails
            }
        }
        
        log.error("Failed to obtain Amadeus access token from all endpoints");
        return null;
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

        log.info("Built Amadeus search URL: {}", url.toString());
        return url.toString();
    }

    private List<FlightTicket> parseFlightResponse(String response, TicketSearchRequest request) {
        List<FlightTicket> tickets = new ArrayList<>();
        
        try {
            log.debug("Parsing Amadeus response: {}", response);
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode data = jsonResponse.get("data");
            
            if (data != null && data.isArray()) {
                log.info("Found {} flight offers from Amadeus API", data.size());
                
                for (int i = 0; i < data.size(); i++) {
                    JsonNode offer = data.get(i);
                    
                    // Log the structure of the first few offers for debugging
                    if (i < 3) {
                        log.info("=== Offer {} ===", i);
                        log.info("Offer keys: {}", offer.fieldNames());
                        
                        if (offer.has("itineraries")) {
                            JsonNode itineraries = offer.get("itineraries");
                            log.info("Itineraries size: {}", itineraries.size());
                            
                            for (int j = 0; j < itineraries.size(); j++) {
                                JsonNode itinerary = itineraries.get(j);
                                log.info("Itinerary {} keys: {}", j, itinerary.fieldNames());
                                
                                if (itinerary.has("segments")) {
                                    JsonNode segments = itinerary.get("segments");
                                    log.info("Itinerary {} has {} segments", j, segments.size());
                                    
                                    for (int k = 0; k < segments.size(); k++) {
                                        JsonNode segment = segments.get(k);
                                        log.info("Segment {}: departure={}, arrival={}", 
                                                k, 
                                                segment.get("departure").get("iataCode").asText(),
                                                segment.get("arrival").get("iataCode").asText());
                                    }
                                }
                            }
                        }
                        
                        if (offer.has("price")) {
                            log.info("Price: {}", offer.get("price"));
                        }
                        
                        log.info("=== End Offer {} ===", i);
                    }
                    
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
            
            // Parse pricing - try direct price field first, then pricingOptions
            JsonNode priceNode = offer.get("price");
            if (priceNode != null && priceNode.has("total")) {
                String totalPrice = priceNode.get("total").asText();
                ticket.setCost(new BigDecimal(totalPrice));
                log.debug("Parsed price from direct price field: {}", totalPrice);
            } else {
                // Fallback to pricingOptions if direct price is not available
                JsonNode pricingOptions = offer.get("pricingOptions");
                if (pricingOptions != null && pricingOptions.isArray() && pricingOptions.size() > 0) {
                    JsonNode firstOption = pricingOptions.get(0);
                    if (firstOption.has("price") && firstOption.get("price").has("total")) {
                        String totalPrice = firstOption.get("price").get("total").asText();
                        ticket.setCost(new BigDecimal(totalPrice));
                        log.debug("Parsed price from pricingOptions: {}", totalPrice);
                    }
                }
            }
            
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
                    
                    // Handle timezone format (remove Z and parse)
                    departureTimeStr = departureTimeStr.replace("Z", "");
                    arrivalTimeStr = arrivalTimeStr.replace("Z", "");
                    
                    ticket.setDepartureTime(LocalDateTime.parse(departureTimeStr));
                    ticket.setArrivalTime(LocalDateTime.parse(arrivalTimeStr));
                    
                    // Parse origin and destination
                    ticket.setOrigin(firstSegment.get("departure").get("iataCode").asText());
                    ticket.setDestination(lastSegment.get("arrival").get("iataCode").asText());
                    
                    // Parse airline
                    ticket.setAirline(firstSegment.get("carrierCode").asText());
                    
                    // Parse stops
                    ticket.setStops(segments.size() - 1);
                    
                    // Parse duration from the segment
                    if (firstSegment.has("duration")) {
                        String isoDuration = firstSegment.get("duration").asText();
                        String formattedDuration = formatDuration(isoDuration);
                        ticket.setDuration(formattedDuration);
                        log.debug("Parsed duration: {} -> {}", isoDuration, formattedDuration);
                    }
                    
                    // Parse outbound segments
                    List<FlightTicket.FlightSegment> outboundSegments = parseSegments(segments);
                    ticket.setOutboundSegments(outboundSegments);
                }
            }
            
            ticket.setRoundTrip(request.isRoundTrip());
            
            // Parse return flight if round trip
            if (request.isRoundTrip() && itineraries != null && itineraries.size() > 1) {
                log.debug("Found {} itineraries, parsing return flight", itineraries.size());
                JsonNode inbound = itineraries.get(1);
                JsonNode segments = inbound.get("segments");
                
                if (segments != null && segments.isArray() && segments.size() > 0) {
                    log.debug("Found {} segments in return flight", segments.size());
                    JsonNode firstSegment = segments.get(0);
                    JsonNode lastSegment = segments.get(segments.size() - 1);
                    
                    String returnDepartureTimeStr = firstSegment.get("departure").get("at").asText();
                    String returnArrivalTimeStr = lastSegment.get("arrival").get("at").asText();
                    
                    log.debug("Return flight times - departure: {}, arrival: {}", returnDepartureTimeStr, returnArrivalTimeStr);
                    
                    returnDepartureTimeStr = returnDepartureTimeStr.replace("Z", "");
                    returnArrivalTimeStr = returnArrivalTimeStr.replace("Z", "");
                    
                    ticket.setReturnDepartureTime(LocalDateTime.parse(returnDepartureTimeStr));
                    ticket.setReturnArrivalTime(LocalDateTime.parse(returnArrivalTimeStr));
                    
                    log.debug("Set return flight times - departure: {}, arrival: {}", 
                             ticket.getReturnDepartureTime(), ticket.getReturnArrivalTime());
                    
                    // Parse return segments
                    List<FlightTicket.FlightSegment> returnSegments = parseSegments(segments);
                    ticket.setReturnSegments(returnSegments);
                    
                    // Parse return flight duration
                    if (firstSegment.has("duration")) {
                        String returnDuration = firstSegment.get("duration").asText();
                        // For round trips, we could store return duration separately or combine with outbound
                        // For now, we'll just log it for debugging
                        log.debug("Return flight duration: {}", returnDuration);
                    }
                } else {
                    log.warn("No segments found in return flight itinerary");
                }
            } else {
                log.debug("Not parsing return flight - roundTrip: {}, itineraries size: {}", 
                         request.isRoundTrip(), itineraries != null ? itineraries.size() : "null");
                
                // For round trips without return flight data, we might need to generate mock return flights
                // or the Amadeus API might not support round-trip packages for this route
                if (request.isRoundTrip() && request.getReturnDate() != null) {
                    log.info("Round trip requested but no return flight data found in Amadeus response. " +
                            "This might be because the API returns individual flight offers rather than complete round-trip packages.");
                    
                    // Optionally, we could generate mock return flight data here
                    // For now, we'll leave the return times as null
                }
            }
            
            // Set default values for missing fields
            ticket.setBaggage("1 checked bag");
            ticket.setTravelClass("Economy");
            
            log.debug("Parsed flight ticket: {} to {} for ${} (duration: {})", 
                     ticket.getOrigin(), ticket.getDestination(), ticket.getCost(), ticket.getDuration());
            
            // Log if cost is null for debugging
            if (ticket.getCost() == null) {
                log.warn("Cost is null for ticket from {} to {}. Available fields in offer: {}", 
                        ticket.getOrigin(), ticket.getDestination(), offer.fieldNames());
            }
            
            return ticket;
            
        } catch (Exception e) {
            log.error("Error parsing flight offer", e);
            return null;
        }
    }
    
    /**
     * Converts ISO 8601 duration to human-readable format
     * Example: "PT5H7M" -> "5h 7m"
     */
    private String formatDuration(String isoDuration) {
        if (isoDuration == null || isoDuration.isEmpty()) {
            return null;
        }
        
        try {
            // Remove "PT" prefix and parse hours and minutes
            String duration = isoDuration.replace("PT", "");
            StringBuilder result = new StringBuilder();
            
            // Extract hours
            int hourIndex = duration.indexOf('H');
            if (hourIndex != -1) {
                String hours = duration.substring(0, hourIndex);
                result.append(hours).append("h ");
                duration = duration.substring(hourIndex + 1);
            }
            
            // Extract minutes
            int minuteIndex = duration.indexOf('M');
            if (minuteIndex != -1) {
                String minutes = duration.substring(0, minuteIndex);
                result.append(minutes).append("m");
            }
            
            return result.toString().trim();
        } catch (Exception e) {
            log.warn("Failed to parse duration: {}", isoDuration, e);
            return isoDuration; // Return original if parsing fails
        }
    }
    
    /**
     * Parse segments from Amadeus API response
     */
    private List<FlightTicket.FlightSegment> parseSegments(JsonNode segments) {
        List<FlightTicket.FlightSegment> flightSegments = new ArrayList<>();
        
        for (JsonNode segment : segments) {
            try {
                String departureTimeStr = segment.get("departure").get("at").asText().replace("Z", "");
                String arrivalTimeStr = segment.get("arrival").get("at").asText().replace("Z", "");
                
                FlightTicket.FlightSegment flightSegment = FlightTicket.FlightSegment.builder()
                        .departureAirport(segment.get("departure").get("iataCode").asText())
                        .arrivalAirport(segment.get("arrival").get("iataCode").asText())
                        .departureTime(LocalDateTime.parse(departureTimeStr))
                        .arrivalTime(LocalDateTime.parse(arrivalTimeStr))
                        .airline(segment.get("carrierCode").asText())
                        .flightNumber(segment.has("number") ? segment.get("number").asText() : null)
                        .duration(segment.has("duration") ? formatDuration(segment.get("duration").asText()) : null)
                        .aircraft(segment.has("aircraft") ? segment.get("aircraft").get("code").asText() : null)
                        .terminal(segment.get("departure").has("terminal") ? segment.get("departure").get("terminal").asText() : null)
                        .gate(segment.get("departure").has("gate") ? segment.get("departure").get("gate").asText() : null)
                        .build();
                
                flightSegments.add(flightSegment);
                
            } catch (Exception e) {
                log.warn("Error parsing segment: {}", e.getMessage());
            }
        }
        
        return flightSegments;
    }
} 