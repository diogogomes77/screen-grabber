package dev.dgomes.backend.ws;

import dev.dgomes.backend.fileupload.StorageService;
import dev.dgomes.backend.mjpeg.VideoRecorder;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class UploadWSMjpegHandler extends BinaryWebSocketHandler {

    private final StorageService storageService;
    private VideoRecorder recorder;
    //
    int i=0;
    String name = "picture_";
    String filename;
    SeekableInMemoryByteChannel byteChannel;

    @Autowired
    public UploadWSMjpegHandler(StorageService storageService) {
        this.storageService = storageService;
        this.i = storageService.getNumberFilesInDir()-1;
        this.filename = name + Integer.toString(i) + ".mp4";
    }

    List<WebSocketSession> sessions = new CopyOnWriteArrayList();

    private void setFilename(){

        while(storageService.fileExists(filename)){
            i++;
            filename = name + Integer.toString(i) + ".mp4";
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();
        //storageService.storeByteBuffer(payload,filename);
        byteChannel.write(payload);
        String response = " Chunk: "+ payload.array().length;
        System.out.print(response);
        if (message.isLast()) {
            recorder.addPicture(byteChannel);
            //this.setFilename();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Picture afterConnectionEstablished");
        sessions.add(session);
        this.setFilename();
        recorder = new VideoRecorder(filename,storageService,6);// TODO get frame rate
        byteChannel = new SeekableInMemoryByteChannel();


    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Picture afterConnectionClosed");
        sessions.remove(session);
        recorder.finishVideo();
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

}

