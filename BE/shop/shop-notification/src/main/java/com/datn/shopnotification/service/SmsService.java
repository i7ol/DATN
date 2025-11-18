package com.datn.shopnotification.service;

public interface SmsService {
    void sendSms(String phone, String message);
}
