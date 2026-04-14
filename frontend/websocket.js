import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

export class WebSocketService {
    constructor() {
        this.client = null;
        this.onIdeaReceivedCallback = null;
    }

    connect(onConnectedCallback) {
        // Connect to Spring Boot backend
        const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8085';
        const socket = new SockJS(`${wsBaseUrl}/ws`);
        
        this.client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.onConnect = (frame) => {
            console.log('Connected: ' + frame);
            
            // Subscribe to the ideas topic
            this.client.subscribe('/topic/ideas', (message) => {
                if (message.body) {
                    const idea = JSON.parse(message.body);
                    if (this.onIdeaReceivedCallback) {
                        this.onIdeaReceivedCallback(idea);
                    }
                }
            });
            
            if (onConnectedCallback) onConnectedCallback();
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        };

        this.client.activate();
    }

    sendIdea(title, description, author) {
        if (this.client && this.client.connected) {
            this.client.publish({
                destination: '/app/newIdea',
                body: JSON.stringify({ 
                    title: title + ' (by ' + author + ')', 
                    description: description 
                })
            });
        } else {
            console.warn("WebSocket not connected!");
            alert("Connection lost. Real-time features disabled.");
        }
    }

    onIdeaReceived(callback) {
        this.onIdeaReceivedCallback = callback;
    }
}
