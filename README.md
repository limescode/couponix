# Couponix

Couponix is a REST API for managing discount coupons.

The application allows:

* creating a new coupon,
* using a coupon by a user.

---

## Features

### Create coupon

You can create a coupon with:

* unique code (case insensitive),
* max number of usages,
* country code (ISO 2-letter).

### Use coupon

A user can use a coupon if:

* the coupon exists,
* the usage limit is not reached,
* the user is from the correct country (based on IP),
* the user has not used this coupon before.

---

## Business rules

* Coupon code is **case insensitive** (`WIOSNA` = `wiosna`)
* Each coupon has a **usage limit**
* Coupon can be used only from a specific **country**
* One user can use a coupon **only once**
* When limit is reached → request fails
* When coupon does not exist → request fails
* When country is not allowed → request fails

---

## Technical details

* Java 21
* Spring Boot
* PostgreSQL
* Flyway (database migrations)
* Testcontainers (integration tests)
* Swagger (OpenAPI)

---

## API

### Create coupon

**POST** `/api/coupons`

Example request:

```json
{
  "code": "WAKACJE",
  "maxUsages": 5,
  "countryCode": "PL"
}
```

---

### Use coupon

**POST** `/api/coupons/use`

Example request:

```json
{
  "code": "WAKACJE",
  "userId": "john"
}
```

---

## Swagger

Swagger UI is available after starting the application:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON is available at:

```
http://localhost:8080/v3/api-docs
```

---

## Postman collection

A Postman collection for manual API testing is available in:

```
postman/couponix-collection.json
```

The collection contains example requests for:

* creating a coupon,
* using a coupon,
* testing validation errors,
* testing business error scenarios.

You can import this file into Postman and test the application locally.

---

## Concurrency

The system handles concurrent requests.

When many users try to use the same coupon at the same time:

* only allowed number of usages is accepted,
* other requests are rejected.

This is implemented using:

* database locking (`PESSIMISTIC_WRITE`)
* database constraints (unique indexes)

---

## Running the application

### 1. Start database

```bash
docker-compose up -d
```

### 2. Run application

```bash
./mvnw spring-boot:run
```

---

## Tests

To run tests:

```bash
./mvnw test
```

Integration tests use Testcontainers and real PostgreSQL.

---

## Notes

* Coupon code is normalized to upper case
* Database ensures uniqueness and data consistency
* External API is used to resolve country from IP
* For local testing, localhost is treated as `PL`
