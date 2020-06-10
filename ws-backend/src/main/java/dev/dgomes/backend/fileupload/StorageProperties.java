package dev.dgomes.backend.fileupload;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "upload")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	@Value("${upload.location}")
	private String location;

	public String getLocation() {
		return location;
	}

}
