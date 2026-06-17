package com.signatureapp.service;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSignatureLink(String to, String signerName, String documentName, String url) {
        var message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Signature requested: " + documentName);
        message.setText("""
                Hello %s,

                Please review and sign the document: %s

                Signing link:
                %s

                This link is tokenized for traceability.
                """.formatted(signerName, documentName, url));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            System.out.println("Email not sent. Development link: " + url);
        }
    }
}
