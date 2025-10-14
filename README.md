# Sunflower - E-commerce Backend API

A Spring Boot application providing RESTful API services for an e-commerce platform with JWT authentication, Azure Blob Storage integration, and MySQL database.

## 🛠️ Technologies Used

### Backend Framework
- **Spring Boot 3.3.2** - Main application framework
- **Spring Web** - RESTful web services
- **Spring Data JPA** - Database access layer
- **Hibernate 6.5.2** - ORM framework

### Security
- **Spring Security** - Authentication and authorization
- **Spring Security OAuth2 Resource Server** - OAuth2 support
- **Spring Security OAuth2 JOSE** - JWT token handling
- **Auth0 Java JWT** - JWT token generation and validation

### Database
- **MySQL** - Relational database
- **MySQL Connector/J 8.3.0** - JDBC driver
- **HikariCP** - Connection pooling

### Cloud Storage
- **Azure Storage Blob 12.25.0** - Cloud file storage
- **Cloudinary** - Image hosting and manipulation

### API Documentation
- **SpringDoc OpenAPI 2.5.0** - API documentation
- **Swagger UI** - Interactive API documentation interface

### Development Tools
- **Lombok 1.18.34** - Reduce boilerplate code
- **MapStruct 1.5.5** - Object mapping
- **Spotless** - Code formatting

### HTTP Client
- **Spring Cloud OpenFeign 4.1.3** - Declarative REST client

### Build & Deployment
- **Maven** - Build automation tool
- **Spring Boot Maven Plugin** - Package Spring Boot applications

### Additional Libraries
- **Dotenv Java 3.0.0** - Environment variable management
- **JSON 20250107** - JSON processing
- **Hibernate Validator** - Bean validation

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Azure Storage Account** (for blob storage)
- **Cloudinary Account** (optional, for image hosting)

## ⚙️ Installation & Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd BR-Subflower
```

### 2. Configure Environment Variables

Copy the example environment file and update with your credentials:

```bash
cp .env.example .env
```

Edit `.env` file with your actual configuration values.

### 3. Build the project

```bash
./mvnw clean install
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080/api`

## 📚 API Documentation

Once the application is running, access the interactive API documentation at:

- **Swagger UI:** http://localhost:8080/api/swagger-ui.html
- **API Docs:** http://localhost:8080/api/api-docs

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/hls/sunflower/
│   │   ├── configuration/      # Application configurations
│   │   ├── controller/         # REST API controllers
│   │   ├── dao/               # Repository interfaces
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # JPA entities
│   │   ├── exception/         # Exception handling
│   │   ├── mapper/            # MapStruct mappers
│   │   ├── service/           # Business logic services
│   │   └── util/              # Utility classes
│   └── resources/
│       ├── application.yml    # Application configuration
│       └── META-INF/
│           └── spring.factories
└── test/                      # Test files
```

## 🔑 Environment Variables

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | MySQL database connection URL |
| `DATABASE_USERNAME` | Database username |
| `DATABASE_PASSWORD` | Database password |
| `JWT_SIGNER_KEY` | Secret key for JWT signing |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `GOOGLE_REDIRECT_URI` | OAuth redirect URI |
| `API_ACCESS_KEY` | Kling AI API access key |
| `API_SECRET_KEY` | Kling AI API secret key |
| `AZURE_STORAGE_CONNECTION_STRING` | Azure Storage connection string |
| `AZURE_STORAGE_CONTAINER_NAME` | Azure blob container name |

## 🔒 Security Features

- JWT (JSON Web Tokens) for stateless authentication
- OAuth2 for social login (Google)
- Spring Security for endpoint protection
- Role-based access control

## 📦 Building for Production

```bash
./mvnw clean package -DskipTests
```

The JAR file will be created in the `target/` directory.

## 📝 License

This project is licensed under the MIT License.
