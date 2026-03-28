package vsu.cs.is.infsysserver.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.email.service.EmailService;
import vsu.cs.is.infsysserver.exception.GeneralException;
import vsu.cs.is.infsysserver.exception.NotFoundException;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCode;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCodeRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class VerificationCodeService {

    @Value("${application.security.two-factor.max-code-attempts:5}")
    private int maxCodeAttempts;

    @Value("${application.security.two-factor.block-duration-minutes:5}")
    private int blockDurationMinutes;

    @Value("${application.security.two-factor.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository, EmailService emailService) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.emailService = emailService;
    }

    public void validateAndConsume(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository.findById(email)
                .orElseThrow(() -> new NotFoundException(String.format("Код верификации для почты: %s не найден", email)));

        if (verificationCode.getBlockedUntil() != null && LocalDateTime.now().isBefore(verificationCode.getBlockedUntil())) {
            throw new GeneralException("Слишком много неверных попыток. Попробуйте через " + blockDurationMinutes + " минут", HttpStatus.TOO_MANY_REQUESTS);
        }

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.deleteById(email);
            throw new GeneralException("Код подтверждения истёк", HttpStatus.GONE);
        }

        if (!verificationCode.getCode().equals(code)) {
            int attempts = verificationCode.getAttemptCount() + 1;
            verificationCode.setAttemptCount(attempts);
            if (attempts >= maxCodeAttempts) {
                verificationCode.setBlockedUntil(LocalDateTime.now().plusMinutes(blockDurationMinutes));
            }
            verificationCodeRepository.save(verificationCode);
            throw new GeneralException("Неверный код подтверждения", HttpStatus.UNAUTHORIZED);
        }

        verificationCodeRepository.deleteById(email);
    }

    public void generateAndSendCode(String email) {
        log.info("Generating verification code for email {}", email);
        String code = generateCode();

        VerificationCode verificationCode = verificationCodeRepository.findById(email)
                .orElse(new VerificationCode());
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(LocalDateTime.now());
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verificationCode.setAttemptCount(0);
        verificationCode.setBlockedUntil(null);
        verificationCodeRepository.save(verificationCode);
        log.info("Verification code generated for {}", email);

        emailService.sendVerificationCode(email, code);
    }

    public void resendCode(String email) {
        verificationCodeRepository.findById(email).ifPresent(vc -> {
            long secondsSinceCreation = ChronoUnit.SECONDS.between(vc.getCreatedAt(), LocalDateTime.now());
            if (secondsSinceCreation < resendCooldownSeconds) {
                long remaining = resendCooldownSeconds - secondsSinceCreation;
                throw new GeneralException("Повторная отправка возможна через " + remaining + " секунд", HttpStatus.TOO_MANY_REQUESTS);
            }
        });
        generateAndSendCode(email);
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
