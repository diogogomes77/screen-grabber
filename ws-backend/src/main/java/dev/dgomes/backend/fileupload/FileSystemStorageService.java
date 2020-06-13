package dev.dgomes.backend.fileupload;

import io.humble.video.Muxer;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
	private final HashSet options;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
		options = new HashSet();
		options.add(StandardOpenOption.CREATE);
		options.add(StandardOpenOption.APPEND);
	}

	@Override
	public void storeByteBuffer(ByteBuffer payload, String filename ) {
		try {
			SeekableByteChannel byteChannel = Files.newByteChannel(
					this.rootLocation.resolve(filename),
					options
			);
			byteChannel.write(payload);
			//System.out.println("stored");
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}
	}

	@Override
	public int getNumberFilesInDir() {
		File file = new File(this.rootLocation.toString());
		return file.list().length;
	}

	@Override
	public boolean fileExists(String filename) {
		return new File(this.rootLocation.toString(), filename).exists();
	}

	@Override
	public Muxer getMuxer(String filename){
		Muxer muxer = Muxer.make(this.rootLocation.resolve(filename).toString(), null, "mp4");
		return muxer;
	}

	@Override
	public void saveImage(BufferedImage screen, String filename) {
		File outputfile = new File(this.rootLocation.resolve(filename).toString());
		try {
			ImageIO.write(screen, "jpg", outputfile);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
