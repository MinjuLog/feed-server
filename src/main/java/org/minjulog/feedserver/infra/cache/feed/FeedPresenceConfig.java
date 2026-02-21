package org.minjulog.feedserver.infra.cache.feed;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeedPresenceConfig {

    @Bean
    public FeedPresenceStore feedPresenceStore() {
        return new InMemoryFeedPresenceStore();
    }
}
