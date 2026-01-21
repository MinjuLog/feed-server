package org.minjulog.feedserver.infra;

import org.minjulog.feedserver.application.presence.InMemoryPresenceStore;
import org.minjulog.feedserver.application.presence.PresenceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresenceConfig {

    @Bean
    public PresenceStore presenceStore() { return new InMemoryPresenceStore(); }
}
