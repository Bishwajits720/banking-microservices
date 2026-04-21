# 🏦 Banking Microservices System

A Spring Boot microservices-based banking system implementing account management and transaction processing with concurrency handling, idempotency, and production-grade error handling.

---

## 🚀 Tech Stack

- Java 11
- Spring Boot 2.7
- Spring Data JPA (Hibernate)
- H2 Database
- OpenFeign (Inter-service communication)
- JUnit + Mockito
- Lombok

---

## 🧩 Microservices

### 1. Account Service (Port: 8081)

Handles:
- Account creation
- Balance management
- Debit/Credit operations
- Concurrency handling using optimistic locking (`@Version`)

---

### 2. Transaction Service (Port: 8082)

Handles:
- Deposit
- Withdraw
- Transfer
- Transaction history
- Idempotency using `transactionId`

---

## 🔗 Architecture

Client → Transaction Service → Account Service → Database

---

## ⚙️ Features

### ✅ Concurrency Handling
- Optimistic locking using `@Version`
- Prevents race conditions and negative balances

### ✅ Idempotency
- Duplicate transactions prevented using `transactionId`

### ✅ Error Handling
- Centralized exception handling using `@ControllerAdvice`
- Consistent API response format

### ✅ Security
- API Key based authentication (`API-KEY`)

### ✅ Inter-Service Communication
- Feign Client with interceptor for header propagation

### ✅ Compensation Logic
- Transfer rollback implemented (Saga-lite pattern)

---

## 📡 API Endpoints

### 🏦 Account Service

| API | Method | Description |
|-----|--------|------------|
| `/accounts/get` | POST | Create account |
| `/accounts/getBy/{id}` | GET | Get account |
| `/accounts/credit` | POST | Credit account |
| `/accounts/debit` | POST | Debit account |

---

### 💸 Transaction Service

| API | Method | Description |
|-----|--------|------------|
| `/transactions/deposit` | POST | Deposit money |
| `/transactions/withdraw` | POST | Withdraw money |
| `/transactions/transfer` | POST | Transfer money |

---

## 🔐 Headers

API-KEY: secret123

to  access api ->

## 📸 Screenshots
ADDED of Unit Tesing ALSO UNIT TESING CASES ADDED FOR THESE services .
