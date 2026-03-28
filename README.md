# Online Marketplace API

## Overview
This is a Spring Boot-based REST API for an online marketplace that allows users to browse products, place orders, and manage inventory. The application implements role-based security, external payment gateway integration, and comprehensive error handling.

## Architecture

### Layered Architecture
The application follows a clean-layered architecture:

         +---------------------------------------------------+  
         |               Presentation Layer                  |  
         |  +---------------------------------------------+  |  
         |  |         Controllers (REST Endpoints)        |  |  
         |  +---------------------------------------------+  | 
         +---------------------------------------------------+  
                                 |                           
                                 v
         +---------------------------------------------------+  
         |                  Application Layer                | 
         |  +---------------------------------------------+  |  
         |  |            Services (Business Logic)        |  |  
         |  +---------------------------------------------+  |  
         +---------------------------------------------------+  
                                  |
                                  v
         +---------------------------------------------------+
         |                  Domain Layer                     |
         |   +-------------------+ +----------------------+  |
         |   |     Entities      | |  Repositories (JPA)  |  |
         |   +-------------------+ +----------------------+  |
         +---------------------------------------------------+
                                  |
                                  v
         +---------------------------------------------------+
         |                Infrastructure Layer               |
         |   +-------------------+ +----------------------+  |
         |   |    External API   | |   Security Config    |  |
         |   | (Payment Gateway) | |                      |  |
         |   +-------------------+ +----------------------+  |
         +---------------------------------------------------+

### Data Flow
1. HTTP Request → Controller → Service → Repository → Database
2. Service → External API (Payment Gateway)
3. Response flows back through the same layers

## Technologies Used

- **Spring Boot 4.0.0** Core framework
- **Spring Data JPA** - Database operations
- **Spring Security** -Authentication & Authorization
- **PostgreSQL** - Production database (running in Docker)
- **Maven** -Build tool
- **Lombok** - Reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation
- **RestTemplate** - External API calls

## Database Schema

### Entities and Relationships
   
      ┌──────────────────────────────────────────────┐
      │           ENTITY RELATIONSHIPS               │           
      └──────────────────────────────────────────────┘

    ┌─────────────┐                      ┌─────────────┐
    │    USER     │                      │  PRODUCT    │
    │─────────────│                      │─────────────│
    │ • id (PK)   │                      │ • id (PK)   │
    │ • username  │                      │ • name      │
    │ • email     │                      │ • price     │
    │ • password  │                      │ • stock     │
    └──────┬──────┘                      └──────┬──────┘
           │                                    │
           │ One-to-One                         │ Many-to-Many
           │                                    │
           ▼                                    ▼
    ┌─────────────┐                      ┌─────────────┐
    │   PROFILE   │                      │  CATEGORY   │
    │─────────────│                      │─────────────│
    │ • id (PK)   │                      │ • id (PK)   │
    │ • firstName │                      │ • name      │
    │ • lastName  │                      │ • desc      │
    │ • address   │                      └─────────────┘
    │ • user_id(FK)│
    └─────────────┘

           │
           │ One-to-Many
           ▼
    ┌─────────────┐
    │    ORDER    │
    │─────────────│
    │ • id (PK)   │
    │ • total     │           ┌─────────────────────────────────────────────────────────┐
    │ • status    │           │                  RELATIONSHIP TYPES                     │
    │ • user_id(FK)│          ├─────────────────────────────────────────────────────────┤
    └──────┬──────┘           │     User ──────────► Profile     : ONE-TO-ONE (1:1)     │                                               │
           │                  │     User ──────────► Order       : ONE-TO-MANY (1:M)    │
           │ One-to-Many      │     Order ─────────► OrderItem   : ONE-TO-MANY (1:M)    │                                               
           ▼                  │     Product ◄─────► Category     : MANY-TO-MANY (M:M)   │
    ┌─────────────┐           │                                                         │
    │ ORDER_ITEM  │           └─────────────────────────────────────────────────────────┘
    │─────────────│
    │ • productId │
    │ • quantity  │
    │ • price     │
    │ • order_id(FK)│
    └─────────────┘                                  

## API Endpoints

### User Management
| Method | Endpoint          | Description     | Authentication |
|--------|-------------------|-----------------|----------------|
| POST   | `/api/users`      | Create new user | Public         |
| GET    | `/api/users/{id}` | Get user by ID  | User/Admin     |
| GET    | `/api/users`      | Get all users   | Admin only     |
| PUT    | `/api/users/{id}` | Update user     | User/Admin     |
| DELETE | `/api/users/{id}` | Delete user     | Admin only     |

