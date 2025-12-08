//package com.datn.shopcms.controller;
//
//import com.datn.shopobject.dto.request.MediaRequest;
//import com.datn.shopobject.dto.response.MediaResponse;
//import com.datn.shopcms.service.MediaService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/cms/media")
//@RequiredArgsConstructor
//public class MediaController {
//
//    private final MediaService mediaService;
//
//    // Lấy tất cả media
//    @GetMapping("/all")
//    public List<MediaResponse> getAll() {
//        return mediaService.getAll();
//    }
//
//    // Tạo media bằng JSON (ADMIN)
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin")
//    public MediaResponse create(@RequestBody MediaRequest req) {
//        return mediaService.create(req);
//    }
//
//    // Cập nhật media bằng JSON (ADMIN)
//    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping("/admin/{id}")
//    public MediaResponse update(@PathVariable("id") Long id, @RequestBody MediaRequest req) {
//        return mediaService.update(id, req);
//    }
//
//    // Xóa media (ADMIN)
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/admin/{id}")
//    public void delete(@PathVariable("id") Long id) {
//        mediaService.delete(id);
//    }
//
//    // Upload file (ADMIN)
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin/upload")
//    public MediaResponse upload(@RequestParam("file") MultipartFile file,
//                                @RequestParam(value = "fileName", required = false) String fileName,
//                                @RequestParam(value = "active", required = false, defaultValue = "true") Boolean active) {
//        return mediaService.store(file, fileName, active);
//    }
//}
