package dev.dgomes.backend.ws;

import dev.dgomes.backend.fileupload.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class UploadWSPicturesHandler extends BinaryWebSocketHandler {

    private final StorageService storageService;
    //
    int i=0;
    String name = "picture_";
    String filename;

    @Autowired
    public UploadWSPicturesHandler(StorageService storageService) {
        this.storageService = storageService;
        this.i = storageService.getNumberFilesInDir()-1;
        this.filename = name + Integer.toString(i) + ".png";
    }

    List<WebSocketSession> sessions = new CopyOnWriteArrayList();

    private void setFilename(){

        while(storageService.fileExists(filename)){
            i++;
            filename = name + Integer.toString(i) + ".png";
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();
        storageService.storeByteBuffer(payload,filename);
        String response = "Upload Chunk: size "+ payload.array().length;
        System.out.print(response);
        if (message.isLast()) {
            System.out.println("picture created: " + filename);
            this.setFilename();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Picture afterConnectionEstablished");
        sessions.add(session);

    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Picture afterConnectionClosed");
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

}

