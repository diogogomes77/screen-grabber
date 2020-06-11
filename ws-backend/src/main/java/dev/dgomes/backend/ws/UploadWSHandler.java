package dev.dgomes.backend.ws;

import dev.dgomes.backend.fileupload.FileSystemStorageService;
import dev.dgomes.backend.fileupload.StorageProperties;
import dev.dgomes.backend.fileupload.StorageService;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class UploadWSHandler extends BinaryWebSocketHandler {

    private final StorageService storageService;
    //
    int i=0;
    String name = "cenas_";
    String filename;

    @Autowired
    public UploadWSHandler(StorageService storageService) {
        this.storageService = storageService;
        this.i = storageService.getNumberFilesInDir()-1;
        this.filename = name + Integer.toString(i) + ".webm";
    }

    List<WebSocketSession> sessions = new CopyOnWriteArrayList();

    private void setFilename(){

        while(storageService.fileExists(filename)){
            i++;
            filename = name + Integer.toString(i) + ".webm";
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();
        storageService.storeByteBuffer(payload,filename);
        String response = "Upload Chunk: size "+ payload.array().length;
        if (message.isLast()) {
            System.out.print(" .");
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Binary afterConnectionEstablished");
        sessions.add(session);
        this.setFilename();
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Binary afterConnectionClosed");
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

}

