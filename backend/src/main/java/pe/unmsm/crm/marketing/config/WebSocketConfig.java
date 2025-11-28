package pe.unmsm.crm.marketing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket para actualizaciones en tiempo real
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Prefijo para mensajes enviados desde el servidor al cliente
        config.enableSimpleBroker("/topic");

        // Prefijo para mensajes enviados desde el cliente al servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Endpoint WebSocket con soporte SockJS para fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permite todos los orígenes en desarrollo
                .withSockJS();
    }
}
