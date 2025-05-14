# 📚 Getir Final Case – Library Management System


A Spring Boot-based microservices application for managing a library system, developed as part of a bootcamp project. 

It provides functionality for user registration, authentication, book management, borrowing operations, and real-time stock updates using Server-Sent Events (SSE).

---

## 🚀 Features

- 👥 User registration (PATRON & LIBRARIAN roles)
- 🔐 JWT-based authentication and authorization
- 📚 Book CRUD operations (create, update, delete, search)
- 📖 Book borrowing & return logic
- 📡 Real-time stock availability stream (Spring WebFlux)
- 📊 Overdue borrowing reports (including CSV export)
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

### User Service Database
- `users` table: id, email, password, role, name, surname, phone, address, createdAt, updatedAt

### Book Service Database
- `books` table: id, title, author, isbn, publication_year, genre, stock

### Borrowing Service Database
- `borrowings` table: id, user_id, book_id, borrow_date, due_date, return_date

---
  
## 📖 API Documentation

- 📬 [Postman Collection](https://documenter.getpostman.com/view/36979805/2sB2qUmPwJ)
- 🧭 Swagger UIs:
  - [User Service:](http://localhost:8081/swagger-ui/index.html)
  - [Book Service:](http://localhost:8082/swagger-ui/index.html)
  - [Borrowing Service:](http://localhost:8083/swagger-ui/index.html)

---

## Testing
Run tests and generate coverage:
```bash
mvn clean verify
```
Jacoco reports will be generated under:
cd <module-name>/target/site/jacoco/index.html

## Additional Notes
- Use application-docker.yml for containerized deployments.
- Feign clients are used for communication between services.
- GlobalExceptionHandler standardizes all error responses.

## Author
Kaan Açıkgöz – [LinkedIn](https://www.linkedin.com/in/acikgozkaan/)

Bootcamp Final Project @ Getir
