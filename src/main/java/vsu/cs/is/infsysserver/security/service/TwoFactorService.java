package vsu.cs.is.infsysserver.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.exception.GeneralException;
import vsu.cs.is.infsysserver.exception.NotFoundException;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;

    public ResponseEntity<?> startEnable(String login, String requestedEmail) {
        User user = getUser(login);

        String targetEmail = (requestedEmail != null && !requestedEmail.isBlank())
                ? requestedEmail
                : user.getEmail();

        if (targetEmail == null || targetEmail.isBlank()) {
            return new ResponseEntity<>("Укажите email для подключения 2FA", HttpStatus.BAD_REQUEST);
        }

        try {
            verificationCodeService.generateAndSendCode(targetEmail);
        } catch (GeneralException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> verifyEnable(String login, String email, String code) {
        User user = getUser(login);

        if (email == null || email.isBlank()) {
            return new ResponseEntity<>("Email не указан", HttpStatus.BAD_REQUEST);
        }

        try {
            verificationCodeService.validateAndConsume(email, code);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (GeneralException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        user.setTwoFactorEnabled(true);
        if (!email.equals(user.getEmail())) {
            user.setEmail(email);
        }
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> startDisable(String login) {
        User user = getUser(login);

        if (!user.isTwoFactorEnabled()) {
            return new ResponseEntity<>("2FA уже отключена", HttpStatus.BAD_REQUEST);
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return new ResponseEntity<>("У пользователя не указана почта для 2FA", HttpStatus.BAD_REQUEST);
        }

        try {
            verificationCodeService.generateAndSendCode(user.getEmail());
        } catch (GeneralException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> verifyDisable(String login, String code) {
        User user = getUser(login);

        if (!user.isTwoFactorEnabled()) {
            return new ResponseEntity<>("2FA уже отключена", HttpStatus.BAD_REQUEST);
        }

        try {
            verificationCodeService.validateAndConsume(user.getEmail(), code);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (GeneralException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }

        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> resend(String email) {
        try {
            verificationCodeService.resendCode(email);
        } catch (GeneralException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    private User getUser(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}