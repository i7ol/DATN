package com.datn.shopnotification.service;

import com.datn.shopnotification.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendSms(String phone, String message) {
        // TODO: integrate with provider (Twilio, Nexmo, etc.)
        log.info("SMS to {}: {}", phone, message);
    }
}
