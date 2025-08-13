# AIM Backend API Documentation

This document provides comprehensive information about all available API endpoints for the AIM (Airline Itinerary Management) backend system.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the system uses simple email/password authentication. In production, this should be enhanced with JWT tokens or similar secure authentication mechanisms.

---

## 1. User Management APIs

### 1.1 Register User

**POST** `/users/register`

Register a new user account.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "securepassword123",
  "name": "John Doe"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "email": "user@example.com",
  "password": "securepassword123",
  "name": "John Doe",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**

- `400 Bad Request`: Validation errors (missing fields, invalid email format)
- `500 Internal Server Error`: Server error during registration

### 1.2 Login User

**POST** `/users/login`

Authenticate a user with email and password.

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "email": "user@example.com",
  "password": "securepassword123",
  "name": "John Doe",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**

- `401 Unauthorized`: Invalid email or password
- `500 Internal Server Error`: Server error during login

### 1.3 Get User by Email

**GET** `/users/{email}`

Retrieve user information by email address.

**Response (200 OK):**

```json
{
  "id": 1,
  "email": "user@example.com",
  "password": "securepassword123",
  "name": "John Doe",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**

- `404 Not Found`: User not found

---

## 2. Flight Ticket APIs

### 2.1 Search Flight Tickets

**POST** `/tickets/search`

Search for available flight tickets with pagination support.

**Request Body:**

```json
{
  "origin": "JFK",
  "destination": "LAX",
  "departureDate": "2024-02-15",
  "returnDate": "2024-02-20",
  "roundTrip": true,
  "passengers": 2,
  "page": 0,
  "size": 10
}
```

**Response (200 OK):**

```json
{
  "data": [
    {
      "id": 1,
      "origin": "JFK",
      "destination": "LAX",
      "roundTrip": true,
      "departureTime": "2024-02-15T08:00:00",
      "arrivalTime": "2024-02-15T11:30:00",
      "returnDepartureTime": "2024-02-20T14:00:00",
      "returnArrivalTime": "2024-02-20T17:30:00",
      "airline": "American Airlines",
      "cost": 450.0,
      "stops": 0,
      "baggage": "1 checked bag",
      "travelClass": "Economy"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Request Parameters:**

- `origin` (required): Origin airport code (e.g., "JFK", "LAX")
- `destination` (required): Destination airport code
- `departureDate` (required): Departure date (YYYY-MM-DD format)
- `returnDate` (optional): Return date for round trips
- `roundTrip` (optional): Boolean indicating round trip (default: false)
- `passengers` (optional): Number of passengers (default: 1, min: 1)
- `page` (optional): Page number for pagination (default: 0)
- `size` (optional): Page size (default: 10, max: 100)

### 2.2 Create Flight Ticket

**POST** `/tickets?userEmail={email}`

Create a new flight ticket for a user.

**Request Body:**

```json
{
  "origin": "JFK",
  "destination": "LAX",
  "roundTrip": true,
  "departureTime": "2024-02-15T08:00:00",
  "arrivalTime": "2024-02-15T11:30:00",
  "returnDepartureTime": "2024-02-20T14:00:00",
  "returnArrivalTime": "2024-02-20T17:30:00",
  "airline": "American Airlines",
  "cost": 450.0,
  "stops": 0,
  "baggage": "1 checked bag",
  "travelClass": "Economy"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe"
  },
  "origin": "JFK",
  "destination": "LAX",
  "roundTrip": true,
  "departureTime": "2024-02-15T08:00:00",
  "arrivalTime": "2024-02-15T11:30:00",
  "returnDepartureTime": "2024-02-20T14:00:00",
  "returnArrivalTime": "2024-02-20T17:30:00",
  "airline": "American Airlines",
  "cost": 450.0,
  "stops": 0,
  "baggage": "1 checked bag",
  "travelClass": "Economy"
}
```

**Error Responses:**

- `404 Not Found`: User not found
- `400 Bad Request`: Validation errors

### 2.3 Get Flight Ticket by ID

**GET** `/tickets/{id}`

Retrieve a specific flight ticket by its ID.

**Response (200 OK):**

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe"
  },
  "origin": "JFK",
  "destination": "LAX",
  "roundTrip": true,
  "departureTime": "2024-02-15T08:00:00",
  "arrivalTime": "2024-02-15T11:30:00",
  "returnDepartureTime": "2024-02-20T14:00:00",
  "returnArrivalTime": "2024-02-20T17:30:00",
  "airline": "American Airlines",
  "cost": 450.0,
  "stops": 0,
  "baggage": "1 checked bag",
  "travelClass": "Economy"
}
```

**Error Responses:**

- `404 Not Found`: Ticket not found

### 2.4 Get User's Flight Tickets

**GET** `/tickets/user/{userEmail}`

Retrieve all flight tickets for a specific user.

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "origin": "JFK",
    "destination": "LAX",
    "roundTrip": true,
    "departureTime": "2024-02-15T08:00:00",
    "arrivalTime": "2024-02-15T11:30:00",
    "returnDepartureTime": "2024-02-20T14:00:00",
    "returnArrivalTime": "2024-02-20T17:30:00",
    "airline": "American Airlines",
    "cost": 450.0,
    "stops": 0,
    "baggage": "1 checked bag",
    "travelClass": "Economy"
  }
]
```

### 2.5 Update Flight Ticket

**PUT** `/tickets/{id}`

Update an existing flight ticket.

**Request Body:**

```json
{
  "origin": "JFK",
  "destination": "LAX",
  "roundTrip": true,
  "departureTime": "2024-02-15T08:00:00",
  "arrivalTime": "2024-02-15T11:30:00",
  "returnDepartureTime": "2024-02-20T14:00:00",
  "returnArrivalTime": "2024-02-20T17:30:00",
  "airline": "American Airlines",
  "cost": 450.0,
  "stops": 0,
  "baggage": "1 checked bag",
  "travelClass": "Economy"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe"
  },
  "origin": "JFK",
  "destination": "LAX",
  "roundTrip": true,
  "departureTime": "2024-02-15T08:00:00",
  "arrivalTime": "2024-02-15T11:30:00",
  "returnDepartureTime": "2024-02-20T14:00:00",
  "returnArrivalTime": "2024-02-20T17:30:00",
  "airline": "American Airlines",
  "cost": 450.0,
  "stops": 0,
  "baggage": "1 checked bag",
  "travelClass": "Economy"
}
```

**Error Responses:**

- `404 Not Found`: Ticket not found
- `400 Bad Request`: Validation errors

### 2.6 Delete Flight Ticket

**DELETE** `/tickets/{id}`

Delete a flight ticket.

**Response (204 No Content):** Empty response body

**Error Responses:**

- `404 Not Found`: Ticket not found

---

## 3. Order Management APIs

### 3.1 Create Order

**POST** `/orders`

Create a new order for selected flight tickets.

**Request Body:**

```json
{
  "userEmail": "user@example.com",
  "flightTicketIds": [1, 2, 3],
  "itineraryNumber": "ITN123456789"
}
```

**Response (201 Created):**

```json
{
  "orderId": 1,
  "userEmail": "user@example.com",
  "itineraryNumber": "ITN123456789",
  "flightTickets": [
    {
      "id": 1,
      "origin": "JFK",
      "destination": "LAX",
      "roundTrip": true,
      "departureTime": "2024-02-15T08:00:00",
      "arrivalTime": "2024-02-15T11:30:00",
      "returnDepartureTime": "2024-02-20T14:00:00",
      "returnArrivalTime": "2024-02-20T17:30:00",
      "airline": "American Airlines",
      "cost": 450.0,
      "stops": 0,
      "baggage": "1 checked bag",
      "travelClass": "Economy"
    }
  ],
  "totalCost": 1350.0,
  "createdAt": "2024-01-15T10:30:00",
  "status": "CONFIRMED"
}
```

### 3.2 Get Order by ID

**GET** `/orders/{id}`

Retrieve a specific order by its ID.

**Response (200 OK):**

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe"
  },
  "flightTicket": {
    "id": 1,
    "origin": "JFK",
    "destination": "LAX",
    "roundTrip": true,
    "departureTime": "2024-02-15T08:00:00",
    "arrivalTime": "2024-02-15T11:30:00",
    "returnDepartureTime": "2024-02-20T14:00:00",
    "returnArrivalTime": "2024-02-20T17:30:00",
    "airline": "American Airlines",
    "cost": 450.0,
    "stops": 0,
    "baggage": "1 checked bag",
    "travelClass": "Economy"
  },
  "email": "user@example.com",
  "ticketInfo": "Flight from JFK to LAX",
  "orderNumber": "ORD123456789",
  "itineraryNumber": "ITN123456789",
  "cost": 450.0,
  "hotelStayOrder": null,
  "rentalOrder": null,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**

