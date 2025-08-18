# Central Banking System - Backend

A comprehensive Java-based banking system designed to handle multi-currency transactions, fraud detection, and inter-bank communications. Built with Spring Boot and following enterprise-grade patterns, this system demonstrates complex financial domain modeling and real-world banking operations.

## Table of Contents
- [System Overview](#system-overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security Implementation](#security-implementation)
- [Known Issues & Technical Debt](#known-issues--technical-debt)
- [Future Enhancements](#future-enhancements)
- [Contributing](#contributing)
- [License](#license)

## System Overview

This central banking system serves as the backbone for processing financial transactions between multiple banks. It handles currency conversions using real-time exchange rates, implements sophisticated fraud detection algorithms, and maintains comprehensive audit trails for regulatory compliance.

The system was built to understand and implement complex financial domain logic, demonstrating proficiency in enterprise Java development, security patterns, and financial technology concepts.

## Key Features

### Core Banking Operations
- **Multi-Currency Transactions**: Support for USD, EUR, and GBP with real-time conversion
- **Inter-Bank Communication**: Secure API-based communication between financial institutions
- **Account Management**: Complete lifecycle management for bank accounts
- **Transaction Processing**: Atomic transaction processing with rollback capabilities

### Security & Compliance
- **JWT Authentication**: Role-based access control with refresh token support
- **Fraud Detection**: Multi-layered fraud detection using amount thresholds, velocity checks, and pattern analysis
- **Rate Limiting**: Configurable rate limiting to prevent DDoS and abuse
- **Audit Logging**: Comprehensive audit trails for all system actions

### Operational Features
- **Real-Time Exchange Rates**: Integration with Frankfurter API for live currency data
- **Scheduled Processing**: Background processing for fraud case reviews and rate updates
- **Error Handling**: Global exception handling with detailed error responses
- **API Documentation**: Complete Swagger/OpenAPI documentation

## Architecture

The system follows a layered architecture pattern:

```
Controllers (REST API)
    ↓
Services (Business Logic)
    ↓
Repositories (Data Access)
    ↓
Database (MySQL)
```

### Key Design Patterns
- **Repository Pattern**: Clean separation between business logic and data access
- **DTO Pattern**: Data transfer objects for API communication
- **Service Layer**: Encapsulation of business logic
- **Dependency Injection**: Spring's IoC container for loose coupling

## Technology Stack

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Database**: MySQL with Flyway migrations
- **Security**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **ORM**: Spring Data JPA
- **Mapping**: MapStruct for DTO conversions

## Getting Started

### Prerequisites
- Java 21 or higher
- MySQL 8.0+
- Maven 3.6+

### Environment Setup
Create a `.env` file in the project root:
```env
DATABASE_URL=jdbc:mysql://localhost:3306/centralBank?createDatabaseIfNotExist=true
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_key
INTERBANK_SECRET=your_interbank_secret
BANK_MASTER_SECRET=your_master_secret
```

### Installation
1. Clone the repository
```bash
git clone https://github.com/Johnbivo/CentralBankSystem.git
cd CentralBankSystem
```

2. Install dependencies
```bash
mvn clean install
```

3. Run database migrations
```bash
mvn flyway:migrate
```

4. Start the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/api-docs`

### Authentication
Most endpoints require JWT authentication. To test:
1. Register/Login via `/auth/login`
2. Copy the JWT token from the response
3. Click "Authorize" in Swagger UI
4. Enter: `Bearer your_jwt_token`

## Database Schema

The system uses the following core entities:
- **Banks**: Financial institutions in the network
- **Accounts**: Customer accounts with balance and currency
- **Transactions**: Financial transfers between accounts
- **Employees**: System users with role-based access
- **FraudCases**: Flagged transactions for review
- **AuditLogs**: Comprehensive activity logging

Database migrations are managed with Flyway and located in `src/main/resources/db/migration/`.

## Security Implementation

### Authentication & Authorization
- JWT-based stateless authentication
- Role-based access control (ADMIN, EMPLOYEE, BANK)
- Secure password hashing with BCrypt
- Refresh token mechanism for session management

### Rate Limiting
Configurable rate limits for different operations:
- Global API rate limiting
- Authentication attempt restrictions
- Transaction frequency controls
- Fraud review operation limits

### Data Protection
- Input validation and sanitization
- SQL injection prevention through parameterized queries
- XSS protection through proper encoding
- CSRF protection disabled for stateless API

## Known Issues & Technical Debt

### Architecture Concerns
1. **Service Layer Coupling**: Some services have multiple responsibilities that could be further separated
2. **Transaction Processing**: The TransactionService handles both same-bank and inter-bank logic, creating complexity
3. **Configuration Management**: Some business rules are hardcoded rather than externalized

### Security Improvements Needed
1. **Multi-Factor Authentication**: Currently only supports single-factor JWT authentication
2. **Field-Level Encryption**: Sensitive data like account numbers should be encrypted at rest
3. **API Rate Limiting**: Could benefit from more sophisticated rate limiting algorithms

### Operational Gaps
1. **Monitoring**: No health check endpoints or application metrics
2. **Backup Strategy**: No automated backup procedures implemented
3. **Test Coverage**: Limited unit and integration test coverage
4. **Error Recovery**: Manual intervention required for some failed transaction scenarios

### Compliance Considerations
1. **KYC Integration**: Know Your Customer workflows not implemented
2. **AML Screening**: Anti-Money Laundering checks beyond basic fraud detection needed
3. **Regulatory Reporting**: Automated regulatory report generation missing

## Future Enhancements

### Short-term Improvements
- [ ] Implement comprehensive unit test suite
- [ ] Add health check endpoints with Spring Boot Actuator
- [ ] Extract fraud detection rules to configuration
- [ ] Implement proper logging framework with structured logs

### Medium-term Goals
- [ ] Add real-time notifications (email, SMS)
- [ ] Implement advanced fraud detection with machine learning
- [ ] Add support for additional currencies and payment methods
- [ ] Create admin dashboard for system monitoring

### Long-term Vision
- [ ] Microservices architecture migration
- [ ] Integration with external payment networks (SWIFT, ACH)
- [ ] Blockchain integration for settlement
- [ ] Mobile API development

## Contributing

This project is currently maintained as a portfolio demonstration. While contributions are not actively sought, feedback and suggestions are welcome.

### Development Guidelines
- Follow existing code style and patterns
- Include comprehensive tests for new features
- Update documentation for API changes
- Ensure security considerations are addressed

## License

This project is licensed under a Proprietary Read-Only License. See the [LICENSE](LICENSE) file for details.

The software is provided for educational and portfolio demonstration purposes. Commercial use, modification, or distribution is not permitted without explicit authorization.

---

**Note**: This system is a demonstration of enterprise Java development capabilities and should not be used in production environments without significant additional security hardening, testing, and compliance verification.

## Contact

For questions about this project or potential collaboration opportunities, please reach out through GitHub issues or direct contact.

---
*Built with Java 21, Spring Boot, and a passion for clean, maintainable code.*
