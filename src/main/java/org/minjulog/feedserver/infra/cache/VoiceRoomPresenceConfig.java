package org.minjulog.feedserver.infra.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VoiceRoomPresenceConfig {

    @Bean
    public VoiceRoomPresenceStore voiceRoomPresenceStore() {
        return new CaffeineVoiceRoomPresenceStore();
    }
}
