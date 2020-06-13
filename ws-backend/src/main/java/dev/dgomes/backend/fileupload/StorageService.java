package dev.dgomes.backend.fileupload;

import io.humble.video.Muxer;
import org.springframework.core.io.Resource;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void storeByteBuffer(ByteBuffer payload, String name);
    int getNumberFilesInDir();
    boolean fileExists(String filename);

    //void inMemoryStoreByteBuffer(ByteBuffer payload, String name);
    public Muxer getMuxer(String filename);


    void saveImage(BufferedImage screen, String filename);
}