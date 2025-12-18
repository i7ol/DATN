package com.datn.shopdatabase.storage;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String ROOT_DIR = "E:/DATN/BE/shop/uploads/";
    private static final String PRODUCT_DIR = "products/";

    @Override
    public String store(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(ROOT_DIR + PRODUCT_DIR);
            Files.createDirectories(uploadPath);

            // Lấy tên file và extension
            String originalName = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = getExtension(originalName);

            // Tạo tên file mới với UUID
            String fileName = UUID.randomUUID() + ext;

            // Lưu file
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Trả về URL để truy cập từ web
            return "http://localhost:8081/uploads/products/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Cannot store file", e);
        }
    }

    @Override
    public void delete(String url) {
        try {
            if (url == null) return;

            // Trích xuất tên file từ URL
            String fileName = Paths.get(url).getFileName().toString();
            Path filePath = Paths.get(ROOT_DIR + PRODUCT_DIR + fileName);

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new RuntimeException("Cannot delete file", e);
        }
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf(".");
        return dot > 0 ? fileName.substring(dot) : "";
    }
}