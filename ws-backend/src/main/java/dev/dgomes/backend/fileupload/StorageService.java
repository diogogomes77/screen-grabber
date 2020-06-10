package dev.dgomes.backend.fileupload;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void store_stream(InputStream fileStream, String name);

    void storeByteBuffer(ByteBuffer payload, String name);

}