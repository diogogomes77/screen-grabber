package dev.dgomes.backend.fileupload;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void storeByteBuffer(ByteBuffer payload, String name);
    int getNumberFilesInDir();
    boolean fileExists(String filename);




}