package vsu.cs.is.infsysserver.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RuntimeException {

    private final HttpStatus status;

    public NotFoundException(String message) {
        super(message);
        status = HttpStatus.NOT_FOUND;
    }

    public NotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}