### Product Management
| Method | Endpoint                     | Description       | Authentication |
|--------|------------------------------|-------------------|----------------|
| POST   | `/api/products`              | Create product    | Admin only     |
| GET    | `/api/products/{id}`         | Get product by ID | Public         |
| GET    | `/api/products`              | Get all products  | Public         |
| GET    | `/api/products/search?name=` | Search products   | Public         |
| PUT    | `/api/products/{id}`         | Update product    | Admin only     |
| DELETE | `/api/products/{id}`         | Delete product    | Admin only     |

### Category Management
| Method | Endpoint               | Description        | Authentication |
|--------|------------------------|--------------------|----------------|
| POST   | `/api/categories`      | Create category    | Admin only     |
| GET    | `/api/categories/{id}` | Get category       | Public         |
| GET    | `/api/categories`      | Get all categories | Public         |
| PUT    | `/api/categories/{id}` | Update category    | Admin only     |
| DELETE | `/api/categories/{id}` | Delete category    | Admin only     |

### Order Management
| Method | Endpoint                               | Description         | Authentication |
|--------|----------------------------------------|---------------------|----------------|
| POST   | `/api/orders`                          | Place new order     | User only      |
| GET    | `/api/orders/{orderId}`                | Get order details   | User/Admin     |
| GET    | `/api/orders/user/{userId}`            | Get user orders     | User/Admin     |
| PATCH  | `/api/orders/{orderId}/status?status=` | Update order status | Admin only     |

## Security Implementation

- **Authentication**: Basic Authentication
- **Authorization**: Role-based access control
    - `ROLE_USER`: Can place orders, view own orders, update profile
    - `ROLE_ADMIN`: Full access to all endpoints
- **Password Encoding**: BCrypt
- **Session Management**: Stateless (no sessions)

## Default Users

| Username     | Password    | Role  |
|--------------|-------------|-------|
| Ebise_Gutema | password123 | ADMIN |
| admin        | admin123    | USER  |

## Validation Rules

### User Validation
- Username: 3-50 characters, unique
- Email: Valid email format, unique
- Password: Minimum 6 characters

### Product Validation
- Name: 3–100 characters
- Price: > 0
- Stock Quantity: >= 0

### Order Validation
- At least one item per order
- Quantity >= 1 per item
- Stock availability check before order

## External API Integration

### Payment Gateway Simulation
The application integrates with an external payment API:
- **URL**: Configurable in application.properties
- **Method**: POST
- **Request**: Amount, currency, payment method, transaction ID
- **Response**: Transaction ID and status

If the external API is unavailable, the application falls back to a mock implementation.

## Error Handling

Global exception handling with `@ControllerAdvice`:

| Exception                       | HTTP Status | Description           |
|---------------------------------|-------------|-----------------------|
| ResourceNotFoundException       | 404         | Resource not found    |
| InsufficientStockException      | 400         | Stock not available   |
| MethodArgumentNotValidException | 400         | Validation failed     |
| Exception                       | 500         | Internal server error |


## Project Structure
      src/main/java/et/edu/aau/onlinemarketplace/
      ├── OnlineMarketplaceApplication.java
      ├── Config/ 
      │ ├── AppConfig.java
      │ ├── SecurityConfig.java
      │ └── SwaggerConfig.java
      ├── Controller/
      │ ├── UserController.java
      │ ├── ProductController.java
      │ ├── OrderController.java
      │ └── CategoryController.java
      ├── Dtos/
      │ ├── request/
      │ │ ├── UserRequestDTO.java
      │ │ ├── ProductRequestDTO.java
      │ │ └── OrderRequestDTO.java
      │ └── response/
      │ ├── UserResponseDTO.java
      │ ├── ProductResponseDTO.java
      │ └── OrderResponseDTO.java
      ├── entity/
      │ ├── User.java
      │ ├── Profile.java
      │ ├── Product.java
      │ ├── Category.java
      │ └── Order.java
      ├── repository/
      │ ├── UserRepository.java
      │ ├── ProductRepository.java
      │ ├── CategoryRepository.java
      │ └── OrderRepository.java
      ├── service/
      │ ├── UserService.java
      │ ├── ProductService.java
      │ ├── OrderService.java
      │ └── PaymentService.java
      ├── exception/
      │ ├── GlobalExceptionHandler.java
      │ ├── ResourceNotFoundException.java
      │ └── InsufficientStockException.java
      └── client/
        └── PaymentGatewayClient.java     

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker Desktop(for PostgreSQL)
 - IntelliJ IDEA (recommended)

### Step-by-Step Setup

1. **Clone the repository**
``bash  
git clone <your-repository-url>,
cd onlinemarketplace
    
