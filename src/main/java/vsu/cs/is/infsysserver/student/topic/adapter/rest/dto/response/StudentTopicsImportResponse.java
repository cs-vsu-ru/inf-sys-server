package vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response;

import java.util.List;

public record StudentTopicsImportResponse(
        int processedRows,
        int createdRows,
        int updatedRows,
        int skippedRows,
        List<StudentTopicsImportErrorResponse> errors
) {
}
