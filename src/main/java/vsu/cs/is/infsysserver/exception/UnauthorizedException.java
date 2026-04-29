package vsu.cs.is.infsysserver.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RuntimeException {

    private final HttpStatus status;

    public UnauthorizedException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
    }
}
