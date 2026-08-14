import { Client } from '@stomp/stompjs'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const WS_URL = API_BASE_URL.replace(/^http/, 'ws') + '/ws'

// Create and activate a STOMP client on native WebSocket (without SockJS)
export function createStompClient(onConnect) {
    const client = new Client({
        brokerURL: WS_URL,
        reconnectDelay: 5000,
        onConnect
    })
    client.activate()
    return client
}