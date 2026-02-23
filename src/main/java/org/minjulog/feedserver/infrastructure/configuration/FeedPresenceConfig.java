package org.minjulog.feedserver.infrastructure.configuration;

import org.minjulog.feedserver.infrastructure.cache.FeedPresenceStore;
import org.minjulog.feedserver.infrastructure.cache.InMemoryFeedPresenceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeedPresenceConfig {

    @Bean
    public FeedPresenceStore feedPresenceStore() {
        return new InMemoryFeedPresenceStore();
    }
}
