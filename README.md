# Lending Record Manager

A web application to replace a paper register used to record money given, taken, and kept with a person.

## Status
In progress. Currently built: people records (create, list, delete) through a REST API.

## Planned features
- Accounts per person (given, taken, kept) with due dates and guarantor
- Per-person interest rate with a grace period and late charges
- Partial repayments and extra money taken
- Monthly statement per person
- Overdue and deadline dashboard

## Tech stack
Java 21, Spring Boot, Spring Data JPA (Hibernate), MySQL, REST APIs

## How to run
1. Create a MySQL database named `lending_db`.
2. Create `src/main/resources/application-local.properties` with your datasource URL, username, and password.
3. Run `LendingApplication`. The API is at `http://localhost:8081/api/people`.
