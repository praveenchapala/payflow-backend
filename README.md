# PayFlow — Backend

A Spring Boot REST API for a full-stack payment gateway integration project using Razorpay Test Mode.

## Overview

PayFlow Backend handles payment-order creation and provides the server-side foundation for integrating Razorpay into a web application.

The backend is built with Java and Spring Boot. It communicates with Razorpay using the Razorpay Java SDK and exposes HTTP endpoints consumed by the React frontend.

## Features

- REST API built with Spring Boot
- Razorpay order creation
- Integration with the Razorpay Java SDK
- Environment-variable-based credential configuration
- Health-check endpoint
- Maven-based dependency management and build
- Cross-origin configuration for local frontend development

> **Current status:** Backend order creation works. Server-side payment-signature verification and database persistence are the next implementation milestones.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Build tool:** Maven
- **Payment gateway:** Razorpay
- **Payment SDK:** Razorpay Java SDK
- **Configuration:** Spring Boot `application.properties`
- **Version control:** Git and GitHub

## Project Structure

```text
payflow-backend/
└── payflow-backend/
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── com/payflow/
        │   │       ├── controller/
        │   │       ├── service/
        │   │       └── PayflowBackendApplication.java
        │   └── resources/
        │       └── application.properties
        └── test/
```

The repository currently contains the Maven project in the nested `payflow-backend` directory.

## Prerequisites

- Java 21 or a compatible configured JDK
- Git
- Internet access for resolving Maven dependencies
- A Razorpay account with Test Mode credentials

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/praveenchapala/payflow-backend.git
cd payflow-backend/payflow-backend
```

### 2. Configure environment variables

The `src/main/resources/application.properties` file references environment variables:

```properties
spring.application.name=payflow-backend
razorpay.key.id=${RAZORPAY_KEY_ID}
razorpay.key.secret=${RAZORPAY_KEY_SECRET}
```

Set your own Razorpay **Test Mode** credentials in PowerShell:

```powershell
$env:RAZORPAY_KEY_ID="YOUR_RAZORPAY_TEST_KEY_ID"
$env:RAZORPAY_KEY_SECRET="YOUR_RAZORPAY_TEST_KEY_SECRET"
```

Replace the placeholders locally with your credentials. Never commit actual secrets to GitHub.

These variables apply to the current PowerShell session. Set them again in any new terminal session used to start the backend.

### 3. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application is configured to run locally on port `8080`, unless changed in the application configuration.

### 4. Verify the backend

Open the health endpoint:

`http://localhost:8080/api/health`

The endpoint should respond when the backend is running.

## API Overview

| Endpoint | Purpose |
|---|---|
| `GET /api/health` | Check whether the backend is running |
| Payment order-creation endpoint | Create a Razorpay order for the frontend |

Consult the current `PaymentController.java` for the exact HTTP method, path, request format, and response schema of the order-creation endpoint.

## Payment Workflow

1. The frontend requests an order from the backend.
2. The backend communicates with Razorpay to create the order.
3. The backend returns the order details and public Checkout key ID.
4. The frontend opens Razorpay Checkout using the order details.
5. After Checkout, the frontend can send the payment response to the backend for verification.

The final verification step is not yet implemented in the current version.

## Build and Test

Run the test suite and verify the Maven build:

```powershell
.\mvnw.cmd clean verify
```

To inspect dependency resolution:

```powershell
.\mvnw.cmd dependency:tree
```

## Security Considerations

- Keep the Razorpay secret key on the server.
- Do not hardcode credentials in Java source files or commit them to version control.
- Verify the Razorpay payment signature on the backend before marking a payment as verified.
- Use the backend-created order ID when verifying a payment.
- Validate incoming request data and handle failed or repeated requests safely.

## Planned Improvements

- Implement Razorpay payment-signature verification
- Add payment-status handling
- Persist orders and transactions in MySQL
- Handle duplicate requests and payment failures
- Process Razorpay webhooks securely
- Add unit and integration tests
- Deploy the backend with secure environment configuration

## Related Repository

**Frontend:** https://github.com/praveenchapala/payflow-frontend

## Learning Objectives

This project demonstrates REST API development, Maven dependency management, third-party payment gateway integration, secure configuration, and backend payment verification concepts.
