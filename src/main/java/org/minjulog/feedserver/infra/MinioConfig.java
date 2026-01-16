package org.minjulog.feedserver.infra;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${env.MINIO.END_POINT}")
    private String MINIO_END_POINT;

    @Value("${env.MINIO.ACCESS_KEY}")
    private String MINIO_ACCESS_KEY;

    @Value("${env.MINIO.SECRET_KEY}")
    private String MINIO_SECRET_KEY;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(MINIO_END_POINT)
                .credentials(MINIO_ACCESS_KEY, MINIO_SECRET_KEY)
                .build();
    }
}
