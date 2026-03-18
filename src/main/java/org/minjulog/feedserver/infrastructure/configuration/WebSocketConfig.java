package org.minjulog.feedserver.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.infrastructure.interceptor.StompConnectInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String SIMPLE_BROKER = "simple";

    private final StompConnectInterceptor stompConnectInterceptor;

    @Value("${stomp.broker-type:simple}")
    private String brokerType;

    @Value("${stomp.relay.host:localhost}")
    private String relayHost;

    @Value("${stomp.relay.port:61613}")
    private Integer relayPort;

    @Value("${stomp.relay.client-login:guest}")
    private String clientLogin;

    @Value("${stomp.relay.client-passcode:guest}")
    private String clientPasscode;

    @Value("${stomp.relay.system-login:guest}")
    private String systemLogin;

    @Value("${stomp.relay.system-passcode:guest}")
    private String systemPasscode;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        if (SIMPLE_BROKER.equalsIgnoreCase(brokerType)) {
            registry.enableSimpleBroker("/topic", "/queue");
            return;
        }

        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin(clientLogin)
                .setClientPasscode(clientPasscode)
                .setSystemLogin(systemLogin)
                .setSystemPasscode(systemPasscode);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompConnectInterceptor);
    }
}
