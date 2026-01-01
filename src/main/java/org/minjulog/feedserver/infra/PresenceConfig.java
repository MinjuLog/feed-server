package org.minjulog.feedserver.infra;

import org.minjulog.feedserver.application.InMemoryPresenceStore;
import org.minjulog.feedserver.application.PresenceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresenceConfig {

    @Bean
    public PresenceStore presenceStore() { return new InMemoryPresenceStore(); }
}
