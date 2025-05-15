package com.example.auth.service;

import com.example.auth.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendPasswordResetEmail(User user, String resetUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setTo(user.getEmail());
        helper.setSubject("Password Reset Request");
        
        String emailContent = String.format(
            "Hi %s,<br><br>" +
            "You requested to reset your password.<br><br>" +
            "Click the link below to set a new password:<br><br>" +
            "<a href=\"%s\">Reset Password</a><br><br>" +
            "If you did not request this, please ignore this email and your password will remain unchanged.<br><br>" +
            "Thanks,<br>The Learning Management System Team",
            user.getName(), resetUrl
        );
        
        helper.setText(emailContent, true);
        mailSender.send(message);
    }
} 