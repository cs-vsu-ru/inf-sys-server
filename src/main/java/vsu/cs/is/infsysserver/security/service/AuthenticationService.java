package vsu.cs.is.infsysserver.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.security.entity.dto.request.AuthenticationRequest;
import vsu.cs.is.infsysserver.security.entity.dto.request.RegisterRequest;
import vsu.cs.is.infsysserver.security.entity.dto.response.AuthenticationResponse;
import vsu.cs.is.infsysserver.security.entity.dto.response.StudentAuthenticationResponse;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.security.entity.token.Token;
import vsu.cs.is.infsysserver.security.entity.token.TokenRepository;
import vsu.cs.is.infsysserver.security.entity.token.TokenType;
import vsu.cs.is.infsysserver.security.util.UserMapper;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.io.IOException;

import static vsu.cs.is.infsysserver.security.util.Constants.BEARER_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final StudentRepository studentRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LdapAuthentication ldapAuthentication;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .login(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        var savedUser = repository.save(user);
        var userDetails = UserMapper.mapUserToUserDetails(savedUser);
        var jwtToken = jwtService.generateToken(userDetails);
//        var refreshToken = jwtService.generateRefreshToken(userDetails);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
//                .refreshToken(refreshToken)
                .mainRole(user.getRole().name())
                .build();
    }

    @Transactional
    public StudentAuthenticationResponse registerStudent(StudentRequest request) {
        var user = User.builder()
                .login(request.getLogin())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        var savedUser = repository.save(user);
        User supervisor = repository.getReferenceById(request.getSupervisor());

        Student student = Student.builder()
                .user(savedUser)
                .patronymic(request.getPatronymic())
                .group(request.getGroup())
                .course(request.getCourse())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .supervisor(supervisor)
                .build();

        studentRepository.save(student);

        var userDetails = UserMapper.mapUserToUserDetails(savedUser);
        var jwtToken = jwtService.generateToken(userDetails);
        saveUserToken(savedUser, jwtToken);

        StudentResponse studentResponse = new StudentResponse(student);

        return StudentAuthenticationResponse.builder()
                .accessToken(jwtToken)
                .user(studentResponse)
                .build();
    }

    public ResponseEntity<?> authenticate(AuthenticationRequest request) {
        var optionalUser = repository.findByLogin(request.getUsername());

        if (optionalUser.isEmpty() || !ldapAuthentication.isConnectionSuccess(request)
        ) {
            return new ResponseEntity<>("Неправильный логин или пароль", HttpStatus.UNAUTHORIZED);
        }

        var user = optionalUser.get();
        if (user.getPassword().isEmpty() || passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String password = passwordEncoder.encode(request.getPassword());
            repository.savePasswordByLogin(user.getLogin(), password);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var userDetail = UserMapper.mapUserToUserDetails(user);
        var jwtToken = jwtService.generateToken(userDetail);
//        var refreshToken = jwtService.generateRefreshToken(userDetail);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(jwtToken)
//                .refreshToken(refreshToken)
                .mainRole(user.getRole().name())
                .build());
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return;
        }
        String refreshToken = authHeader.substring(BEARER_PREFIX.length());
        String userLogin = jwtService.extractUsername(refreshToken);
        if (userLogin != null) {
            var user = this.repository.findByLogin(userLogin)
                    .orElseThrow();
            var userDetails = UserMapper.mapUserToUserDetails(user);
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                var accessToken = jwtService.generateToken(userDetails);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
//                        .refreshToken(refreshToken)
                        .mainRole(user.getRole().name())
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
