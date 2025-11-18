package com.datn.shopnotification.service;

import com.datn.shopnotification.entity.NotificationSetting;
import com.datn.shopnotification.repository.NotificationSettingRepository;
import com.datn.shopnotification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl implements NotificationSettingService {

    private final NotificationSettingRepository repo;

    @Override
    public NotificationSetting getByUser(Long userId) {
        return repo.findById(userId).orElseGet(() -> {
            NotificationSetting s = NotificationSetting.builder().userId(userId)
                    .emailEnabled(true).smsEnabled(false).pushEnabled(true).build();
            return repo.save(s);
        });
    }

    @Override
    public NotificationSetting update(Long userId, NotificationSetting setting) {
        setting.setUserId(userId);
        return repo.save(setting);
    }
}
