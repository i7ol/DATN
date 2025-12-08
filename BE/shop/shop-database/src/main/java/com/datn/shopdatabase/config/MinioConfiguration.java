package com.datn.shopdatabase.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfiguration {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000") // Đúng port bạn mở trong docker
                .credentials("admin", "admin123")   // root user + password trong docker-compose
                .build();
    }
}
