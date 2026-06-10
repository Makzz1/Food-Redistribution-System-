package com.foodredistribution.foodredistribution.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.repository.UserRepository;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    public EmailService(
            JavaMailSender mailSender,
            UserRepository userRepository

    ) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    @Async
    public void sendVerificationEmail(
            String toEmail,
            String token
    ) {

        String verificationLink =
                "http://localhost:8080/api/v1/auth/verify-email?token="
                        + token;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject(
                "Verify Your Email"
        );

        message.setText(
                "Click the link to verify your email:\n"
                        + verificationLink
        );

        mailSender.send(message);
    }

    public void verifyEmail(String token) {

        User user = userRepository
                .findByVerificationToken(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid verification token"
                        ));

        user.setEmailVerified(true);

        user.setVerificationToken(null);

        userRepository.save(user);
    }

    @Async
    public void sendFoodNotificationEmail(

                String toEmail,
                String foodName,
                String location,
                Integer quantity

        ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject(
                "New Food Available Nearby"
        );

        message.setText(
                """
                Food Available Nearby!
                
                Food: """ + foodName + "\n"

                        +

                        "Quantity: " + quantity + "\n"

                        +

                        "Location: " + location
        );

        mailSender.send(message);
        }

    @Async
    public void sendPasswordResetEmail(
            String toEmail,
            String token
    ) {

        // Replace with your actual frontend URL
        String resetLink =
                "http://localhost:3000/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject("Reset Your Password");

        message.setText(
                "You requested a password reset.\n\n"
                        + "Click the link below to reset your password:\n"
                        + resetLink
                        + "\n\nThis link will expire in 15 minutes.\n\n"
                        + "If you did not request this, please ignore this email."
        );

        mailSender.send(message);
    }

}