# 📚 Getir Final Case – Library Management System


A Spring Boot-based microservices application for managing a library system, developed as part of a bootcamp project. 

It provides functionality for user registration, authentication, book management, borrowing operations, and real-time stock updates using Server-Sent Events (SSE).

![Description of Image](https://github.com/kaanacikgoz/getir-final-case/blob/main/Getir-final-case-architecture.png)

---

## 🚀 Features

- 👥 User registration (PATRON & LIBRARIAN roles)
- 🔐 JWT-based authentication and authorization
- 📚 Book CRUD operations (create, update, delete, search)
- 📖 Book borrowing & return logic
- 📡 Real-time stock availability stream (Spring WebFlux)
- 📊 Overdue borrowing reports
- 📄 Global error handling with detailed error responses
- ✅ Comprehensive test coverage with Jacoco

---

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.4**
- **Spring Security**
- **Spring Data JPA**
- **Spring Cloud Netflix Eureka (Discovery Server)**
- **Spring WebFlux (SSE)**
- **PostgreSQL**
- **Maven**
- **Docker & Docker Compose**
- **JWT (JSON Web Tokens)**
- **Feign Client (inter-service communication)**
- **Jacoco (test coverage)**
- **Postman (API documentation)**

---

  
## 📖 API Documentation

- 📬 [Postman Collection](https://documenter.getpostman.com/view/36979805/2sB2qUmPwJ)
- 🧭 Swagger UIs:
  - [User Service:](http://localhost:8081/swagger-ui/index.html)
  - [Book Service:](http://localhost:8082/swagger-ui/index.html)
  - [Borrowing Service:](http://localhost:8083/swagger-ui/index.html)
 
---

## 🧑‍💻 Run Locally

To run the application without Docker:

### 🧾 Prerequisites
- Java 21+
- Maven
- PostgreSQL installed and running locally
- Optional: Postman or any REST client for API testing

### 🛠️ Step-by-Step Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/getir-final-case.git
   cd getir-final-case
   ```
   
2. **Start PostgreSQL databases:**
Ensure that PostgreSQL is running with the following databases created:
   - `userdb`
   - `bookdb`
   - `borrowingdb`
   (or configure your own database URLs in each module's `application.yml`)

3. **Run Eureka Discovery Server first:**
   ```bash
   cd discovery-server && mvn spring-boot:run
   ```
4. **Run the other services in separate terminals:**
   ```bash
   cd user-service && mvn spring-boot:run
   cd book-service && mvn spring-boot:run
   cd borrowing-service && mvn spring-boot:run
   cd api-gateway && mvn spring-boot:run
   ```
5. **Test the application:**
  - Use Swagger UI or Postman to interact with the endpoints.
  - Swagger URLs:
      - `http://localhost:8081/swagger-ui/index.html` (user-service)
      - `http://localhost:8082/swagger-ui/index.html` (book-service)
      - `http://localhost:8083/swagger-ui/index.html` (borrowing-service)
  

## 🐳 Docker Setup & Run Instructions
If you prefer running the application in Docker containers:

### 🧾 Prerequisites
- Docker & Docker Compose installed

### 🧱 Build and Start Containers
1. **Build the JARs for all services:**
   ```bash
   mvn clean package
   ```
2. **Start all services via Docker Compose:**
   ```bash
   docker-compose up --build
   ```
   This will:
     - Build and run all microservices (user, book, borrowing, api-gateway, discovery-server)
     - Spin up PostgreSQL containers for each service
  
3. **Access the services:**
   - API Gateway: http://localhost:8080
   - Swagger UIs:
       - http://localhost:8081/swagger-ui/index.html
       - http://localhost:8082/swagger-ui/index.html
       - http://localhost:8083/swagger-ui/index.html

4. **Shut down the containers when done:**
   ```bash
   docker-compose down
   ```
   ℹ️ Note: The app automatically picks up application-docker.yml when running inside Docker.
   
---

## 🗃️ Database Schema

### 👥 User Service

| Table  | Columns | Description |
|--------|---------|-------------|
| `users` | **id** (UUID) <br> **email** (String) <br> **password** (String) <br> **role** (Enum) <br> name (String) <br> surname (String) <br> phone (String) <br> address (String) <br> createdAt (Timestamp) <br> updatedAt (Timestamp) | Auth and User Crud operations. |

### 📚 Book Service

| Table  | Columns | Description |
|--------|---------|-------------|
| `books` | **id** (UUID) <br> **title** (String) <br> **author** (String) <br> **isbn** (String) <br> publication_year (Integer) <br> genre (String) <br> **stock** (int) | Independent book catalog with inventory management. |

### 🔄 Borrowing Service

| Table  | Columns | Description |
|--------|---------|-------------|
| `borrowings` | **id** (UUID) <br> **user_reference** (UUID) <br> **book_reference** (UUID) <br> **borrow_date** (Timestamp) <br> **due_date** (Timestamp) <br> return_date (Timestamp) | Transaction records using ID references. Maintains eventual consistency through events. |


---

## 🧪 Testing
Run tests and generate coverage:
```bash
   mvn clean verify
```
Jacoco reports will be generated under:
```bash
   cd <module-name>/target/site/jacoco/index.html
```
📂 Replace `<module-name>` with the actual service name (e.g., user-service, book-service, etc.).

## 📝 Additional Notes

- 👤 **Default Librarian Creation**  
  On initial startup, the system **automatically creates a default user with the `LIBRARIAN` role**  
  to ensure immediate administrative access for testing and management.

  **Demo Credentials:**
  ```java
      Email : admin@getir.com
      Password: 123456
  ```

- 🐳 **Docker Support**  
  Containerized deployment configurations are handled using `application-docker.yml`.  
  Ensure the correct Spring profile (e.g., `docker`) is activated when deploying via Docker Compose.

- 🔁 **Inter-Service Communication**  
  All microservices communicate through **Feign Clients**, enabling type-safe and declarative HTTP calls between services.

- ⚠️ **Global Error Handling**  
  A centralized `GlobalExceptionHandler` standardizes error responses across all services.  
  Most exceptions are also **logged** with appropriate severity (`warn`, `error`) within:
  - GlobalExceptionHandler
  - Service methods
  - Filters (e.g., JWT authentication)

## Author
Kaan Açıkgöz – [LinkedIn](https://www.linkedin.com/in/acikgozkaan/)

Bootcamp Final Project @ Getir
