package vsu.cs.is.infsysserver.student.nir.adapter.rest.dto.response;

public record StudentNirImportErrorResponse(
        int rowNumber,
        String sheetName,
        String studentFullName,
        String message
) {
}
