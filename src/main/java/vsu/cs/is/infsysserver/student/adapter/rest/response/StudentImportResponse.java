package vsu.cs.is.infsysserver.student.adapter.rest.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudentImportResponse {

    private int created;
    private int updated;
    private int skipped;
    private List<StudentImportError> errors = new ArrayList<>();

    public void incrementCreated() {
        created++;
    }

    public void incrementUpdated() {
        updated++;
    }

    public void incrementSkipped() {
        skipped++;
    }

    public void addError(int row, String field, String message) {
        errors.add(new StudentImportError(row, field, message));
    }

    @Data
    @AllArgsConstructor
    public static class StudentImportError {
        private int row;
        private String field;
        private String message;
    }
}