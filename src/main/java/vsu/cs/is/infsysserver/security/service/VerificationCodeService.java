package vsu.cs.is.infsysserver.security.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.email.service.EmailService;
import vsu.cs.is.infsysserver.exception.NotFoundException;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCode;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCodeRepository;

import org.springframework.http.HttpStatus;
import vsu.cs.is.infsysserver.exception.GeneralException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    public void validateAndConsume(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository.findById(email)
                .orElseThrow(() -> new NotFoundException(String.format("Код верификации для почты: %s не найден", email)));

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.deleteById(email);
            throw new GeneralException("Код подтверждения истёк", HttpStatus.GONE);
        }

        if (!verificationCode.getCode().equals(code)) {
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
        verificationCodeRepository.save(verificationCode);
        log.info("Verification code generated for {}", email);

        emailService.sendVerificationCode(email, code);
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
