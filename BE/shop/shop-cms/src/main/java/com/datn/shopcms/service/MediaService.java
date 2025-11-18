package com.datn.shopcms.service;

import com.datn.shopcms.dto.request.MediaRequest;
import com.datn.shopcms.dto.response.MediaResponse;
import com.datn.shopcms.entity.Media;
import com.datn.shopcms.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;

    @Value("${app.media.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        if (!uploadDir.endsWith("/")) uploadDir += "/";
        File folder = new File(uploadDir);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể tạo thư mục upload: " + uploadDir);
        }
    }

    // Upload file
    public MediaResponse store(MultipartFile file, String fileName, Boolean active) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File upload trống hoặc null");
        }

        String originalName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = UUID.randomUUID() + "_" + originalName;
        } else {
            fileName = UUID.randomUUID() + "_" + fileName;
        }

        String filePath = uploadDir + fileName;

        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể lưu file '" + originalName + "' vào '" + filePath + "': " + e.getMessage());
        }

        Media media = Media.builder()
                .fileName(fileName)
                .url("/media/" + fileName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .active(active != null ? active : true)
                .build();

        try {
            return map(mediaRepository.save(media));
        } catch (Exception e) {
            new File(filePath).delete();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Upload file thành công nhưng lưu vào database thất bại: " + e.getMessage());
        }
    }

    // CRUD bằng JSON request
    public MediaResponse create(MediaRequest req) {
        try {
            Media media = Media.builder()
                    .fileName(req.fileName())
                    .url(req.url())
                    .contentType(req.contentType())
                    .size(req.size())
                    .active(req.active() != null ? req.active() : true)
                    .build();
            return map(mediaRepository.save(media));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tạo media thất bại: " + e.getMessage());
        }
    }

    public MediaResponse update(Long id, MediaRequest req) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Media không tồn tại với id=" + id));

        // Map dữ liệu từ request sang entity
        if (req.fileName() != null) media.setFileName(req.fileName());
        if (req.url() != null) media.setUrl(req.url());
        if (req.contentType() != null) media.setContentType(req.contentType());
        if (req.size() != null) media.setSize(req.size());
        if (req.active() != null) media.setActive(req.active());

        try {
            return map(mediaRepository.save(media));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cập nhật media id=" + id + " thất bại: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy media với id=" + id));
        try {
            mediaRepository.delete(media);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Xóa media id=" + id + " thất bại: " + e.getMessage());
        }
    }

    public List<MediaResponse> getAll() {
        try {
            return mediaRepository.findAll().stream()
                    .map(this::map)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lấy danh sách media thất bại: " + e.getMessage());
        }
    }

    private MediaResponse map(Media m) {
        return new MediaResponse(
                m.getId(),
                m.getFileName(),
                m.getUrl(),
                m.getContentType(),
                m.getSize(),
                m.getActive(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
