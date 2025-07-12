# Appointment Booking Service

A Kotlin + Spring Boot microservice for scheduling appointments between clients and professionals. This service focuses on REST design, concurrency safety, and caching.

## Features

- **RESTful API Design**: Clean, intuitive endpoints following REST principles
- **Concurrency Safety**: Thread-safe booking operations with professional-level locking
- **Caching**: Intelligent caching for availability queries using Caffeine
- **Conflict Detection**: Prevents double-booking with overlap detection
- **Pagination**: Efficient listing with pagination support
- **Validation**: Comprehensive input validation and error handling

## Built with

- **Kotlin** - Primary language
- **Spring Boot 3.2.0** - Framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database for development
- **Caffeine Cache** - High-performance caching
- **Gradle** - Build tool

## API Endpoints

### 1. POST /bookings
Create a new booking. Fails if the time slot is already booked.

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



