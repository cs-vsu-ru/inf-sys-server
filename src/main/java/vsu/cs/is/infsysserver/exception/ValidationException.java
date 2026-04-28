package vsu.cs.is.infsysserver.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {

    private final HttpStatus status;

    public ValidationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
