package vsu.cs.is.infsysserver.exception.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private final boolean success;
    private final ErrorDetails error;

    @Data
    @Builder
    public static class ErrorDetails {
        private final String code;
        private final String message;
    }

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }
}
