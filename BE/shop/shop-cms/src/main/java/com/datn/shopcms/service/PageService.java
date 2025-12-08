package com.datn.shopcms.service;

import com.datn.shopobject.dto.request.PageRequest;
import com.datn.shopobject.dto.response.PageResponse;
import com.datn.shopdatabase.entity.PageEntity;
import com.datn.shopdatabase.repository.PageRepository;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageService {
    private PageRepository pageRepository;

    public PageResponse create(PageRequest req) {
        if (pageRepository.findBySlug(req.slug()).isPresent())
            throw new AppException(ErrorCode.SLUG_EXISTED);

        PageEntity page = PageEntity.builder()
                .slug(req.slug())
                .title(req.title())
                .summary(req.summary())
                .content(req.content())
                .metaTitle(req.metaTitle())
                .metaDescription(req.metaDescription())
                .active(Boolean.TRUE.equals(req.active()))
                .build();

        PageEntity saved = pageRepository.save(page);
        return map(saved);
    }

    public PageResponse update(Long id, PageRequest req) {
        PageEntity page = pageRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        page.setTitle(req.title());
        page.setSummary(req.summary());
        page.setContent(req.content());
        page.setMetaTitle(req.metaTitle());
        page.setMetaDescription(req.metaDescription());
        page.setActive(Boolean.TRUE.equals(req.active()));
        pageRepository.save(page);
        return map(page);
    }

    public PageResponse getBySlug(String slug) {
        PageEntity page = pageRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return map(page);
    }

    private PageResponse map(PageEntity p) { return new PageResponse(p.getId(), p.getSlug(), p.getTitle(), p.getSummary(), p.getContent(), p.getMetaTitle(), p.getMetaDescription(), p.getActive(), p.getCreatedAt(), p.getUpdatedAt()); }
}
