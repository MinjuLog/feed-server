package org.minjulog.feedserver.infrastructure.configuration;

import org.minjulog.feedserver.infrastructure.cache.InMemoryVoiceRoomPresenceStore;
import org.minjulog.feedserver.infrastructure.cache.InMemoryVoiceRoomTransportModeStore;
import org.minjulog.feedserver.infrastructure.cache.VoiceRoomPresenceStore;
import org.minjulog.feedserver.infrastructure.cache.VoiceRoomTransportModeStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VoiceRoomPresenceConfig {

    @Bean
    public VoiceRoomPresenceStore voiceRoomPresenceStore() {
        return new InMemoryVoiceRoomPresenceStore();
    }

    @Bean
    public VoiceRoomTransportModeStore voiceRoomTransportModeStore() {
        return new InMemoryVoiceRoomTransportModeStore();
    }
}
