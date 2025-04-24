# RockPaperScissorsApi 🎮🪨📄✂️

This project is an implementation of a multiplayer **Rock-Paper-Scissors** game API developed using [Ktor](https://ktor.io).

## 🛠 Features included:

| Name                                                                   | Description                                                                      |
|------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                |
| [WebSockets](https://start.ktor.io/p/ktor-websockets)                  | Adds WebSocket protocol support for bidirectional client connections             |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Automatic content serialization and deserialization for JSON and more!           |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization conveniently                                          |

---

## 🏗 How to Build & Run the Project:

| Task                          | Description                                                        |
|-------------------------------|--------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                      |
| `./gradlew build`             | Build everything                                                   |
| `buildFatJar`                 | Build an executable JAR for your server with dependencies included |
| `buildImage`                  | Build a Docker image to deploy your app                            |
| `publishImageToLocalRegistry` | Publish Docker image locally                                       |
| `run`                         | Start the server                                                   |
| `runDocker`                   | Start via Docker container                                         |

Once the server is running, you'll see:
---

## API Endpoints with Use Cases

### 1️⃣ **POST /join**
- **Purpose**: Allows a player to join the game. 
- **Request Body**:
  ```json
  {
    "id": null,
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
  - Returns a unique ID for each new player for later interactions. 🔑

---

### 2️⃣ **GET /players**
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

### 3️⃣ **WebSocket /game**
- **Purpose**: Connect players to the game and perform real-time interactions. 💬
- **How It Works**:
  - Each player connects using their assigned `playerId` parameter.
  - Players send their moves (`ROCK`, `PAPER`, or `SCISSORS`) via the WebSocket.
  - Once both players send their moves, the server determines the result and broadcasts it to all clients.

- **Initial Request Example (Client Connects)**:
  ```
  ws://0.0.0.0:8080/game?playerId=player1-id
  ```
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
    "player1": {
      "id": "player1-id",
      "name": "Player 1"
    },
    "player2": {
      "id": "player2-id",
      "name": "Player 2"
    },
    "player1Move": "ROCK",
    "player2Move": "SCISSORS",
    "winner": "Player 1"
  }
  ```

- **Use Cases**:
  - Enables real-time game moves between two players. 🤝
  - Broadcasts game results to both clients. 🌟
  - Once the game is over, the session resets for a fresh match! 🔄

---

## 🚀 Use Case Scenarios

1. **Joining a Game**
  - Players join and are assigned an ID to be managed by the server. Useful for identifying players in subsequent actions. 

2. **Viewing Players**
  - Players or admins fetch the list of current participants to monitor the lobby. 

3. **Real-time Game**
  - Leveraging WebSocket for real-time interactions makes the game dynamic and engaging. 

4. **Broadcasting Game Results**
  - Players receive instant results and can restart for a new match once both have played. 

---

Enjoy playing Rock-Paper-Scissors with this API! 🎉🪨📄✂️