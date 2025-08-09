package com.budget.buddy.core.email;

import com.budget.buddy.core.event.SendVerificationEmailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class SendVerificationEmailListener {
    private final JavaMailSender mailSender;

    @EventListener
    public void handle(SendVerificationEmailEvent event) {
        // build and send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject("Verify Your Budget Buddy AccountPayload");
        message.setText("Click this link to verify your account: http://localhost:8080/api/v1/auth/verify?token=" + event.token());
        mailSender.send(message);
    }
}