- `404 Not Found`: Order not found

### 3.3 Get User's Orders

**GET** `/orders/user/{userEmail}`

Retrieve all orders for a specific user.

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "John Doe"
    },
    "flightTicket": {
      "id": 1,
      "origin": "JFK",
      "destination": "LAX",
      "roundTrip": true,
      "departureTime": "2024-02-15T08:00:00",
      "arrivalTime": "2024-02-15T11:30:00",
      "returnDepartureTime": "2024-02-20T14:00:00",
      "returnArrivalTime": "2024-02-20T17:30:00",
      "airline": "American Airlines",
      "cost": 450.0,
      "stops": 0,
      "baggage": "1 checked bag",
      "travelClass": "Economy"
    },
    "email": "user@example.com",
    "ticketInfo": "Flight from JFK to LAX",
    "orderNumber": "ORD123456789",
    "itineraryNumber": "ITN123456789",
    "cost": 450.0,
    "hotelStayOrder": null,
    "rentalOrder": null,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

### 3.4 Get Order by Itinerary Number

**GET** `/orders/itinerary/{itineraryNumber}`

Retrieve an order by its itinerary number.

**Response (200 OK):**

```json
{
  "id": 1,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe"
  },
  "flightTicket": {
    "id": 1,
    "origin": "JFK",
    "destination": "LAX",
    "roundTrip": true,
    "departureTime": "2024-02-15T08:00:00",
    "arrivalTime": "2024-02-15T11:30:00",
    "returnDepartureTime": "2024-02-20T14:00:00",
    "returnArrivalTime": "2024-02-20T17:30:00",
    "airline": "American Airlines",
    "cost": 450.0,
    "stops": 0,
    "baggage": "1 checked bag",
    "travelClass": "Economy"
  },
  "email": "user@example.com",
  "ticketInfo": "Flight from JFK to LAX",
  "orderNumber": "ORD123456789",
  "itineraryNumber": "ITN123456789",
  "cost": 450.0,
  "hotelStayOrder": null,
  "rentalOrder": null,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**

- `404 Not Found`: Order not found

---

## Error Handling

### Common HTTP Status Codes

- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **204 No Content**: Request successful, no content to return
- **400 Bad Request**: Invalid request data or validation errors
- **401 Unauthorized**: Authentication required or failed
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

### Error Response Format

For validation errors (400 Bad Request):

```json
{
  "fieldName": "Error message for this field",
  "anotherField": "Another error message"
}
```

For general errors:

```json
"Error message string"
```

---

## Data Types

### Date/Time Format

All dates and times are in ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`

