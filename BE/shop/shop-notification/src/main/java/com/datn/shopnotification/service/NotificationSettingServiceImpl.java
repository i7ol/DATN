package com.datn.shopnotification.service;

import com.datn.shopdatabase.entity.NotificationSettingEntity;
import com.datn.shopdatabase.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl implements NotificationSettingService {

    private  NotificationSettingRepository repo;

    @Override
    public NotificationSettingEntity getByUser(Long userId) {
        return repo.findById(userId).orElseGet(() -> {
            NotificationSettingEntity s = NotificationSettingEntity.builder().userId(userId)
                    .emailEnabled(true).smsEnabled(false).pushEnabled(true).build();
            return repo.save(s);
        });
    }

    @Override
    public NotificationSettingEntity update(Long userId, NotificationSettingEntity setting) {
        setting.setUserId(userId);
        return repo.save(setting);
    }
}
