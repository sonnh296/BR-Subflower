package com.hls.sunflower.service;

public interface EmailService {
    void sendVerificationEmail(String to, String username, String verificationToken);

    void sendWelcomeEmail(String to, String username);
}
