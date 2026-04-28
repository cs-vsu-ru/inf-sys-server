package vsu.cs.is.infsysserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vsu.cs.is.infsysserver.security.entity.dto.request.AuthenticationRequest;
import vsu.cs.is.infsysserver.security.entity.dto.request.VerifyTwoFactorRequest;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.security.entity.token.TokenRepository;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCode;
import vsu.cs.is.infsysserver.security.entity.verificationcode.VerificationCodeRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Интеграционные тесты аутентификации")
class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    VerificationCodeRepository verificationCodeRepository;

    @MockBean
    JavaMailSender mailSender;

    private static final String LOGIN_NO_2FA  = "it_user_no2fa";
    private static final String LOGIN_2FA     = "it_user_2fa";
    private static final String PASSWORD      = "IntegrationPass1";
    private static final String EMAIL_2FA     = "it_user_2fa@cs.vsu.ru";

    @BeforeEach
    void setUp() {
        verificationCodeRepository.deleteAll();
        cleanupUser(LOGIN_NO_2FA);
        cleanupUser(LOGIN_2FA);

        userRepository.save(User.builder()
                .login(LOGIN_NO_2FA)
                .password(passwordEncoder.encode(PASSWORD))
                .role(Role.USER)
                .twoFactorEnabled(false)
                .build());

        userRepository.save(User.builder()
                .login(LOGIN_2FA)
                .password(passwordEncoder.encode(PASSWORD))
                .email(EMAIL_2FA)
                .role(Role.USER)
                .twoFactorEnabled(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        verificationCodeRepository.deleteAll();
        cleanupUser(LOGIN_NO_2FA);
        cleanupUser(LOGIN_2FA);
    }

    // ─────────────────────── Старый флоу (2FA выключен) ──────────────────────────

    @Test
    @DisplayName("Логин без 2FA — возвращает JWT")
    void authenticate_TwoFactorDisabled_ReturnsJwt() throws Exception {
        var request = new AuthenticationRequest(LOGIN_NO_2FA, PASSWORD);

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").isNotEmpty())
                .andExpect(jsonPath("$.mainRole").value("USER"));
    }

    @Test
    @DisplayName("Логин с несуществующим пользователем — 401")
    void authenticate_UserNotFound_Returns401() throws Exception {
        var request = new AuthenticationRequest("ghost_user", "anyPassword");

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Логин с неверным паролем — 401")
    void authenticate_WrongPassword_Returns401() throws Exception {
        var request = new AuthenticationRequest(LOGIN_NO_2FA, "WrongPassword!");

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────── Новый флоу (2FA включён) ───────────────────────────

    @Test
    @DisplayName("Логин с 2FA — код отправляется, возвращает pendingResponse")
    void authenticate_TwoFactorEnabled_ReturnsPendingResponse() throws Exception {
        var request = new AuthenticationRequest(LOGIN_2FA, PASSWORD);

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFactor").value(true))
                .andExpect(jsonPath("$.email").value(EMAIL_2FA));

        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Логин с 2FA — код сохраняется в БД")
    void authenticate_TwoFactorEnabled_CodeSavedToDatabase() throws Exception {
        var request = new AuthenticationRequest(LOGIN_2FA, PASSWORD);

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var savedCode = verificationCodeRepository.findById(EMAIL_2FA);
        assert savedCode.isPresent();
        assert savedCode.get().getCode().length() == 6;
        assert savedCode.get().getExpiresAt().isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Верификация 2FA — верный код — возвращает JWT и удаляет код")
    void verifyTwoFactor_ValidCode_ReturnsJwtAndDeletesCode() throws Exception {
        // Генерируем код через /authenticate
        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthenticationRequest(LOGIN_2FA, PASSWORD))))
                .andExpect(status().isOk());

        String code = verificationCodeRepository.findById(EMAIL_2FA).orElseThrow().getCode();

        // Верифицируем код
        mockMvc.perform(post("/api/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyTwoFactorRequest(EMAIL_2FA, code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").isNotEmpty())
                .andExpect(jsonPath("$.mainRole").value("USER"));

        // Код удалён из БД
        assert verificationCodeRepository.findById(EMAIL_2FA).isEmpty();
    }

    @Test
    @DisplayName("Верификация 2FA — неверный код — ошибка")
    void verifyTwoFactor_WrongCode_ReturnsError() throws Exception {
        verificationCodeRepository.save(new VerificationCode(
                EMAIL_2FA, "111111",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now(), 0, null
        ));

        mockMvc.perform(post("/api/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyTwoFactorRequest(EMAIL_2FA, "999999"))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Верификация 2FA — истёкший код — ошибка и код удаляется из БД")
    void verifyTwoFactor_ExpiredCode_ReturnsErrorAndCleansUp() throws Exception {
        verificationCodeRepository.save(new VerificationCode(
                EMAIL_2FA, "123456",
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now().minusMinutes(6), 0, null
        ));

        mockMvc.perform(post("/api/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyTwoFactorRequest(EMAIL_2FA, "123456"))))
                .andExpect(status().is4xxClientError());

        assert verificationCodeRepository.findById(EMAIL_2FA).isEmpty();
    }

    @Test
    @DisplayName("Верификация 2FA — email не существует — ошибка")
    void verifyTwoFactor_UnknownEmail_ReturnsError() throws Exception {
        mockMvc.perform(post("/api/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyTwoFactorRequest("ghost@cs.vsu.ru", "000000"))))
                .andExpect(status().is4xxClientError());
    }

    private void cleanupUser(String login) {
        userRepository.findByLogin(login).ifPresent(userRepository::delete);
    }
}