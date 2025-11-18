package com.datn.shopcms.controller;

import com.datn.shopcms.dto.request.PageRequest;
import com.datn.shopcms.dto.response.PageResponse;
import com.datn.shopcms.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cms/pages")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    @GetMapping("/slug/{slug}")
    public PageResponse getBySlug(@PathVariable("slug") String slug) { return pageService.getBySlug(slug); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public PageResponse create(@RequestBody PageRequest req) { return pageService.create(req); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    public PageResponse update(@PathVariable("id") Long id, @RequestBody PageRequest req) { return pageService.update(id, req); }
}

