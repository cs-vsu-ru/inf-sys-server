package vsu.cs.is.infsysserver.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import vsu.cs.is.infsysserver.exception.GeneralException;
import vsu.cs.is.infsysserver.exception.NotFoundException;
import vsu.cs.is.infsysserver.exception.UnauthorizedException;
import vsu.cs.is.infsysserver.security.entity.dto.request.AuthenticationRequest;
import vsu.cs.is.infsysserver.security.entity.dto.request.VerifyTwoFactorRequest;
import vsu.cs.is.infsysserver.security.entity.dto.response.AuthenticationResponse;
import vsu.cs.is.infsysserver.security.entity.dto.response.TwoFactorRequiredResponse;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.security.entity.token.Token;
import vsu.cs.is.infsysserver.security.entity.token.TokenRepository;
import vsu.cs.is.infsysserver.security.service.AuthenticationService;
import vsu.cs.is.infsysserver.security.service.JwtService;
import vsu.cs.is.infsysserver.security.service.LdapAuthentication;
import vsu.cs.is.infsysserver.security.service.VerificationCodeService;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты AuthenticationService")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private LdapAuthentication ldapAuthentication;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private AuthenticationService authenticationService;

    // ─────────────────────────── Старый флоу (без 2FA) ───────────────────────────

    @Test
    @DisplayName("Аутентификация — пользователь не найден — 401")
    void authenticate_UserNotFound_Returns401() {
        // given
        var request = new AuthenticationRequest("unknown", "pass");
        doReturn(Optional.empty()).when(userRepository).findByLogin("unknown");
        // LDAP не вызывается — short-circuit при пустом Optional

        // then
        assertThrows(UnauthorizedException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    @DisplayName("Аутентификация — LDAP вернул ошибку — 401")
    void authenticate_LdapFails_Returns401() {
        // given
        var user = buildUser(false, "student@cs.vsu.ru");
        var request = new AuthenticationRequest(user.getLogin(), "pass");
        doReturn(Optional.of(user)).when(userRepository).findByLogin(user.getLogin());
        doReturn(false).when(ldapAuthentication).isConnectionSuccess(request);

        // then
        assertThrows(UnauthorizedException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    @DisplayName("Аутентификация — authenticationManager отклонил — 401")
    void authenticate_AuthManagerRejects_Returns401() {
        // given
        var user = buildUser(false, "student@cs.vsu.ru");
        var request = new AuthenticationRequest(user.getLogin(), "wrong_pass");
        doReturn(Optional.of(user)).when(userRepository).findByLogin(user.getLogin());
        doReturn(true).when(ldapAuthentication).isConnectionSuccess(request);
        doReturn(false).when(passwordEncoder).matches("wrong_pass", user.getPassword());
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager).authenticate(any());

        // then
        assertThrows(UnauthorizedException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    @DisplayName("Аутентификация без 2FA — возвращает JWT")
    void authenticate_TwoFactorDisabled_ReturnsJwtToken() {
        // given
        var user = buildUser(false, "student@cs.vsu.ru");
        var request = new AuthenticationRequest(user.getLogin(), "raw_pass");
        doReturn(Optional.of(user)).when(userRepository).findByLogin(user.getLogin());
        doReturn(true).when(ldapAuthentication).isConnectionSuccess(request);
        doReturn(false).when(passwordEncoder).matches("raw_pass", user.getPassword());
        doReturn(List.of()).when(tokenRepository).findAllValidTokenByUser(user.getId());
        doReturn("jwt-token").when(jwtService).generateToken(any(UserDetails.class));

        // when
        ResponseEntity<?> response = authenticationService.authenticate(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = (AuthenticationResponse) response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token", body.getAccessToken());
        assertEquals(Role.USER.name(), body.getMainRole());

        verify(verificationCodeService, never()).generateAndSendCode(any());
    }

    @Test
    @DisplayName("Аутентификация без 2FA — синхронизация пароля при совпадении")
    void authenticate_TwoFactorDisabled_PasswordSyncedOnMatch() {
        // given
        var user = buildUser(false, "student@cs.vsu.ru");
        var request = new AuthenticationRequest(user.getLogin(), "raw_pass");
        doReturn(Optional.of(user)).when(userRepository).findByLogin(user.getLogin());
        doReturn(true).when(ldapAuthentication).isConnectionSuccess(request);
        doReturn(true).when(passwordEncoder).matches("raw_pass", user.getPassword());
        doReturn("new_encoded").when(passwordEncoder).encode("raw_pass");
        doReturn(List.of()).when(tokenRepository).findAllValidTokenByUser(user.getId());
        doReturn("jwt-token").when(jwtService).generateToken(any(UserDetails.class));

        // when
        authenticationService.authenticate(request);

        // then
        verify(userRepository).savePasswordByLogin(user.getLogin(), "new_encoded");
    }

    // ─────────────────────────── Новый флоу (2FA включён) ────────────────────────

    @Test
    @DisplayName("Аутентификация с 2FA — код отправлен, возвращает TwoFactorRequiredResponse")
    void authenticate_TwoFactorEnabled_SendsCodeAndReturnsPendingResponse() {
        // given
        var user = buildUser(true, "student@cs.vsu.ru");
        var request = new AuthenticationRequest(user.getLogin(), "raw_pass");
        doReturn(Optional.of(user)).when(userRepository).findByLogin(user.getLogin());
        doReturn(true).when(ldapAuthentication).isConnectionSuccess(request);
        doReturn(false).when(passwordEncoder).matches("raw_pass", user.getPassword());

        // when
        ResponseEntity<?> response = authenticationService.authenticate(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = (TwoFactorRequiredResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isRequiresTwoFactor());
        assertEquals("student@cs.vsu.ru", body.getEmail());

        verify(verificationCodeService).generateAndSendCode("student@cs.vsu.ru");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Аутентификация с 2FA — почта не указана — 400")
    void authenticate_TwoFactorEnabledNoEmail_Returns400() {
        // given
        var user = buildUser(true, null);
        var request = new AuthenticationRequest(user.getLogin(), "raw_pass");
        doReturn(Optional.of(user)).when(userRepository).findByLogin(user.getLogin());
        doReturn(true).when(ldapAuthentication).isConnectionSuccess(request);
        doReturn(false).when(passwordEncoder).matches("raw_pass", user.getPassword());

        // then
        assertThrows(GeneralException.class, () -> authenticationService.authenticate(request));
        verify(verificationCodeService, never()).generateAndSendCode(any());
    }

    // ─────────────────────────── verifyTwoFactor ─────────────────────────────────

    @Test
    @DisplayName("Верификация 2FA — верный код — возвращает JWT")
    void verifyTwoFactor_ValidCode_ReturnsJwtToken() {
        // given
        var user = buildUser(true, "student@cs.vsu.ru");
        var request = new VerifyTwoFactorRequest("student@cs.vsu.ru", "654321");
        doReturn(Optional.of(user)).when(userRepository).findByEmail("student@cs.vsu.ru");
        doNothing().when(verificationCodeService).validateAndConsume("student@cs.vsu.ru", "654321");
        doReturn(List.of()).when(tokenRepository).findAllValidTokenByUser(user.getId());
        doReturn("jwt-token").when(jwtService).generateToken(any(UserDetails.class));

        // when
        ResponseEntity<?> response = authenticationService.verifyTwoFactor(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = (AuthenticationResponse) response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token", body.getAccessToken());
        assertEquals(Role.USER.name(), body.getMainRole());
    }

    @Test
    @DisplayName("Верификация 2FA — пользователь не найден по email — 404")
    void verifyTwoFactor_UserNotFound_Returns404() {
        // given
        var request = new VerifyTwoFactorRequest("ghost@cs.vsu.ru", "000000");
        doReturn(Optional.empty()).when(userRepository).findByEmail("ghost@cs.vsu.ru");

        // then
        assertThrows(NotFoundException.class, () -> authenticationService.verifyTwoFactor(request));
        verify(verificationCodeService, never()).validateAndConsume(any(), any());
    }

    @Test
    @DisplayName("Верификация 2FA — старые токены отзываются при выдаче нового JWT")
    void verifyTwoFactor_ValidCode_RevokesOldTokens() {
        // given
        var user = buildUser(true, "student@cs.vsu.ru");
        var oldToken = Token.builder().expired(false).revoked(false).build();
        var request = new VerifyTwoFactorRequest("student@cs.vsu.ru", "654321");
        doReturn(Optional.of(user)).when(userRepository).findByEmail("student@cs.vsu.ru");
        doNothing().when(verificationCodeService).validateAndConsume("student@cs.vsu.ru", "654321");
        doReturn(List.of(oldToken)).when(tokenRepository).findAllValidTokenByUser(user.getId());
        doReturn("jwt-token").when(jwtService).generateToken(any(UserDetails.class));

        // when
        authenticationService.verifyTwoFactor(request);

        // then
        assertTrue(oldToken.isExpired());
        assertTrue(oldToken.isRevoked());
        verify(tokenRepository).saveAll(List.of(oldToken));
    }

    // ──────────────────────────────── helpers ────────────────────────────────────

    private User buildUser(boolean twoFactorEnabled, String email) {
        return User.builder()
                .id(1L)
                .login("testuser")
                .password("encoded_pass")
                .email(email)
                .role(Role.USER)
                .twoFactorEnabled(twoFactorEnabled)
                .build();
    }
}