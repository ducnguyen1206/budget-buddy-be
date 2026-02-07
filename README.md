# Budget Buddy - Backend API

A comprehensive financial tracking and budget management REST API built with Spring Boot 3.5 and Java 21.

## 📋 Overview

Budget Buddy is a personal finance management system that helps users track transactions, manage budgets, set spending thresholds, and monitor their financial health across multiple accounts and currencies.

### Key Features

- **Multi-Account Management** - Track multiple bank accounts, credit cards, and wallets
- **Transaction Tracking** - Record income, expenses, and transfers with categorization
- **Budget Management** - Set monthly budgets per category with spending alerts
- **Threshold Monitoring** - Configure spending limits and track daily threshold violations
- **Installment Tracking** - Manage recurring payments and installment plans
- **Subscription Management** - Track recurring subscriptions and memberships
- **Savings Goals** - Set and monitor savings targets
- **Multi-Currency Support** - Handle transactions in VND and SGD
- **User Authentication** - Secure JWT-based authentication with Google OAuth2 support

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.9
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Liquibase
- **Security**: Spring Security with JWT
- **API Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven 3.9
- **Containerization**: Docker

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- PostgreSQL 16+
- Redis 7+
- Docker & Docker Compose (optional)

### Environment Variables

Create a `.env` file in the project root with the following variables:

```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/budgetbuddy
DB_USERNAME=budgetbuddy
DB_PASSWORD=your_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Application Configuration
APP_CORS=http://localhost:5174
COOKIES=false

# JWT Configuration
JWT_SECRET=your_jwt_secret_key

# Email Configuration (for verification emails)
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
EMAIL_CLIENT_ID=your_oauth_client_id
EMAIL_CLIENT_SECRET=your_oauth_client_secret

# Google OAuth Configuration
GOOGLE_URL=https://oauth2.googleapis.com/token
GOOGLE_USER_INFO_URL=https://www.googleapis.com/oauth2/v2/userinfo
```

### Option 1: Run with Docker Compose (Recommended)

The easiest way to run the entire stack:

```bash
# Start all services (app, PostgreSQL, Redis)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

The API will be available at `http://localhost:8080`

### Option 2: Run Locally

1. **Start PostgreSQL and Redis** (using Docker):
   ```bash
   docker-compose up -d postgres redis
   ```

2. **Build the application**:
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

   Or run the JAR directly:
   ```bash
   java -jar target/BudgetBuddy-0.0.1-SNAPSHOT.jar
   ```

### Option 3: Development Mode

Run with hot reload for development:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Main API Endpoints

| Module | Endpoint | Description |
|--------|----------|-------------|
| **Auth** | `POST /api/v1/auth/register` | Register new user |
| | `POST /api/v1/auth/login` | Login with credentials |
| | `POST /api/v1/auth/google` | Login with Google OAuth |
| **Accounts** | `GET /api/v1/accounts` | List all accounts |
| | `POST /api/v1/accounts` | Create new account |
| **Transactions** | `GET /api/v1/transactions` | List transactions with filters |
| | `POST /api/v1/transactions` | Create transaction |
| | `GET /api/v1/transaction/threshold` | Get threshold violations |
| **Budgets** | `GET /api/v1/budgets` | List budgets with date range |
| | `POST /api/v1/budgets` | Create budget |
| | `PUT /api/v1/budgets/{id}` | Update budget |
| **Categories** | `GET /api/v1/categories` | List categories |
| | `POST /api/v1/categories` | Create custom category |
| **Thresholds** | `GET /api/v1/thresholds` | List spending thresholds |
| | `POST /api/v1/thresholds` | Create threshold |

## 🧪 Testing

Run all tests:
```bash
./mvnw test
```

Run specific test class:
```bash
./mvnw test -Dtest=BudgetDataImplTest
```

Run tests with coverage report:
```bash
./mvnw clean test jacoco:report
```

View coverage report at: `target/site/jacoco/index.html`

## 🏗️ Project Structure

```
src/main/java/com/budget/buddy/
├── core/                          # Core shared components
│   ├── config/                    # Configuration classes
│   ├── security/                  # Security & JWT handling
│   └── utils/                     # Utility classes
├── transaction/                   # Transaction module
│   ├── application/               # API layer (Controllers, DTOs)
│   ├── domain/                    # Business logic & entities
│   └── infrastructure/            # Data access (Repositories)
└── user/                          # User module
    ├── application/               # User management APIs
    ├── domain/                    # User entities & services
    └── infrastructure/            # User repositories

src/main/resources/
├── application.properties         # Application configuration
└── db/changelog/                  # Liquibase migrations
```

## 🔧 Database Migrations

Liquibase automatically runs migrations on startup. To manually manage migrations:

```bash
# Generate changelog diff
./mvnw liquibase:diff

# Rollback last changeset
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Update database
./mvnw liquibase:update
```

## 🚢 Deployment

### Jenkins CI/CD Pipeline

The project includes a `Jenkinsfile` for automated deployment:

1. **Build JAR** - Compiles and packages the application
2. **Build Docker Image** - Creates Docker image with the JAR
3. **Deploy** - Deploys to production using Docker with host networking

### Manual Docker Deployment

```bash
# Build the JAR
./mvnw clean package -DskipTests

# Build Docker image
docker build -t budget-buddy:latest .

# Run container
docker run -d \
  --name budget-backend \
  --restart always \
  --network="host" \
  --env-file .env \
  budget-buddy:latest
```

## 📝 Development Guidelines

### Code Style
- Follow standard Java conventions
- Use Lombok annotations to reduce boilerplate
- Write meaningful commit messages
- Add Javadoc for public APIs

### Adding New Features
1. Create entity in `domain/model/`
2. Create repository in `infrastructure/repository/`
3. Create DTOs in `application/dto/`
4. Implement service in `domain/service/`
5. Create controller in `application/controller/`
6. Add Liquibase migration in `db/changelog/changes/`
7. Write unit tests

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- **Budget Buddy Team** - Initial work

## 🐛 Troubleshooting

### Common Issues

**Port already in use:**
```bash
# Find process using port 8080
lsof -i :8080
# Kill the process
kill -9 <PID>
```

**Database connection failed:**
- Ensure PostgreSQL is running
- Check database credentials in `.env`
- Verify database exists: `psql -U budgetbuddy -d budgetbuddy`

**Redis connection failed:**
- Ensure Redis is running: `redis-cli ping`
- Check Redis host/port configuration

**Liquibase migration failed:**
- Check migration files in `db/changelog/changes/`
- Manually rollback if needed
- Clear Liquibase lock: `UPDATE DATABASECHANGELOGLOCK SET LOCKED=FALSE;`

## 📞 Support

For issues and questions, please open an issue on GitHub.