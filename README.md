# 🎬 Watchora

**Watchora** is a video streaming application built with a microservices architecture. Videos are uploaded, encoded, catalogued, and streamed by independent services that communicate through Kafka events, with MySQL for the movie catalog and Redis for caching streaming URLs.

---

## 📑 Table of Contents

- [Architecture](#-architecture)
- [Services](#-services)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Infrastructure Details](#-infrastructure-details)
- [Configuration](#-configuration)
- [Roadmap](#-roadmap)
- [Author](#-author)

---

## 🏗 Architecture

Watchora is split into four services plus shared infrastructure (Kafka, MySQL, Redis) that runs in Docker.

```
                ┌──────────────────┐
                │  videos-service  │
                └────────┬─────────┘
                         │ events
                         ▼
   ┌──────────────┐   ┌───────┐   ┌───────────────────┐
   │ encoding-    │◄──┤ Kafka ├──►│  content-service  │──► MySQL (content_db)
   │ service      │   └───┬───┘   └───────────────────┘
   └──────────────┘       │
                          ▼
                ┌───────────────────┐
                │ streaming-service │──► Redis (streaming URL cache)
                └───────────────────┘
```

<!-- TODO: Adjust the diagram so the arrows match your actual event flow (which service produces / consumes which topic). -->

---

## 🧩 Services

| Service | Responsibility |
|---|---|
| `videos-service` | Handles video uploads and video-level operations. |
| `encoding-service` | Processes and encodes uploaded videos into streamable formats. |
| `content-service` | Manages the movie catalog (titles, metadata) stored in MySQL. |
| `streaming-service` | Serves video streams and caches streaming URLs in Redis for fast playback. |

<!-- TODO: Replace the one-line descriptions above with what each service really does: main endpoints, Kafka topics produced/consumed, and default port. -->

---

## 🛠 Tech Stack

**Infrastructure (via Docker Compose)**

| Component | Image | Purpose |
|---|---|---|
| Apache Kafka | `confluentinc/cp-kafka:7.4.0` | Event streaming between services |
| Zookeeper | `confluentinc/cp-zookeeper:7.4.0` | Required by Kafka |
| MySQL | `mysql:8.0` | Movie catalog storage |
| Redis | `redis:latest` | Streaming URL cache |

**Application services**

<!-- TODO: List language / framework / build tool, e.g. Java 17, Spring Boot, Maven, FFmpeg for encoding, etc. -->

---

## 📂 Project Structure

```
Watchora/
├── content-service/      # Movie catalog service
├── encoding-service/     # Video encoding service
├── streaming-service/    # Video streaming service
├── videos-service/       # Video upload / management service
├── docker-compose.yml    # Kafka, Zookeeper, MySQL, Redis
└── .gitignore
```

---

## 🚀 Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- <!-- TODO: JDK / Node.js version required by the services -->
- <!-- TODO: FFmpeg, if encoding-service needs it installed locally -->

### 1. Clone the repository

```bash
git clone https://github.com/Eng-Shailendra/Watchora.git
cd Watchora
```

### 2. Start the infrastructure

```bash
docker compose up -d
```

This starts Redis, MySQL, Zookeeper, and Kafka on a shared `watchora-network` bridge network. Check that everything is running:

```bash
docker compose ps
```

### 3. Run the services

Start each service from its own folder:

```bash
# Example — replace with your actual run command
cd content-service
# TODO: e.g. ./mvnw spring-boot:run   or   npm install && npm start
```

Repeat for `videos-service`, `encoding-service`, and `streaming-service`.

### 4. Stop everything

```bash
docker compose down          # stop containers
docker compose down -v       # stop and delete MySQL data volume
```

---

## 🔌 Infrastructure Details

| Component | Host address (from your machine) | Address inside Docker network |
|---|---|---|
| MySQL | `localhost:3306` | `mysql:3306` |
| Redis | `localhost:6379` | `redis:6379` |
| Kafka | `localhost:9092` | `kafka:29092` |
| Zookeeper | — | `zookeeper:2181` |

- **MySQL database:** `content_db`
- **Kafka topics:** auto-creation is enabled, so topics are created on first use.
- **Persistence:** MySQL data is stored in the `mysql-data` Docker volume.

---

## ⚙️ Configuration

Services running on your machine (outside Docker) should connect using the `localhost` addresses above. Example:

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/content_db
spring.datasource.username=root
spring.datasource.password=root

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
```

<!-- TODO: Adjust property names if your services are not Spring Boot. -->

> ⚠️ The credentials in `docker-compose.yml` (`root` / `root`) are for **local development only**. Use environment variables or a secrets manager for any real deployment.

---

## 🗺 Roadmap

- [ ] Containerize each service and add it to `docker-compose.yml`
- [ ] API gateway / single entry point
- [ ] User authentication and watch history
- [ ] Adaptive bitrate streaming (HLS / DASH)
- [ ] Frontend client

<!-- TODO: Keep only what you actually plan to build. -->

---

## 👤 Author

**Shailendra Kumar Sahu**

- GitHub: [@Eng-Shailendra](https://github.com/Eng-Shailendra)

---

⭐ If you find this project useful, consider giving it a star!
