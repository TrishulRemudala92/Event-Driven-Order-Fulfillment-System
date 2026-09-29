# Event-Driven Order Fulfillment System

This is a personal backend project developed to practise building microservices with Spring Boot and Apache Kafka. It models a basic order fulfilment process using separate services for orders, products, payments and users.

When a new order is created, the Order Service stores the order and publishes an event to Kafka. The Payment Service consumes the event and creates the corresponding payment record. Duplicate payment processing is prevented by checking the order number before saving a new payment.

## System Architecture

![Event-Driven Order Fulfillment System](assets/Event-Driven-Order-Fulfillment-Final.png)
## Services

- **Order Service** – Creates and retrieves customer orders.
- **Product Service** – Manages product information and stock data.
- **Payment Service** – Processes order events received through Kafka and provides payment details.
- **User Service** – Manages user information.

## Technologies

- Java 17
- Spring Boot
- Spring Data JPA
- REST APIs
- Apache Kafka
- MySQL
- Maven
- JUnit 5 and Mockito
- Swagger/OpenAPI
- Postman
- GitHub Actions
- Docker

## Application Flow

1. The client sends a request to create an order.
2. The Order Service saves the order in its database.
3. An `OrderCreatedEvent` is published to Kafka.
4. The Payment Service consumes the event.
5. The Payment Service checks whether a payment already exists for the order.
6. If no payment exists, a new payment record is created.

## Service Ports

| Service | Port |
|---|---:|
| Order Service | 8081 |
| Product Service | 8082 |
| Payment Service | 8083 |
| User Service | 8084 |
| Kafka | 9092 |
| MySQL | 3307 → 3306 |


## Main API Endpoints

### Order Service

```text
POST /api/orders
GET  /api/orders
GET  /api/orders/{orderNumber}
```

### Payment Service

```text
GET /api/payments
GET /api/payments/order/{orderNumber}
```

The remaining endpoints can be viewed and tested through Swagger UI.

## Running the Project

### Requirements

Before starting the services, make sure the following are installed and running:

- Java 17
- Maven
- MySQL
- Apache Kafka

Update the database username and password in the `application.properties` file of each service.

Start every service separately:

```bash
cd order-service
mvn spring-boot:run
```

Repeat the command for `product-service`, `payment-service` and `user-service`.

## API Documentation

Swagger UI is available at:

```text
http://localhost:8081/swagger-ui/index.html
http://localhost:8082/swagger-ui/index.html
http://localhost:8083/swagger-ui/index.html
http://localhost:8084/swagger-ui/index.html
```

The APIs can also be tested using Postman.

## Testing

The services contain unit tests written with JUnit 5 and Mockito.

Run the tests inside an individual service:

```bash
mvn clean test
```

A GitHub Actions workflow is also configured to run the Maven tests automatically when code changes are pushed to the repository.

### Requirements

For running the complete application with Docker, make sure Docker Desktop is installed and running.

### Run with Docker Compose

The project includes a `compose.yaml` file for running the four microservices together with MySQL and Apache Kafka.

Build the Docker images:

```bash
docker compose build
```

Start all containers:

```bash
docker compose up -d
```

Check the running containers:

```bash
docker compose ps
```

View container logs:

```bash
docker compose logs
```

Stop the containers:

```bash
docker compose down
```

The `mysql-init.sql` script initializes the required MySQL databases when the MySQL container starts.

### Run Services Manually

The services can also be started individually without Docker.

Make sure Java 17, Maven, MySQL and Apache Kafka are installed and running.

Start a service using:

```bash
cd order-service
mvn spring-boot:run
```

Repeat the command for `product-service`, `payment-service` and `user-service`.

## Project Status

The main microservices, REST APIs, database operations, Kafka communication, service tests and Docker-based container setup are implemented.

The complete application can be started using Docker Compose with MySQL, Apache Kafka, Order Service, Product Service, Payment Service and User Service running as separate containers.

Kubernetes deployment is currently in progress. The goal is to run the microservices, MySQL and Apache Kafka together in a Kubernetes cluster using separate Kubernetes resources for each component.

Planned improvements include:

* Complete the Kubernetes deployment of the application
* Spring Security and JWT authentication
* Centralised exception handling

