package vsu.cs.is.infsysserver.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import vsu.cs.is.infsysserver.exception.dto.ErrorResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleNotFoundException_shouldReturn404() {
        NotFoundException ex = new NotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getError().getCode());
        assertEquals("User not found", response.getBody().getError().getMessage());
    }

    @Test
    void handleUnauthorizedException_shouldReturn401() {
        UnauthorizedException ex = new UnauthorizedException("Invalid token");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnauthorizedException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getError().getCode());
    }

    @Test
    void handleBadCredentialsException_shouldReturn401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getBody().getError().getCode());
        assertEquals("Неверный логин или пароль", response.getBody().getError().getMessage());
    }

    @Test
    void handleForbiddenException_shouldReturn403() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().getError().getCode());
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ACCESS_DENIED", response.getBody().getError().getCode());
    }

    @Test
    void handleConflictException_shouldReturn409() {
        ConflictException ex = new ConflictException("User with email already exists");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().getError().getCode());
    }

    @Test
    void handleValidationException_shouldReturn400() {
        ValidationException ex = new ValidationException("Invalid email format");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().getError().getCode());
    }

    @Test
    void handleGeneralException_shouldReturnCustomStatus() {
        GeneralException ex = new GeneralException("Custom error", HttpStatus.ACCEPTED);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(ex);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("ACCEPTED", response.getBody().getError().getCode());
    }

    @Test
    void handleEmailException_shouldReturn500() {
        EmailException ex = new EmailException("SMTP error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEmailException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("EMAIL_ERROR", response.getBody().getError().getCode());
    }

    @Test
    void handleUnexpectedException_shouldReturn500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAllUncaughtException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError().getCode());
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturn400WithMessageContainingFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "must not be blank");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("email"));
        assertTrue(response.getBody().getError().getMessage().contains("must not be blank"));
    }
}
