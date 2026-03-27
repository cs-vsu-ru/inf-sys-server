package vsu.cs.is.infsysserver.email.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.email.service.EmailService;
import vsu.cs.is.infsysserver.exception.EmailException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationCode(String email, String code) {
        try {
            log.info("Sending verification code to {}", email);
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(email);
            message.setSubject("Код подтверждения");
            String text = String.format(
                    "Здравствуйте!\n\nВаш код подтверждения для входа: %s",
                    code
            );
            message.setText(text);
            mailSender.send(message);
            log.info("Verification code should be sent");
        } catch (Exception e) {
            throw new EmailException("JavaMail error: " + e.getMessage());
        }
    }
}