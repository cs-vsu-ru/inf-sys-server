package vsu.cs.is.infsysserver.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vsu.cs.is.infsysserver.email.service.EmailService;
import vsu.cs.is.infsysserver.exception.GeneralException;
import vsu.cs.is.infsysserver.exception.NotFoundException;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCode;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCodeRepository;
import vsu.cs.is.infsysserver.security.service.VerificationCodeService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты VerificationCodeService")
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    // ──────────────────────────── generateAndSendCode ────────────────────────────

    @Test
    @DisplayName("Генерация кода для новой почты — сохранение и отправка")
    void generateAndSendCode_NewEmail_SavesAndSendsCode() {
        // given
        String email = "student@cs.vsu.ru";
        doReturn(Optional.empty()).when(verificationCodeRepository).findById(email);
        doReturn(new VerificationCode()).when(verificationCodeRepository).save(any(VerificationCode.class));

        // when
        verificationCodeService.generateAndSendCode(email);

        // then
        ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(verificationCodeRepository).save(captor.capture());
        VerificationCode saved = captor.getValue();

        assertEquals(email, saved.getEmail());
        assertNotNull(saved.getCode());
        assertEquals(6, saved.getCode().length());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getExpiresAt());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));

        verify(emailService).sendVerificationCode(eq(email), eq(saved.getCode()));
    }

    @Test
    @DisplayName("Генерация кода при существующей записи — перезапись")
    void generateAndSendCode_ExistingEmail_OverwritesCode() {
        // given
        String email = "student@cs.vsu.ru";
        var existing = new VerificationCode(email, "000001", LocalDateTime.now().plusMinutes(3), LocalDateTime.now().minusMinutes(2), 0, null);
        doReturn(Optional.of(existing)).when(verificationCodeRepository).findById(email);

        // when
        verificationCodeService.generateAndSendCode(email);

        // then
        verify(verificationCodeRepository).save(existing);
        verify(emailService).sendVerificationCode(eq(email), anyString());
    }

    // ──────────────────────────── validateAndConsume ─────────────────────────────

    @Test
    @DisplayName("Валидация верного кода — код удаляется")
    void validateAndConsume_ValidCode_DeletesCode() {
        // given
        String email = "student@cs.vsu.ru";
        String code = "123456";
        var verificationCode = new VerificationCode(email, code, LocalDateTime.now().plusMinutes(4), LocalDateTime.now(), 0, null);
        doReturn(Optional.of(verificationCode)).when(verificationCodeRepository).findById(email);

        // when
        verificationCodeService.validateAndConsume(email, code);

        // then
        verify(verificationCodeRepository).deleteById(email);
    }

    @Test
    @DisplayName("Валидация истёкшего кода — исключение и очистка записи")
    void validateAndConsume_ExpiredCode_ThrowsAndCleansUp() {
        // given
        String email = "student@cs.vsu.ru";
        String code = "123456";
        var expired = new VerificationCode(email, code, LocalDateTime.now().minusSeconds(1), LocalDateTime.now().minusMinutes(6), 0, null);
        doReturn(Optional.of(expired)).when(verificationCodeRepository).findById(email);

        // when / then
        assertThrows(GeneralException.class, () -> verificationCodeService.validateAndConsume(email, code));
        verify(verificationCodeRepository).deleteById(email);
    }

    @Test
    @DisplayName("Валидация неверного кода — исключение, запись не удаляется")
    void validateAndConsume_WrongCode_ThrowsWithoutDeletion() {
        // given
        String email = "student@cs.vsu.ru";
        var verificationCode = new VerificationCode(email, "111111", LocalDateTime.now().plusMinutes(4), LocalDateTime.now(), 0, null);
        doReturn(Optional.of(verificationCode)).when(verificationCodeRepository).findById(email);

        // when / then
        assertThrows(GeneralException.class, () -> verificationCodeService.validateAndConsume(email, "999999"));
        verify(verificationCodeRepository, never()).deleteById(email);
    }

    @Test
    @DisplayName("Валидация — код не найден — NotFoundException")
    void validateAndConsume_CodeNotFound_ThrowsNotFoundException() {
        // given
        String email = "student@cs.vsu.ru";
        doReturn(Optional.empty()).when(verificationCodeRepository).findById(email);

        // when / then
        assertThrows(NotFoundException.class, () -> verificationCodeService.validateAndConsume(email, "123456"));
        verify(verificationCodeRepository, never()).deleteById(any());
    }
}