package com.budget.buddy.core.email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Service
public class GmailService {

    @Value("${google.gmail.client-id}")
    private String clientId;

    @Value("${google.gmail.client-secret}")
    private String clientSecret;

    @Value("${google.gmail.refresh-token}")
    private String refreshToken;

    @Value("${google.gmail.from-email}")
    private String fromEmail;

    private static final String APPLICATION_NAME = "Budget Buddy";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final Logger logger = LogManager.getLogger(GmailService.class);

    // We keep a single instance of the Gmail client
    private Gmail gmailClient;

    /**
     * @PostConstruct ensures this runs ONCE when the app starts.
     */
    @PostConstruct
    public void init() {
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            // Create the credential once. It handles token refreshing automatically.
            Credential credential = new GoogleCredential.Builder()
                    .setTransport(HTTP_TRANSPORT)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setRefreshToken(refreshToken);

            this.gmailClient = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            System.out.println("✅ Gmail Service Initialized Successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Gmail API", e);
        }
    }

    /**
     * Sends an email to ANYONE.
     */
    public void sendEmail(String toEmail, String subject, String bodyText) {
        try {
            // 1. Build the email content
            MimeMessage emailContent = createEmail(toEmail, subject, bodyText);

            // 2. Encode it (Base64)
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            emailContent.writeTo(buffer);
            byte[] bytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

            // 3. Send via Google API
            Message message = new Message();
            message.setRaw(encodedEmail);

            gmailClient.users().messages().send("me", message).execute();

            logger.info("Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.error("❌ Failed to send email: {}", e.getMessage());
        }
    }

    private MimeMessage createEmail(String to, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(fromEmail));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText); // For HTML, use: email.setContent(html, "text/html; charset=utf-8");
        return email;
    }
}