2. **Start PostgreSQL Database with Docker**
docker run --name marketplace-postgres
-e POSTGRES_USER=postgres
-e POSTGRES_PASSWORD=password
-e POSTGRES_DB=marketplacedb
-p 5432:5432
-d postgres:latest
 
3. **Verify PostgreSQL is running**
   docker ps
   
4. **Build the project**
 mvn clean install
 
5. **Run the application**
mvn spring-boot:run

6. **Run the application**
- API Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

If you want the Docker commands without any formatting:
Start PostgreSQL:
docker run --name marketplace-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=marketplacedb -p 5432:5432 -d postgres:latest

Check if running:
docker ps

Stop PostgreSQL:
docker start marketplace-postgres

Access PostgreSQL shell:
docker exec -it marketplace-postgres psql -U postgres -d marketplacedb

View logs:
docker logs marketplace-postgres

Remove container (if needed):
docker rm -f marketplace-postgres

### Database Connection in IntelliJ

1. Open Database Tool Window: `View` → `Tool Windows` → `Database`
2. Click `+` → `Data Source` → `PostgreSQL`
3. Fill in:
   - Host: `localhost`
   - Port: `5432`
   - Database: `marketplacedb`
   - User: `postgres`
   - Password: `password`
4. Click `Test Connection` → `OK`

Testing with HTTP File
Create a test.http file in your project root with the following requests:

### 1. CREATE A NEW USER
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}

### 2. CREATE CATEGORY (Admin only)
POST http://localhost:8080/api/categories
Authorization: Basic RWJpc2VfR3V0ZW1hOnBhc3N3b3JkMTIz
Content-Type: application/json

{
  "name": "Electronics",
  "description": "Electronic devices"
}

### 3. CREATE PRODUCT (Admin only)
POST http://localhost:8080/api/products
Authorization: Basic RWJpc2VfR3V0ZW1hOnBhc3N3b3JkMTIz
Content-Type: application/json

{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "stockQuantity": 10,
  "categoryIds": [1]
}

### 4. GET ALL PRODUCTS (Public)
GET http://localhost:8080/api/products

### 5. PLACE ORDER (Authenticated user)
POST http://localhost:8080/api/orders
Authorization: Basic RWJpc2VfR3V0ZW1hOnBhc3N3b3JkMTIz
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 1
    }
  ],
  "paymentMethod": "CREDIT_CARD"
}

### 6. UPDATE ORDER STATUS (Admin only)
PATCH http://localhost:8080/api/orders/1/status?status=SHIPPED
Authorization: Basic RWJpc2VfR3V0ZW1hOnBhc3N3b3JkMTIz

Docker Commands Reference
# Start PostgreSQL container
docker start marketplace-postgres

# Stop PostgreSQL container
docker stop marketplace-postgres

# View running containers
docker ps

# View PostgreSQL logs
docker logs marketplace-postgres

# Access PostgreSQL shell
docker exec -it marketplace-postgres psql -U postgres -d marketplacedb

# Remove container (if needed)
docker rm -f marketplace-postgres

Common Error Responses
# 401 Unauthorized
{
  "status": 401,
  "error": "Unauthorized,"
  "message": "Authentication failed"
}

# 404 Not Found
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/products/16"
}

# 400 Bad Request (Validation)
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Price must be greater than 0",
  "path": "/api/products"
}

# 400 Bad Request (Insufficient Stock)
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Insufficient Stock",
  "message": "Insufficient stock for product 'Laptop'. Requested: 100, Available: 10",
  "path": "/api/orders"
}

# Troubleshooting
Issue: Connection refused to PostgreSQL
Solution: Start PostgreSQL container
docker start marketplace-postgres

Issue: Port 5432 already in use
Solution: Change port in docker command and application.properties
docker run --name marketplace-postgres -p 5433:5432 ...

Issue: Authentication fails
Solution: Check a user role in a database
docker exec -it marketplace-postgres psql -U postgres -d marketplacedb -c "SELECT username, role FROM users;"

Issue: Tables not created
Solution: Check spring.jpa.hibernate.ddl-auto=update in application.properties

# Performance Considerations
    Lazy Loading: Used for collections to avoid unnecessary data loading

    Connection Pool: HikariCP for efficient database connections

    Stateless: No session storage for better scalability

    Transaction Management: @Transactional for data consistency

# Future Enhancements
    JWT Authentication instead of Basic Auth

    Redis caching for product listings

    Pagination for product listing endpoints

    Docker Compose for multi-container setup

    Unit and integration tests

# License
This project is licensed under the Apache 2.0 License.

# Contact
    Ebise Gutema  ebisegutema@gmail.com
Project Link: https://github.com/ebii-7/onlinemarketplace
