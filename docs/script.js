let token = null;
let playerId = null;
const apiBaseUrl = 'https://rockpaperscissorsapi-production.up.railway.app';

document.getElementById('joinButton').addEventListener('click', async () => {
    const playerName = document.getElementById('playerName').value;
    if (!playerName) {
        alert('Please enter a player name!');
        return;
    }

    try {
        // First, log in to get a JWT token
        const loginResponse = await fetch(`${apiBaseUrl}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: '', name: playerName })
        });
        const loginData = await loginResponse.json();
        token = loginData.token;
        playerId = loginData.playerId;

        // Then, join the game with the token
        const joinResponse = await fetch(`${apiBaseUrl}/join`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ id: playerId, name: playerName })
        });
        const joinData = await joinResponse.json();
        document.getElementById('playerIdValue').textContent = playerId;
        document.getElementById('playerId').classList.remove('hidden');
        document.getElementById('gameSection').classList.remove('hidden');
        fetchPlayers();
        connectWebSocket();
    } catch (error) {
        console.error('Error joining game:', error);
        alert('Failed to join game.');
    }
});

async function fetchPlayers() {
    try {
        const response = await fetch(`${apiBaseUrl}/players`);
        const players = await response.json();
        document.getElementById('playersListValue').textContent = players.map(p => p.name).join(', ');
        document.getElementById('playersList').classList.remove('hidden');
    } catch (error) {
        console.error('Error fetching players:', error);
    }
}

function connectWebSocket() {
    // Note: WebSocket headers for auth might not work in all browsers
    // You may need to pass the token as a query param or use a different approach
    ws = new WebSocket(`wss://rockpaperscissorsapi-production.up.railway.app/game`);
    ws.onopen = () => console.log('WebSocket connected');
    ws.onmessage = (event) => {
        const result = JSON.parse(event.data);
        document.getElementById('gameResult').textContent = `Result: ${result.player1Id} played ${result.player1Move}, ${result.player2Id} played ${result.player2Move}. Winner: ${result.winner === 'tie' ? 'Tie' : result.winner === playerId ? 'You' : 'Opponent'}`;
        ws.close();
    };
    ws.onerror = (error) => console.error('WebSocket error:', error);
}

function sendMove(move) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ playerId, move }));
    } else {
        alert('WebSocket not connected. Please join the game again.');
    }
}

document.getElementById('rockButton').addEventListener('click', () => sendMove('ROCK'));
document.getElementById('paperButton').addEventListener('click', () => sendMove('PAPER'));
document.getElementById('scissorsButton').addEventListener('click', () => sendMove('SCISSORS'));