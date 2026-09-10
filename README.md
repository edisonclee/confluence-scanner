# Confluence Scanner

A cryptocurrency market scanner built with **Java 17 and Spring Boot**.

The application collects cryptocurrency market data and applies technical-analysis conditions to identify potential market setups. It is designed as a REST API backend with a separate React-based frontend.

## Features

* Binance Futures market data integration
* Bitunix symbol filtering
* Higher-timeframe Bollinger Band analysis
* Heiken Ashi analysis
* Bollinger Band Width ranking
* Multi-timeframe confluence detection
* REST API
* OpenAPI / Swagger documentation
* Docker support

## Architecture

```text
                    ┌─────────────────────┐
                    │    React UI         │
                    │  confluence-ui      │
                    └──────────┬──────────┘
                               │
                               │ REST API
                               ▼
                    ┌─────────────────────┐
                    │   Spring Boot API   │
                    │ ScannerController   │
                    └──────────┬──────────┘
                               ▼
                    ┌─────────────────────┐
                    │   ScannerService    │
                    └──────────┬──────────┘
                               ▼
                    ┌─────────────────────┐
                    │    ScannerEngine    │
                    └──────────┬──────────┘
                               ▼
                    ┌─────────────────────┐
                    │ MarketDataLoader    │
                    └──────────┬──────────┘
                               ▼
                    ┌─────────────────────┐
                    │ Binance Futures API │
                    └─────────────────────┘
```

## Current Scanner

The current implementation focuses on **Bollinger Band-based market scanning**.

The scanner uses:

* Heiken Ashi candles
* Higher-timeframe Bollinger Bands
* Bollinger Band Width
* Multi-timeframe conditions
* Symbol filtering
* Market-data processing and ranking

The goal is to reduce the amount of manual chart scanning required when looking for potential setups.

## Technology Stack

| Area              | Technology          |
| ----------------- | ------------------- |
| Language          | Java 17             |
| Backend           | Spring Boot         |
| API               | REST                |
| JSON              | Jackson             |
| Build             | Maven               |
| API Documentation | OpenAPI / Swagger   |
| Containerization  | Docker              |
| Market Data       | Binance Futures API |

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── ...
│   └── resources/
│       └── ...
├── test/
│   └── ...
│
├── docs/
│   └── architecture.md
│
├── Dockerfile
├── pom.xml
└── README.md
```

## Running Locally

### Requirements

* Java 17 or later
* Maven

### Run with Maven

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

### Build

```bash
./mvnw clean package
```

Windows:

```bat
mvnw.cmd clean package
```

## API Documentation

The application includes OpenAPI / Swagger support for exploring and testing the REST endpoints.

Once the application is running, open the Swagger UI provided by the application.

## Docker

The project includes a `Dockerfile` for running the backend in a container.

Build the image:

```bash
docker build -t confluence-scanner .
```

Run the container:

```bash
docker run -p 8080:8080 confluence-scanner
```

## Related Project

The frontend is maintained in a separate repository:

**confluence-ui**

The frontend communicates with this application through REST APIs.

## Roadmap

Planned scanner modules include:

* RSI
* MACD
* Volume
* Open Interest
* Funding Rate
* EMA
* Additional market-analysis metrics

## Project Status

This is an actively developed personal project.

The application is being expanded from a technical-analysis scanner into a broader cryptocurrency market-analysis tool.

## Why I Built This

I built this project to combine backend engineering with a practical market-analysis problem.

It provides hands-on experience with:

* Java and Spring Boot
* REST API development
* External API integration
* Market-data processing
* Technical-analysis calculations
* Application architecture
* Docker-based deployment
* Building a separate frontend and backend application
