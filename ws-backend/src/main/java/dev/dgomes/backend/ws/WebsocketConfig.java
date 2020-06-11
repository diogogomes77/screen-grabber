package dev.dgomes.backend.ws;

import dev.dgomes.backend.fileupload.StorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebsocketConfig extends AbstractWebSocketMessageBrokerConfigurer implements WebSocketConfigurer {

    private final StorageService storageService;

    public WebsocketConfig(StorageService storageService) {
        this.storageService = storageService;
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new UploadWSHandler(storageService), "/binary")
                .setAllowedOrigins("*");
               // .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(50 * 1024 * 1024);
    }
}
