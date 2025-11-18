package com.datn.shopcms.repository;

import com.datn.shopcms.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByPositionAndActiveTrueOrderBySortOrderAsc(String position);
}
