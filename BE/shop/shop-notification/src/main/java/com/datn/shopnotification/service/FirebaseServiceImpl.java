package com.datn.shopnotification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class FirebaseServiceImpl implements FirebaseService {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully.");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    @Override
    public void sendPush(String deviceToken, String title, String message) {
        try {
            Message msg = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(msg);
            log.info("Push sent successfully: {}", response);
        } catch (Exception e) {
            log.error("Error sending push to {}: {} - {}", deviceToken, title, message, e);
        }
    }
}
