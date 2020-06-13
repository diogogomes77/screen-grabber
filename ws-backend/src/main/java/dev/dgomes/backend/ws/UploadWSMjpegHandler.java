package dev.dgomes.backend.ws;

import dev.dgomes.backend.fileupload.StorageService;
import dev.dgomes.backend.mjpeg.HumbleExporter;
import dev.dgomes.backend.mjpeg.VideoRecorder;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class UploadWSMjpegHandler extends BinaryWebSocketHandler {

    private final StorageService storageService;
    //private VideoRecorder recorder;
    private HumbleExporter recorder;
    //
    int i=0;
    String name = "video_";
    String filename;
    SeekableInMemoryByteChannel byteChannel;
    int ii=0;
    private boolean firstImamge = true;

    @Autowired
    public UploadWSMjpegHandler(StorageService storageService) {
        this.storageService = storageService;
        this.i = storageService.getNumberFilesInDir()-1;
        this.filename = name + Integer.toString(i) + ".mp4";
        recorder = new HumbleExporter(filename,storageService,6);// TODO get frame rate
    }

    List<WebSocketSession> sessions = new CopyOnWriteArrayList();

    private void setFilename(){

        while(storageService.fileExists(filename)){
            i++;
            filename = name + Integer.toString(i) + ".mp4";
        }
    }

    private BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();
        //storageService.storeByteBuffer(payload,filename);
        byteChannel.write(payload);
        String response = " Chunk: "+ payload.array().length;
        //System.out.print(".");
        System.out.print(response);
        if (message.isLast()) {
            System.out.println(" last total size= " + byteChannel.size());

            BufferedImage screen = createImageFromBytes(byteChannel.array());
            String imageFilename = "image_" + Integer.toString(ii) + ".jpg";
            //storageService.saveImage(screen, imageFilename);
            ii++;
            //recorder.addPicture(screen);
            if (firstImamge){
                recorder.open(screen.getWidth(), screen.getHeight());
                firstImamge =false;
            }
            recorder.encode(screen);
            byteChannel = new SeekableInMemoryByteChannel();
            //this.setFilename();
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Mjpeg afterConnectionEstablished");
        sessions.add(session);
        this.setFilename();

        byteChannel = new SeekableInMemoryByteChannel();
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Mjpeg afterConnectionClosed");
        sessions.remove(session);
        recorder.close();
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

}

