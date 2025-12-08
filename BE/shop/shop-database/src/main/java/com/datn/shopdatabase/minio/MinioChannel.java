package com.datn.shopdatabase.minio;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioChannel {

    private static final String BUCKET = "resources";
    private final MinioClient minioClient;

    @PostConstruct
    private void init() {
        createBucket(BUCKET);
    }

    @SneakyThrows
    private void createBucket(final String name) {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            log.info("Bucket '{}' created.", name);
        }
    }

    @SneakyThrows
    public String upload(@NonNull MultipartFile file) {
        String fileName = System.currentTimeMillis() + "-" + Objects.requireNonNull(file.getOriginalFilename());
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(BUCKET)
                        .object(fileName)
                        .build()
        );
    }

    public List<String> uploadMultiple(@NonNull List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                urls.add(upload(file));
            }
        }
        return urls;
    }
    @SneakyThrows
    public void delete(@NonNull String url) {
        // Tách fileName từ URL
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(fileName)
                        .build()
        );
    }

}
