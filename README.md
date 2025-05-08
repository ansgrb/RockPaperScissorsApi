# RockPaperScissorsApi 🎮🪨📄✂️

This project is an implementation of a multiplayer **Rock-Paper-Scissors** game API developed using [Ktor](https://ktor.io).

### Todo list:
* [x] Adding a database (MongoDB)
* [x] JWT auth to secure endpoints
* [x] Enhance features (game history, leaderboard)
* [x] Logging and error handling
* [ ] Scaling with Redis (cashing) and Railway (auto-scaling)
* [x] Security with rate-limiting and input validation
* [ ] Monitoring and UptimeRobot
* [ ] A very, very stretch goal for blockchain integration

---

## 🛠 Features included:

| Name                                                                   | Description                                                                      |
|------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                |
| [WebSockets](https://start.ktor.io/p/ktor-websockets)                  | Adds WebSocket protocol support for bidirectional client connections             |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Automatic content serialization and deserialization for JSON and more!           |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization conveniently                                          |
| [Authentication](https://start.ktor.io/p/authentication)               | Implements user authentication with JWT tokens                                    |
| [Authorization](https://start.ktor.io/p/authorization)                 | Controls access to protected resources based on user roles                        |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Handles errors and exceptions with custom responses                              |
| [CORS](https://start.ktor.io/p/cors)                                   | Enables Cross-Origin Resource Sharing for API endpoints                          |
| [Rate Limiting](https://start.ktor.io/p/rate-limit)                    | Limits the number of requests a client can make within a time period             |
| [MongoDB](https://www.mongodb.com/)                                    | NoSQL database for storing player data, game history, and leaderboard            |
| [Config YAML](https://start.ktor.io/p/yaml)                            | YAML-based configuration for flexible application settings                        |
| [Logging](https://ktor.io/docs/logging.html)                           | Comprehensive logging using Logback for monitoring and debugging                  |

---

## 🏗 How to Build & Run the Project:

| Task                          | Description                                                        |
|-------------------------------|--------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                      |
| `./gradlew build`             | Build everything                                                   |
| `./gradlew :server:buildFatJar` | Build an executable JAR for the server with dependencies included |
| `docker build -t rps-api .`   | Build a Docker image for the application                           |
| `docker-compose up -d`        | Start MongoDB database in Docker container                         |
| `./gradlew :server:run`       | Start the server locally                                           |
| `docker run -p 8080:8080 rps-api` | Run the application in Docker container                        |

### Docker Setup

The project includes Docker support for both the application and MongoDB:

1. **MongoDB Container**:
  - Run `docker-compose up -d` to start MongoDB
  - MongoDB runs on port 27017
  - Credentials are configured in the docker-compose.yml file

2. **Application Container**:
  - Uses a multi-stage build process
  - Build stage uses Gradle 8.5 with JDK 17
  - Runtime stage uses OpenJDK 17 slim image
  - Application runs on port 8080

You can run the full stack (MongoDB + Application) using:

```bash
# Start MongoDB
docker-compose up -d

# Build and run the application
docker build -t rps-api .
docker run --network host -p 8080:8080 rps-api
```

---

## API Endpoints with Use Cases

### 1️⃣ **POST /login**
- **Purpose**: Authenticate a player and generate a JWT token for secure access.
- **Request Body**:
  ```json
  {
    "id": "",
    "name": "Ali"
  }
  ```
- **Response**:
  ```json
  {
    "token": "jwt-token-string",
    "playerId": "unique-player-id",
    "name": "Ali"
  }
  ```
- **Use Cases**:
  - Authentication before joining the game. 🔐
  - Returns JWT token needed for authenticated endpoints. 🎫

---

### 2️⃣ **POST /join**
- **Purpose**: Allows a player to join the game. 
- **Authorization**: Requires JWT token in the Authorization header.
- **Request Body**:
  ```json
  {
    "id": "player-id",
    "name": "Ali"
  }
  ```
- **Response**:
  - **201 Created**:
    ```json
    {
      "id": "unique-player-id",
      "name": "Ali"
    }
    ```
    Player successfully joined. ✅
  - **409 Conflict**:
    ```
    Game is full
    ```
    No slots available for new players. 🚫

- **Use Cases**:
  - A new player wants to join the waiting lobby before the game starts. 🏁
  - Uses player ID and token from login for secure access. 🔑

---

### 3️⃣ **GET /players**
- **Purpose**: List all players currently in the game. 👥
- **Response Example**:
  ```json
  [
    {
      "id": "player1-id",
      "name": "Ali"
    },
    {
      "id": "player2-id",
      "name": "Alya"
    }
  ]
  ```
- **Use Cases**:
  - Players can view the list of participants currently available. 👀
  - Useful to verify if other players have joined before starting the game. ✅

---

### 4️⃣ **GET /leaderboard**
- **Purpose**: Retrieve the current game leaderboard showing player wins.
- **Response Example**:
  ```json
  [
    {
      "player": "Ali",
      "wins": 7
    },
    {
      "player": "Alya",
      "wins": 5
    },
    {
      "player": "Tie",
      "wins": 2
    }
  ]
  ```
- **Use Cases**:
  - Track player performance across multiple games. 📊
  - See who has the most wins in the current session. 🏆

---

### 5️⃣ **WebSocket /game**
- **Purpose**: Connect players to the game and perform real-time interactions. 💬
- **How It Works**:
  - Players establish a WebSocket connection.
  - Players send their moves (`ROCK`, `PAPER`, or `SCISSORS`) via the WebSocket along with their player ID.
  - Once both players send their moves, the server determines the result and broadcasts it to all clients.

- **Play Move Example (Client)**:
  ```json
  {
    "playerId": "unique-player-id",
    "move": "ROCK"
  }
  ```
- **Result Example (Server Response)**:
  ```json
  {
    "player1Id": "player1-id",
    "player2Id": "player2-id",
    "player1Move": "ROCK",
    "player2Move": "SCISSORS",
    "winner": "player1-id"
  }
  ```

- **Use Cases**:
  - Enables real-time game moves between two players. 🤝
  - Broadcasts game results to both clients. 🌟
  - Once the game is over, the session resets for a fresh match! 🔄

---

## 🚀 Use Case Scenarios

1. **Authentication & Joining a Game**
  - Players first authenticate to receive a JWT token, then join using this secure token.
  - The token ensures that only authorized players can interact with the game.

2. **Viewing Players**
  - Players or admins fetch the list of current participants to monitor the lobby.

3. **Tracking Performance** 
  - The leaderboard provides a competitive element, letting players see who's winning the most games.

4. **Real-time Game**
  - Leveraging WebSocket for real-time interactions makes the game dynamic and engaging.

5. **Broadcasting Game Results**
  - Players receive instant results and can restart for a new match once both have played.

---

## CLI Client 🚧 (Under Construction)

The project includes a command-line interface (CLI) client that interacts with the API, allowing players to:

- Login and authenticate
- Join games
- List current players
- Play the game through the command line via interactive prompts

### CLI Features & Libraries

The CLI is built using several excellent Kotlin libraries:

- **[Clikt](https://github.com/ajalt/clikt)** (v5.0.3) - Powerful, flexible, and intuitive command-line interface parser
- **[Mordant](https://github.com/ajalt/mordant)** (v3.0.2) - Colorful terminal text styling with ANSI colors and markdown support
- **[Ktor Client](https://ktor.io/docs/client.html)** - HTTP client for API communication
  - CIO Engine - Coroutine-based I/O client
  - Content Negotiation - JSON serialization/deserialization
  - WebSockets - Bidirectional real-time communication
  - Auth - JWT authentication

---
## Tech Stack

- **Kotlin** 2.1.10 - Main programming language
- **Ktor** 3.1.2 - Backend framework
- **MongoDB** 7 - Database for persistence
- **JWT** - Authentication mechanism
- **Docker** - Containerization
- **Gradle** 8.5 - Build tool
- **JDK** 17 - Java Development Kit
- **Logback** 1.4.14 - Logging framework


Enjoy playing Rock-Paper-Scissors with this API! 🎉🪨📄✂️