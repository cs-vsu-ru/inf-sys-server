package vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response;

public record StudentTopicsImportErrorResponse(
        int rowNumber,
        String studentLogin,
        String message
) {
}
