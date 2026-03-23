package com.Major.majorProject.configuration;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage(Session.getInstance(new Properties()));
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                try {
                    return new MimeMessage(Session.getInstance(new Properties()), contentStream);
                } catch (Exception ex) {
                    throw new MailParseException("Failed to create MimeMessage", ex);
                }
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {
            }
        };
    }
}
