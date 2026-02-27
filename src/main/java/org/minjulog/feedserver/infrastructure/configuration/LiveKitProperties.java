package org.minjulog.feedserver.infrastructure.configuration;

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
    private Hybrid hybrid = new Hybrid();

    @Getter
    @Setter
    public static class Hybrid {
        /**
         * auto | mesh | sfu
         */
        private String mode = "auto";
        private int switchToSfuAt = 3;
        private int switchToMeshAt = 2;
    }
}
