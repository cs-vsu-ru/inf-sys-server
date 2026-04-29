package vsu.cs.is.infsysserver.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends RuntimeException {

    private final HttpStatus status;

    public ConflictException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }
}
