package vsu.cs.is.infsysserver.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends RuntimeException {

    private final HttpStatus status;

    public ForbiddenException(String message) {
        super(message);
        this.status = HttpStatus.FORBIDDEN;
    }
}
