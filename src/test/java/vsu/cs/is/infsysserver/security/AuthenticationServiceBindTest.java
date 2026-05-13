package vsu.cs.is.infsysserver.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import vsu.cs.is.infsysserver.exception.ConflictException;
import vsu.cs.is.infsysserver.exception.ForbiddenException;
import vsu.cs.is.infsysserver.exception.UnauthorizedException;
import vsu.cs.is.infsysserver.security.entity.dto.request.AuthenticationRequest;
import vsu.cs.is.infsysserver.security.entity.dto.request.StudentBindRequest;
import vsu.cs.is.infsysserver.security.entity.dto.response.AuthenticationResponse;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.security.entity.token.TokenRepository;
import vsu.cs.is.infsysserver.security.service.AuthenticationService;
import vsu.cs.is.infsysserver.security.service.JwtService;
import vsu.cs.is.infsysserver.security.service.LdapAuthentication;
import vsu.cs.is.infsysserver.security.service.VerificationCodeService;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты bindStudent")
class AuthenticationServiceBindTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private LdapAuthentication ldapAuthentication;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private VerificationCodeService verificationCodeService;

    @InjectMocks private AuthenticationService authenticationService;

    @Test
    @DisplayName("bind — LDAP не пустил — 401")
    void bind_LdapFails_Throws401() {
        var req = new StudentBindRequest("ivanov_i_i", "wrong", "16250362");
        doReturn(false).when(ldapAuthentication).isConnectionSuccess(any(AuthenticationRequest.class));

        assertThrows(UnauthorizedException.class, () -> authenticationService.bindStudent(req));
    }

    @Test
    @DisplayName("bind — moodleLogin не найден — 403")
    void bind_MoodleLoginNotFound_Throws403() {
        var req = new StudentBindRequest("ivanov_i_i", "pass", "16250362");
        doReturn(true).when(ldapAuthentication).isConnectionSuccess(any(AuthenticationRequest.class));
        doReturn(Optional.empty()).when(userRepository).findByLogin("16250362");

        assertThrows(ForbiddenException.class, () -> authenticationService.bindStudent(req));
    }

    @Test
    @DisplayName("bind — adLogin уже занят другим пользователем — ConflictException")
    void bind_AdLoginAlreadyTaken_ThrowsConflict() {
        var pendingUser = User.builder().id(1L).login("16250362").role(Role.USER).build();
        var conflictingUser = User.builder().id(2L).login("ivanov_i_i").role(Role.USER).build();
        var req = new StudentBindRequest("ivanov_i_i", "pass", "16250362");

        doReturn(true).when(ldapAuthentication).isConnectionSuccess(any(AuthenticationRequest.class));
        doReturn(Optional.of(pendingUser)).when(userRepository).findByLogin("16250362");
        doReturn(Optional.of(conflictingUser)).when(userRepository).findByLogin("ivanov_i_i");

        assertThrows(ConflictException.class, () -> authenticationService.bindStudent(req));
    }

    @Test
    @DisplayName("bind — успех: ФИО из LDAP совпадает, login переписывается на AD, выдан JWT")
    void bind_Success_ReturnsJwt() {
        var pendingUser = User.builder()
                .id(1L)
                .login("16250362")
                .firstName("Иван")
                .lastName("Иванов")
                .role(Role.USER)
                .password("")
                .build();
        var req = new StudentBindRequest("ivanov_i_i", "ad_pass", "16250362");

        doReturn(true).when(ldapAuthentication).isConnectionSuccess(any(AuthenticationRequest.class));
        doReturn(Optional.of(new vsu.cs.is.infsysserver.security.service.LdapUserInfo(
                "Иванов", "Иван", "ivanov@cs.vsu.ru"
        ))).when(ldapAuthentication).fetchUserDetails(any(AuthenticationRequest.class));
        doReturn(Optional.of(pendingUser)).when(userRepository).findByLogin("16250362");
        doReturn(Optional.empty()).when(userRepository).findByLogin("ivanov_i_i");
        doReturn("encoded").when(passwordEncoder).encode("ad_pass");
        doAnswer(invocation -> invocation.getArgument(0)).when(userRepository).save(any(User.class));
        doReturn("jwt-token").when(jwtService).generateToken(any(UserDetails.class));

        AuthenticationResponse response = authenticationService.bindStudent(req);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("USER", response.getMainRole());
        assertEquals("ivanov_i_i", pendingUser.getLogin());
        assertEquals("encoded", pendingUser.getPassword());
        verify(tokenRepository).save(any());
    }

    @Test
    @DisplayName("bind — ФИО из LDAP не совпадает с найденным студентом — 403, login не переписан")
    void bind_LdapNameMismatch_Throws403() {
        var petrov = User.builder()
                .id(2L)
                .login("16250400")
                .firstName("Пётр")
                .lastName("Петров")
                .role(Role.USER)
                .build();
        var req = new StudentBindRequest("ivanov_i_i", "ad_pass", "16250400");

        doReturn(true).when(ldapAuthentication).isConnectionSuccess(any(AuthenticationRequest.class));
        doReturn(Optional.of(new vsu.cs.is.infsysserver.security.service.LdapUserInfo(
                "Иванов", "Иван", "ivanov@cs.vsu.ru"
        ))).when(ldapAuthentication).fetchUserDetails(any(AuthenticationRequest.class));
        doReturn(Optional.of(petrov)).when(userRepository).findByLogin("16250400");
        doReturn(Optional.empty()).when(userRepository).findByLogin("ivanov_i_i");

        assertThrows(ForbiddenException.class, () -> authenticationService.bindStudent(req));
        assertEquals("16250400", petrov.getLogin()); // login не должен быть перезаписан
        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }

    @Test
    @DisplayName("bind — LDAP не отдал ФИО — 403, login не переписан")
    void bind_LdapNoDetails_Throws403() {
        var pendingUser = User.builder()
                .id(1L)
                .login("16250362")
                .firstName("Иван")
                .lastName("Иванов")
                .role(Role.USER)
                .build();
        var req = new StudentBindRequest("ivanov_i_i", "ad_pass", "16250362");

        doReturn(true).when(ldapAuthentication).isConnectionSuccess(any(AuthenticationRequest.class));
        doReturn(Optional.empty()).when(ldapAuthentication).fetchUserDetails(any(AuthenticationRequest.class));
        doReturn(Optional.of(pendingUser)).when(userRepository).findByLogin("16250362");
        doReturn(Optional.empty()).when(userRepository).findByLogin("ivanov_i_i");

        assertThrows(ForbiddenException.class, () -> authenticationService.bindStudent(req));
        assertEquals("16250362", pendingUser.getLogin());
        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }
}
