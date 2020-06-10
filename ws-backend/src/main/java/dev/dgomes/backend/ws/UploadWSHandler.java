package dev.dgomes.backend.ws;

import dev.dgomes.backend.fileupload.FileSystemStorageService;
import dev.dgomes.backend.fileupload.StorageProperties;
import dev.dgomes.backend.fileupload.StorageService;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UploadWSHandler extends BinaryWebSocketHandler {

    private final StorageService storageService;
    int i=0;
    String fileName = "cenas_";

    @Autowired
    public UploadWSHandler(StorageService storageService) {

        this.storageService = storageService;
    }

    //Map<WebSocketSession,> sessionToFileMap = new WeakHashMap<>();
    //ServletFileUpload upload = new ServletFileUpload();

    List<WebSocketSession> sessions = new CopyOnWriteArrayList();

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        //System.out.println("BinaryMessage= ");
        System.out.println(message);
        ByteBuffer payload = message.getPayload();
        i++;
        String name = fileName + Integer.toString(i);
        storageService.storeByteBuffer(payload,fileName);
        //System.out.println(name);
//        FileChannel channel =  new FileOutputStream(new File("file.png"), false).getChannel();
//        channel.write(payload);
//        channel.close();
        String response = "Upload Chunk: size "+ payload.array().length;
        System.out.println(response);

        if (message.isLast()) {
            System.out.println("end of upload");
        }


    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Binary afterConnectionEstablished");
        sessions.add(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    //    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message)  {
//        System.out.println("FILE RECEIVING "+message.getPayload());
//        try {
//            session.sendMessage(new TextMessage("FILE RECEIVING "+message.getPayload()));
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
////        for(WebSocketSession webSocketSession : sessions) {
////            try {
////                webSocketSession.sendMessage(new TextMessage("Received " + message.getPayload()));
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
//    }



}

