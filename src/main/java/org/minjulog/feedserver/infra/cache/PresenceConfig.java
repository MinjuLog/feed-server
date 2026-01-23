package org.minjulog.feedserver.infra.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresenceConfig {

    @Bean
    public PresenceStore presenceStore() { return new InMemoryPresenceStore(); }
}
