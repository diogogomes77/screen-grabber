package dev.dgomes.backend;

import dev.dgomes.backend.fileupload.StorageProperties;
import dev.dgomes.backend.fileupload.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({
		StorageProperties.class
})
public class BackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(BackendApplication.class, args);
		System.out.println("SCREEN GRABBER by Diogo Gomes 2020");
	}

}
