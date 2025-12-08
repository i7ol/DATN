package com.datn.shopcms.service;

import com.datn.shopdatabase.entity.BannerEntity;
import com.datn.shopobject.dto.request.BannerRequest;
import com.datn.shopobject.dto.response.BannerResponse;
import com.datn.shopdatabase.repository.BannerRepository;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private BannerRepository bannerRepository;

    public BannerResponse create(BannerRequest req) {
        BannerEntity banner = BannerEntity.builder()
                .imageUrl(req.imageUrl())
                .link(req.link())
                .position(req.position())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .active(req.active() != null ? req.active() : true)
                .build();

        BannerEntity saved = bannerRepository.save(banner);
        return map(saved);
    }

    public BannerResponse update(Long id, BannerRequest req) {
        BannerEntity banner = bannerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        banner.setImageUrl(req.imageUrl());
        banner.setLink(req.link());
        banner.setPosition(req.position());
        banner.setSortOrder(req.sortOrder() != null ? req.sortOrder() : banner.getSortOrder());
        banner.setActive(req.active() != null ? req.active() : banner.getActive());
        bannerRepository.save(banner);
        return map(banner);
    }

    public void delete(Long id) {
        BannerEntity banner = bannerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        bannerRepository.delete(banner);
    }

    public List<BannerResponse> getByPosition(String position) {
        return bannerRepository.findByPositionAndActiveTrueOrderBySortOrderAsc(position)
                .stream().map(this::map).collect(Collectors.toList());
    }

    private BannerResponse map(BannerEntity b) {
        return new BannerResponse(b.getId(), b.getImageUrl(), b.getLink(), b.getPosition(), b.getSortOrder(), b.getActive(), b.getCreatedAt(), b.getUpdatedAt());
    }
}
