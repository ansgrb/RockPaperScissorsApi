# 🎮 RockPaperScissorsApi 🪨📄✂️
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ansgrb/RockPaperScissorsApi)

A **multiplayer Rock–Paper–Scissors API** crafted with [Ktor](https://ktor.io), offering JWT authentication, MongoDB persistence, live WebSocket play, leaderboard tracking, and more!

---
## ✨ Features

|  | **Name**                                                                  | **Description**                                                                           |
|--|---------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| 🛣️ | [Routing](https://start.ktor.io/p/routing)                             | Structured routing DSL for endpoint organization                                           |
| 📡 | [WebSockets](https://start.ktor.io/p/ktor-websockets)                  | Real-time, bidirectional connections for live play                                         |
| 🎛️ | [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Automatic (de)serialization (JSON and beyond!)                                             |
| 📦 | [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Native, seamless JSON serialization/deserialization                                        |
| 🔐 | [Authentication](https://start.ktor.io/p/authentication)               | JWT-based endpoint security                                                                |
| 🔏 | [Authorization](https://start.ktor.io/p/authorization)                 | User role-based access protection                                                          |
| ❗ | [Status Pages](https://start.ktor.io/p/status-pages)                   | Custom error/exception handling                                                            |
| 🌐 | [CORS](https://start.ktor.io/p/cors)                                   | Cross-Origin Resource Sharing for enabled endpoints                                        |
| 🚦 | [Rate Limiting](https://start.ktor.io/p/rate-limit)                    | Request-throttling to fend off abuse                                                       |
| 🗄️ | [MongoDB](https://www.mongodb.com/)                                    | Stores player info, history, and leaderboard                                               |
| ⚙️ | [Config YAML](https://start.ktor.io/p/yaml)                            | Flexible app configuration via simple YAML                                                 |
| 📝 | [Logging (Logback)](https://ktor.io/docs/logging.html)                 | Deep insight and troubleshooting with advanced logs                                        |

---

## 📋 To-Do List

Here’s what’s next for RockPaperScissorsApi — PRs & suggestions **welcome**!

- [x] **Adding a database (MongoDB)**
- [x] **JWT auth to secure endpoints**
- [x] **Enhance features (game history, leaderboard)**
- [x] **Logging and error handling**
- [ ] **Scaling with Redis (cashing) and Railway (auto-scaling)**
- [x] **Security with rate-limiting and input validation**
- [ ] **Monitoring**
- [ ] **Add user registration endpoint**
- [ ] **Enhanced game session management**  
  _Support simultaneous games & reconnection logic_
- [ ] **Retry/timeout logic for dropped WebSocket clients**
- [ ] **Admin endpoints for resetting leaderboard/games**
- [ ] **In-memory fallback for non-persistent testing**
- [ ] **API documentation (OpenAPI/Swagger) generation**
- [ ] **Rate limit per user (not only per IP)**
- [ ] **Unit and integration test improvements**
- [ ] **CLI client auto-completion & history**
- [ ] **Front-end client**  
  _nice-to-have!_
- [ ] **Recording the results on blockchain**  
  _A very, very stretch goal_
- [ ] _(Add your feature here — contributions encouraged!)_

---

## 🛠️ Getting Started

### ⚡ Build, Test & Run

| **Command**                                      | **Action**                                         |
|--------------------------------------------------|-----------------------------------------------------|
| `./gradlew test`                                 | Run unit tests                                      |
| `./gradlew build`                                | Build everything                                    |
| `./gradlew :server:buildFatJar`                  | Build executable JAR (with dependencies)            |
| `docker build -t rps-api .`                      | Build Docker image                                  |
| `docker-compose up -d`                           | Start MongoDB in Docker                             |
| `./gradlew :server:run`                          | Run server locally                                  |
| `docker run -p 8080:8080 rps-api`                | Run the app in Docker                               |


### 🐳 Docker Setup

- **MongoDB Container**
  - `docker-compose up -d` (port **27017**)
  - Credentials in `docker-compose.yml`
- **Application Container**
  - Multi-stage build (Gradle 8.5 + JDK 17, OpenJDK 17 slim)
  - Runs on port **8080**

---

## 📖 API Reference

### 1️⃣ `POST /login`
**Authenticate player, get JWT**

- **Request:**
  ```json
  { "id": "", "name": "Ali" }
  ```
- **Response:**
  ```json
  { "token": "jwt-string", "playerId": "unique-id", "name": "Ali" }
  ```
- <sup>🔑 Use the returned token for subsequent endpoints.</sup>

---

### 2️⃣ `POST /join`
**Join a new game (JWT required)**

- **Headers:** `Authorization: Bearer <jwt>`
- **Request:**
  ```json
  { "id": "player-id", "name": "Ali" }
  ```
- **Responses:**
  - `201 Created`
    ```json
    { "id": "unique-player-id", "name": "Ali" }
    ```
  - `409 Conflict`
    ```
    Game is full
    ```

---

### 3️⃣ `GET /players`
**See all joined players**

- **Response:**
  ```json
  [
    { "id": "player1-id", "name": "Ali" },
    { "id": "player2-id", "name": "Alya" }
  ]
  ```

---

### 4️⃣ `GET /leaderboard`
**Who’s winning? View wins per player**

- **Response:**
  ```json
  [
    { "player": "Ali", "wins": 7 },
    { "player": "Alya", "wins": 5 },
    { "player": "Tie", "wins": 2 }
  ]
  ```

---

### 5️⃣ **WebSocket `/game`**
**Real-time RPS gameplay**

- **Send (client):**
  ```json
  { "playerId": "unique-player-id", "move": "ROCK" }
  ```
- **Receive (result):**
  ```json
  {
    "player1Id": "player1-id",
    "player2Id": "player2-id",
    "player1Move": "ROCK",
    "player2Move": "SCISSORS",
    "winner": "player1-id"
  }
  ```
- <sup>🔄 Play, see instant results, and rematch!</sup>

---

## 🎯 Use Case Scenarios

- **Authenticate & Join:** Log in, receive a JWT, and join a game securely.
- **Discover:** View current lobby players.
- **Track:** Leaderboard keeps score across games.
- **Play Live:** Send moves, get live results.
- **Rematch Fast:** Fresh match automatically starts after a game ends.

---

## CLI Client (Under Construction) 🚧

The CLI lets you:

- Authenticate, join games
- List players, play interactively

**Tech:**
- [Clikt](https://github.com/ajalt/clikt) *(v5.0.3)* — CLI parser
- [Mordant](https://github.com/ajalt/mordant) *(v3.0.2)* — Terminal colors/markdown
- [Ktor Client](https://ktor.io/docs/client.html) — HTTP/WS API communication

---

## 🧰 Tech Stack

- **Kotlin** `2.1.10`
- **Ktor** `3.1.2`
- **MongoDB** `7`
- **JWT** Authentication
- **Docker & Docker Compose**
- **Gradle** `8.5`
- **JDK** `17`
- **Logback** `1.4.14`

---

> 🥳 **Enjoy Rock-Paper-Scissors powered by Kotlin!**  
> _Questions, feedback & PRs welcome!_