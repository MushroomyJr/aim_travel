# AIM Backend - Travel Ticketing Service

A Java Spring Boot backend service for searching and booking travel tickets (flights) with support for one-way and round-trip journeys.

## Features

- **Flight Search**: Search for available flights based on origin, destination, and dates
- **Round-trip Support**: Search for both one-way and round-trip flights
- **Order Creation**: Create orders for selected tickets
- **Mock Data**: Generates realistic mock flight data for testing
- **RESTful API**: Clean REST endpoints with proper HTTP status codes
- **Validation**: Input validation for all API endpoints
- **CORS Support**: Cross-origin resource sharing enabled

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Web**
- **Spring Validation**
- **Lombok**
- **Maven**

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Build the project**

   ```bash
   mvn clean install
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Documentation

### Base URL

```
http://localhost:8080/api/v1
```

### Endpoints

#### 1. Search Tickets

```
POST /api/v1/tickets/search
```

**Request Body:**

```json
{
  "origin": "JFK",
  "destination": "LAX",
  "departureDate": "2024-01-15",
  "returnDate": "2024-01-20",
  "roundTrip": true,
  "passengers": 2
}
```

**Response:**

```json
{
  "tickets": [
    {
      "id": "TICKET-1703123456789-0",
      "origin": "JFK",
      "destination": "LAX",
      "departureTime": "2024-01-15T08:30:00",
      "arrivalTime": "2024-01-15T11:45:00",
      "airline": "Delta",
      "price": 299.0,
      "stops": 0,
      "roundTrip": true,
      "returnDepartureTime": "2024-01-20T14:15:00",
      "returnArrivalTime": "2024-01-20T17:30:00"
    }
  ]
}
```

#### 2. Create Order

```
POST /api/v1/orders
```

**Request Body:**

```json
{
  "ticketIds": ["TICKET-1703123456789-0", "TICKET-1703123456789-1"],
  "userId": "user123"
}
```

**Response:**

```json
{
  "orderId": "ORDER-1703123456789",
  "tickets": [
    {
      "id": "TICKET-1703123456789-0",
      "origin": "JFK",
      "destination": "LAX",
      "departureTime": "2024-01-15T08:30:00",
      "arrivalTime": "2024-01-15T11:45:00",
      "airline": "Delta",
      "price": 299.0,
      "stops": 0,
      "roundTrip": true,
      "returnDepartureTime": "2024-01-20T14:15:00",
      "returnArrivalTime": "2024-01-20T17:30:00"
    }
  ],
  "totalPrice": 598.0,
  "createdAt": "2024-01-15T10:30:00",
  "userId": "user123"
}
```

## Request/Response Models

### TicketSearchRequest

- `origin` (String, required): Origin airport/city code
- `destination` (String, required): Destination airport/city code
- `departureDate` (LocalDate, required): Departure date
- `returnDate` (LocalDate, optional): Return date for round-trip
- `roundTrip` (boolean, required): Whether this is a round-trip
- `passengers` (int, optional): Number of passengers (default: 1)

### FlightTicket

- `id` (String): Unique ticket identifier
- `origin` (String): Origin airport/city
- `destination` (String): Destination airport/city
- `departureTime` (LocalDateTime): Departure date and time
- `arrivalTime` (LocalDateTime): Arrival date and time
- `airline` (String): Airline name
- `price` (BigDecimal): Ticket price
- `stops` (int): Number of stops
- `roundTrip` (boolean): Whether this is a round-trip ticket
- `returnDepartureTime` (LocalDateTime, optional): Return flight departure time
- `returnArrivalTime` (LocalDateTime, optional): Return flight arrival time

### OrderRequest

- `ticketIds` (List<String>, required): List of ticket IDs to order
- `userId` (String, optional): User identifier

### OrderResponse

- `orderId` (String): Unique order identifier
- `tickets` (List<FlightTicket>): Ordered tickets
- `totalPrice` (BigDecimal): Total order price
- `createdAt` (LocalDateTime): Order creation timestamp
- `userId` (String): User identifier

## Mock Data Features

The service generates realistic mock data including:

- **Airlines**: Delta, American Airlines, United, Southwest, JetBlue
- **Flight Times**: Departures between 6 AM and 10 PM
- **Flight Duration**: 1-6 hours
- **Prices**: $100-$800 range
- **Stops**: 0 or 1 stops
- **Round-trip Support**: Return flights for round-trip bookings

## Error Handling

The API returns appropriate HTTP status codes:

- `200 OK`: Successful operation
- `201 Created`: Order created successfully
- `400 Bad Request`: Invalid input data
- `500 Internal Server Error`: Server error

## Development

### Project Structure

```
src/main/java/com/aim/
├── AimBackendApplication.java
├── controller/
│   ├── TicketController.java
│   └── OrderController.java
├── service/
│   ├── TicketService.java
│   ├── OrderService.java
│   └── impl/
│       ├── TicketServiceImpl.java
│       └── OrderServiceImpl.java
├── model/
│   ├── FlightTicket.java
│   └── Order.java
└── dto/
    ├── TicketSearchRequest.java
    ├── TicketSearchResponse.java
    ├── OrderRequest.java
    └── OrderResponse.java
```

### Building for Production

```bash
mvn clean package
java -jar target/aim-backend-1.0.0.jar
```

## Testing the API

### Search for One-way Flights

```bash
curl -X POST http://localhost:8080/api/v1/tickets/search \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "JFK",
    "destination": "LAX",
    "departureDate": "2024-01-15",
    "roundTrip": false,
    "passengers": 1
  }'
```

### Search for Round-trip Flights

```bash
curl -X POST http://localhost:8080/api/v1/tickets/search \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "JFK",
    "destination": "LAX",
    "departureDate": "2024-01-15",
    "returnDate": "2024-01-20",
    "roundTrip": true,
    "passengers": 2
  }'
```

### Create an Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "ticketIds": ["TICKET-1703123456789-0"],
    "userId": "user123"
  }'
```

## Future Enhancements

- **Real API Integration**: Connect to actual flight booking APIs
- **Database Persistence**: Store orders and user data
- **User Authentication**: Add user login and session management
- **Advanced Filters**: Price range, airline preferences, stop preferences
- **Booking Confirmation**: Email confirmations and booking management
- **Payment Integration**: Process payments for orders
- **Multi-city Support**: Support for complex itineraries

## License

This project is licensed under the MIT License.
