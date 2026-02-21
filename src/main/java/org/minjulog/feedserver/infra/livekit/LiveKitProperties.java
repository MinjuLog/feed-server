package org.minjulog.feedserver.infra.livekit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "env.livekit")
public class LiveKitProperties {

    private String apiKey;
    private String apiSecret;
}
