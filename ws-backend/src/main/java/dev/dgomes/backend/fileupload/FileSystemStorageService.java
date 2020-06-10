package dev.dgomes.backend.fileupload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path rootLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
				.filter(path -> !path.equals(this.rootLocation))
				.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);
			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void store_stream(InputStream fileStream, String name) {
		String filename = name;
		try {
			try (InputStream inputStream = fileStream) {
				Files.copy(inputStream, this.rootLocation.resolve(filename),
						StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}
	}

	@Override
	public void storeByteBuffer(ByteBuffer payload, String filename ) {
		System.out.println("storeByteBuffer");
		//String filename = "cenas";
		//ByteBufferBackedInputStream fileStream = new ByteBufferBackedInputStream(payload);
		Set options = new HashSet();
		options.add(StandardOpenOption.CREATE);
		options.add(StandardOpenOption.APPEND);
		try {
			//try (InputStream inputStream = fileStream) {
			SeekableByteChannel byteChannel = Files.newByteChannel(
					this.rootLocation.resolve(filename),
					options
			);
			byteChannel.write(payload);
			/*
			Files.write(
						this.rootLocation.resolve(filename),
						payload,
						StandardOpenOption.APPEND);
*/

				//Files.copy(inputStream, this.rootLocation.resolve(filename),
				//		StandardCopyOption.REPLACE_EXISTING);
				System.out.println("stored");
			//}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}
	}

}
