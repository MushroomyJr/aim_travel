# Sample API Calls for AIM Backend

This document contains sample API calls to populate the database with test data.

## Base URL

```
http://localhost:8080/api/v1
```

## 1. Create Users

### User 1 - John Doe

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123",
    "name": "John Doe"
  }'
```

### User 2 - Jane Smith

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@example.com",
    "password": "password456",
    "name": "Jane Smith"
  }'
```

### User 3 - Bob Wilson

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob.wilson@example.com",
    "password": "password789",
    "name": "Bob Wilson"
  }'
```

## 2. Create Flight Tickets

### Flight Ticket 1 - New York to London

```bash
curl -X POST "http://localhost:8080/api/v1/tickets?userEmail=john.doe@example.com" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "JFK",
    "destination": "LHR",
    "roundTrip": true,
    "departureTime": "2024-08-15T10:00:00",
    "arrivalTime": "2024-08-15T22:00:00",
    "returnDepartureTime": "2024-08-22T14:00:00",
    "returnArrivalTime": "2024-08-22T02:00:00",
    "airline": "British Airways",
    "cost": 1200.00,
    "stops": 0,
    "baggage": "2 checked bags",
    "travelClass": "Economy"
  }'
```

### Flight Ticket 2 - Los Angeles to Tokyo

```bash
curl -X POST "http://localhost:8080/api/v1/tickets?userEmail=jane.smith@example.com" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "LAX",
    "destination": "NRT",
    "roundTrip": false,
    "departureTime": "2024-09-10T08:30:00",
    "arrivalTime": "2024-09-11T12:30:00",
    "airline": "Japan Airlines",
    "cost": 1800.00,
    "stops": 1,
    "baggage": "1 checked bag",
    "travelClass": "Business"
  }'
```

### Flight Ticket 3 - Chicago to Paris

```bash
curl -X POST "http://localhost:8080/api/v1/tickets?userEmail=bob.wilson@example.com" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "ORD",
    "destination": "CDG",
    "roundTrip": true,
    "departureTime": "2024-10-05T16:00:00",
    "arrivalTime": "2024-10-06T08:00:00",
    "returnDepartureTime": "2024-10-12T10:00:00",
    "returnArrivalTime": "2024-10-12T02:00:00",
    "airline": "Air France",
    "cost": 950.00,
    "stops": 0,
    "baggage": "1 checked bag",
    "travelClass": "Economy"
  }'
```

### Flight Ticket 4 - Miami to Barcelona

```bash
curl -X POST "http://localhost:8080/api/v1/tickets?userEmail=john.doe@example.com" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "MIA",
    "destination": "BCN",
    "roundTrip": true,
    "departureTime": "2024-11-20T12:00:00",
    "arrivalTime": "2024-11-21T06:00:00",
    "returnDepartureTime": "2024-11-27T08:00:00",
    "returnArrivalTime": "2024-11-27T02:00:00",
    "airline": "Iberia",
    "cost": 1100.00,
    "stops": 1,
    "baggage": "2 checked bags",
    "travelClass": "Premium Economy"
  }'
```

## 3. Create Orders

### Order 1 - John's London Trip

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "john.doe@example.com",
    "flightTicketIds": [2],
    "itineraryNumber": "ITN-2024-001"
  }'
```

### Order 2 - Jane's Tokyo Trip

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "jane.smith@example.com",
    "flightTicketIds": [1],
    "itineraryNumber": "ITN-2024-002"
  }'
```

### Order 3 - Bob's Paris Trip

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "bob.wilson@example.com",
    "flightTicketIds": [3],
    "itineraryNumber": "ITN-2024-003"
  }'
```

### Order 4 - John's Barcelona Trip

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "john.doe@example.com",
    "flightTicketIds": [4],
    "itineraryNumber": "ITN-2024-004"
  }'
```

## 4. Query Data

### Get All Users

```bash
curl -X GET http://localhost:8080/api/v1/users/john.doe@example.com
```

### Get All Flight Tickets for a User

```bash
curl -X GET http://localhost:8080/api/v1/tickets/user/john.doe@example.com
```

### Get All Orders for a User

```bash
curl -X GET http://localhost:8080/api/v1/orders/user/john.doe@example.com
```

### Get Specific Order

```bash
curl -X GET http://localhost:8080/api/v1/orders/1
```

### Get Order by Itinerary Number

```bash
curl -X GET http://localhost:8080/api/v1/orders/itinerary/ITN-2024-001
```

## 5. Search Flight Tickets

### Search for flights from New York to London

```bash
curl -X POST http://localhost:8080/api/v1/tickets/search \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "JFK",
    "destination": "LHR",
    "departureDate": "2024-08-15",
    "page": 0,
    "size": 10
  }'
```

## Notes

1. **Order of Execution**: Execute the API calls in the order shown above (Users → Flight Tickets → Orders)
2. **Flight Ticket IDs**: The flight ticket IDs in the order creation calls assume the tickets are created in the order shown. You may need to adjust the IDs based on the actual IDs returned when creating the flight tickets.
3. **Error Handling**: If you get errors, check that:
   - The backend is running on port 8080
   - The database connection is working
   - The user emails match exactly
   - The flight ticket IDs are correct

## Current Status

✅ **Users**: Successfully created and working
✅ **Flight Tickets**: Successfully created and working  
❌ **Orders**: Database schema mismatch issue

The order creation is currently failing due to a database schema mismatch. The foreign key constraint is trying to reference a `users` table, but the actual table is named `user`. This is a common issue with Hibernate/JPA when the database schema doesn't match the entity definitions.

## Working API Calls

### Users (Working)

```powershell
# Create John Doe
$body = @{
    email = "john.doe@example.com"
    password = "password123"
    name = "John Doe"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/register" -Method POST -Body $body -ContentType "application/json"
```

### Flight Tickets (Working)

```powershell
# Create flight ticket for John
$body = @{
    origin = "JFK"
    destination = "LHR"
    roundTrip = $true
    departureTime = "2024-08-15T10:00:00"
    arrivalTime = "2024-08-15T22:00:00"
    returnDepartureTime = "2024-08-22T14:00:00"
    returnArrivalTime = "2024-08-22T02:00:00"
    airline = "British Airways"
    cost = 1200.00
    stops = 0
    baggage = "2 checked bags"
    travelClass = "Economy"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/tickets?userEmail=john.doe@example.com" -Method POST -Body $body -ContentType "application/json"
```

### Query Data (Working)

```powershell
# Get user
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/john.doe@example.com" -Method GET

# Get user's flight tickets
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/tickets/user/john.doe@example.com" -Method GET
```

## Testing with PowerShell

If you're using PowerShell, you can use `Invoke-RestMethod` instead of curl:

```powershell
# Example for creating a user
$body = @{
    email = "john.doe@example.com"
    password = "password123"
    name = "John Doe"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/register" -Method POST -Body $body -ContentType "application/json"
```
