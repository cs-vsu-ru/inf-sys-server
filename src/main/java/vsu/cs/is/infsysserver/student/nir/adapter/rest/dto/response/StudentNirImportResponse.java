package vsu.cs.is.infsysserver.student.nir.adapter.rest.dto.response;

import java.util.List;

public record StudentNirImportResponse(
        int processedRows,
        int createdRows,
        int updatedRows,
        int skippedRows,
        List<StudentNirImportErrorResponse> errors
) {
}
