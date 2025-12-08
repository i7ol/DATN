package com.datn.shopnotification.service;


import com.datn.shopdatabase.entity.NotificationSettingEntity;

public interface NotificationSettingService {
    NotificationSettingEntity getByUser(Long userId);
    NotificationSettingEntity update(Long userId, NotificationSettingEntity setting);
}
