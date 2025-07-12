# Appointment Booking Service

A Kotlin + Spring Boot microservice for scheduling appointments between clients and professionals. This service focuses on REST design, concurrency safety, and caching.

## Features

- **RESTful API Design**: Clean, intuitive endpoints following REST principles
- **Concurrency Safety**: Thread-safe booking operations with professional-level locking
- **Caching**: Intelligent caching for availability queries using Caffeine
- **Conflict Detection**: Prevents double-booking with overlap detection
- **Pagination**: Efficient listing with pagination support
- **Validation**: Comprehensive input validation and error handling

## Technology Stack

- **Kotlin** - Primary language
- **Spring Boot 3.2.0** - Framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database for development
- **Caffeine Cache** - High-performance caching
- **Gradle** - Build tool

## API Endpoints

### 1. POST /bookings
Create a new booking. Rejects overlapping appointments.

**Request Body:**
```json
{
  "clientId": "client1",
  "professionalId": "doc1",
  "startTime": "2025-07-15T10:00:00",
  "endTime": "2025-07-15T11:00:00",
  "serviceType": "consultation",
  "notes": "First appointment"
}
```

**Response:**
- `201 Created` - Booking created successfully
- `409 Conflict` - Time slot conflicts with existing booking

### 2. GET /bookings
List bookings with optional filtering and pagination.

**Query Parameters:**
- `clientId` (optional) - Filter by client
- `professionalId` (optional) - Filter by professional
- `date` (optional) - Filter by date (YYYY-MM-DD format)
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size

**Response:**
```json
{
  "bookings": [...],
  "totalElements": 2,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

### 3. DELETE /bookings/{id}
Delete a booking by ID.

**Response:**
- `204 No Content` - Booking deleted successfully
- `404 Not Found` - Booking not found

### 4. GET /bookings/availability
Show available time slots for a professional on a specific date.

**Query Parameters:**
- `professionalId` (required) - Professional ID
- `date` (required) - Date in YYYY-MM-DD format

**Response:**
```json
{
  "professionalId": "doc1",
  "date": "2025-07-15",
  "availableSlots": [
    {
      "startTime": "2025-07-15T09:00:00",
      "endTime": "2025-07-15T10:00:00"
    }
  ]
}
```

## Running the Application

### Prerequisites
- Java 17 or higher
- Gradle

### Build and Run
```bash
./gradlew build
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Testing the API

1. **Create a booking:**
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "client1",
    "professionalId": "doc1",
    "startTime": "2025-07-15T10:00:00",
    "endTime": "2025-07-15T11:00:00",
    "serviceType": "consultation",
    "notes": "First appointment"
  }'
```

2. **Check availability:**
```bash
curl -X GET "http://localhost:8080/bookings/availability?professionalId=doc1&date=2025-07-15"
```

3. **List bookings:**
```bash
curl -X GET "http://localhost:8080/bookings?professionalId=doc1&date=2025-07-15"
```

4. **Delete a booking:**
```bash
curl -X DELETE http://localhost:8080/bookings/1
```

## Architecture Highlights

### Concurrency Safety
- Uses `ReentrantLock` per professional to prevent race conditions
- Thread-safe booking operations ensure data consistency

### Caching Strategy
- Caffeine cache for availability queries
- Cache eviction on booking creation/deletion
- 10-minute cache expiration with 100-entry limit

### Database Design
- JPA entities with proper validation
- Optimized queries for conflict detection
- Pagination support for large datasets

### Error Handling
- Global exception handler for consistent error responses
- Proper HTTP status codes (201, 409, 404, 204)
- Validation annotations for input validation

## Project Structure

```
src/main/kotlin/com/appointmentbooking/
├── AppointmentBookingServiceApplication.kt
├── config/
│   └── CacheConfig.kt
├── controller/
│   ├── BookingController.kt
│   └── RootController.kt
├── dto/
│   └── BookingDto.kt
├── entity/
│   └── Booking.kt
├── exception/
│   ├── Exceptions.kt
│   └── GlobalExceptionHandler.kt
├── repository/
│   └── BookingRepository.kt
└── service/
    └── BookingService.kt
```

## Configuration

The application uses H2 in-memory database with the following configuration:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
      path: /h2-console
  cache:
    type: caffeine
```