### Currency

All monetary values are represented as decimal numbers (BigDecimal) with 2 decimal places.

### Airport Codes

Use standard 3-letter IATA airport codes (e.g., "JFK", "LAX", "ORD").

---

## CORS Configuration

The API supports CORS with the following configuration:

- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: All headers

---

## Development Notes

### Environment

- **Server Port**: 8080
- **Database**: MySQL (localhost:3306/aimdb)
- **API Version**: v1

### Testing

The system includes test Amadeus API credentials for development. In production, real credentials should be used.

### Pagination

Most list endpoints support pagination with the following parameters:

- `page`: Page number (0-based)
- `size`: Number of items per page (max 100)

---

## Frontend Integration Examples

### JavaScript/TypeScript Examples

#### Search for Flights

```javascript
const searchFlights = async (searchRequest) => {
  const response = await fetch("http://localhost:8080/api/v1/tickets/search", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(searchRequest),
  });

  if (response.ok) {
    const data = await response.json();
    return data;
  } else {
    throw new Error("Failed to search flights");
  }
};

// Usage
const searchRequest = {
  origin: "JFK",
  destination: "LAX",
  departureDate: "2024-02-15",
  returnDate: "2024-02-20",
  roundTrip: true,
  passengers: 2,
  page: 0,
  size: 10,
};

const results = await searchFlights(searchRequest);
```

#### Create Order

```javascript
const createOrder = async (orderRequest) => {
  const response = await fetch("http://localhost:8080/api/v1/orders", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(orderRequest),
  });

  if (response.ok) {
    const data = await response.json();
    return data;
  } else {
    throw new Error("Failed to create order");
  }
};

// Usage
const orderRequest = {
  userEmail: "user@example.com",
  flightTicketIds: [1, 2, 3],
  itineraryNumber: "ITN123456789",
};

const order = await createOrder(orderRequest);
```

#### User Authentication

```javascript
const loginUser = async (email, password) => {
  const response = await fetch("http://localhost:8080/api/v1/users/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  if (response.ok) {
    const user = await response.json();
    // Store user session/token
    return user;
  } else {
    throw new Error("Login failed");
  }
};

// Usage
const user = await loginUser("user@example.com", "password123");
```

---

This documentation provides all the necessary information for frontend developers to integrate with the AIM backend API. The API follows RESTful conventions and includes comprehensive error handling and pagination support.
