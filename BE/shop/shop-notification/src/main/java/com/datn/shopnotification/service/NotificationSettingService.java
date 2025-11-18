package com.datn.shopnotification.service;

import com.datn.shopnotification.entity.NotificationSetting;

public interface NotificationSettingService {
    NotificationSetting getByUser(Long userId);
    NotificationSetting update(Long userId, NotificationSetting setting);
}
