package com.datn.shopnotification.service;

public interface FirebaseService {
    void sendPush(String deviceToken, String title, String message);
}
