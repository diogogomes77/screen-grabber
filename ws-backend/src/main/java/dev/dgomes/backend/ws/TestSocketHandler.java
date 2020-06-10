package dev.dgomes.backend.ws;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestSocketHandler extends TextWebSocketHandler {

    List<WebSocketSession> sessions = new CopyOnWriteArrayList();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {
        System.out.println("TextMessage= " + message);
        /*for(WebSocketSession webSocketSession : sessions) {
            String response = "ACK Received: "+ message.getPayload();
            webSocketSession.sendMessage(new TextMessage(response));
        }*/
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("TEST afterConnectionEstablished");
        sessions.add(session);
    }
}
