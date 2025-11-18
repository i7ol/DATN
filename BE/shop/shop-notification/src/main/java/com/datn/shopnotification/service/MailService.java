package com.datn.shopnotification.service;

public interface MailService {
    void sendEmail(String to, String subject, String content);
    void sendEmailWithAttachment(String to, String subject, String content, byte[] attachment, String filename);
}
