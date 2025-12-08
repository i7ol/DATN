//package com.datn.shopcms.controller;
//
//
//import com.datn.shopcms.dto.request.BannerRequest;
//import com.datn.shopcms.dto.response.BannerResponse;
//import com.datn.shopcms.service.BannerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/cms/banners")
//@RequiredArgsConstructor
//public class BannerController {
//
//    private final BannerService bannerService;
//
//    // Lấy tất cả banner theo position
//    @GetMapping("/position/{position}")
//    public List<BannerResponse> getByPosition(@PathVariable("position") String position) {
//        return bannerService.getByPosition(position);
//    }
//
//    // ADMIN endpoints
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin")
//    public BannerResponse create(@RequestBody BannerRequest req) {
//        return bannerService.create(req);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping("/admin/{id}")
//    public BannerResponse update(@PathVariable("id") Long id, @RequestBody BannerRequest req) {
//        return bannerService.update(id, req);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/admin/{id}")
//    public void delete(@PathVariable("id") Long id) {
//        bannerService.delete(id);
//    }
//}
