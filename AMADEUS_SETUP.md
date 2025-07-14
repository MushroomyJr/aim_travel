# Amadeus API Setup Guide

This guide explains how to set up the Amadeus API integration for live flight data.

## Prerequisites

1. Create a free account at [Amadeus for Developers](https://developers.amadeus.com/)
2. Register your application to get API credentials

## Configuration

### Option 1: Environment Variables (Recommended)

Set the following environment variables:

```bash
export AMADEUS_CLIENT_ID="your_client_id_here"
export AMADEUS_CLIENT_SECRET="your_client_secret_here"
```

### Option 2: Application Properties

Add the credentials to `src/main/resources/application.yml`:

```yaml
app:
  api:
    use-mock-data: false
    amadeus-client-id: your_client_id_here
    amadeus-client-secret: your_client_secret_here
```

## Usage

### Enable Real API Data

To use real flight data instead of mock data:

1. Set `app.api.use-mock-data: false` in `application.yml`
2. Provide valid Amadeus API credentials
3. Restart the application

### API Endpoints

The application will automatically:

- Use real Amadeus API data when credentials are provided and `use-mock-data` is false
- Fall back to mock data if the API is unavailable or credentials are missing
- Validate that departure dates are not in the past
- Return proper validation error messages

### Testing

Test the API with a valid request:

```bash
curl -X POST http://localhost:8080/api/v1/tickets/search \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "NYC",
    "destination": "LAX",
    "departureDate": "2024-12-25",
    "passengers": 1,
    "roundTrip": false
  }'
```

### Validation

The API now validates:

- Origin and destination are required
- Departure date must be today or in the future
- At least 1 passenger is required
- Return date is optional for round trips

### Error Handling

Invalid requests will return a 400 Bad Request with detailed error messages:

```json
{
  "departureDate": "Departure date must be today or in the future",
  "origin": "Origin is required"
}
```

## Notes

- The Amadeus test API has rate limits
- IATA airport codes are required (e.g., "NYC", "LAX", "LHR")
- The API uses OAuth2 authentication with automatic token refresh
- Mock data is used as fallback when API is unavailable
