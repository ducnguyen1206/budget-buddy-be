package com.budget.buddy.core.email;

import com.budget.buddy.core.dto.SendVerificationEmailEvent;
import com.budget.buddy.transaction.domain.service.impl.AccountDataImpl;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendVerificationEmailListener {

    // 1. Inject your new GmailService instead of JavaMailSender
    private final GmailService gmailService;

    private static final Logger logger = LogManager.getLogger(SendVerificationEmailListener.class);

    @Value("${cors.app}")
    private String appCors;

    @Async
    @EventListener
    public void handle(SendVerificationEmailEvent event) {
        String toEmail = event.email();
        logger.info("Sending verification email to: {}", toEmail);

        String subject = "Verify Your Budget Buddy Account";
        String body = String.format("Click this link to verify your account: %s/token/%s", appCors, event.token());

        gmailService.sendEmail(toEmail, subject, body);
    }
}
