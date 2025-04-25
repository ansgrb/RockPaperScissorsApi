const apiBaseUrl = 'https://rockpaperscissorsapi-production.up.railway.app';
let playerId = null;
let ws = null;

document.getElementById('joinButton').addEventListener('click', async () => {
    const playerName = document.getElementById('playerName').value;
    if (!playerName) {
        alert('Please enter a player name!');
        return;
    }

    try {
        const response = await fetch(`${apiBaseUrl}/join`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: '', name: playerName })
        });
        const data = await response.json();
        playerId = data.id;
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
    ws = new WebSocket(`wss://rockpaperscissorsapi-production.up.railway.app/game?playerId=${playerId}`);
    ws.onopen = () => console.log('WebSocket connected');
    ws.onmessage = (event) => {
        const result = JSON.parse(event.data);
        document.getElementById('gameResult').textContent = `Result: ${result.player1.name} played ${result.player1Move}, ${result.player2.name} played ${result.player2Move}. Winner: ${result.winner === 'tie' ? 'Tie' : result.winner === playerId ? 'You' : 'Opponent'}`;
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