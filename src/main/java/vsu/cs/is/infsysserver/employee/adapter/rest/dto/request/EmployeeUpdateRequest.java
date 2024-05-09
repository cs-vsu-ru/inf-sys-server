package vsu.cs.is.infsysserver.employee.adapter.rest.dto.request;

import java.time.LocalDate;

public record EmployeeUpdateRequest(
        Long id,
        String firstName,
        String lastName,
        String patronymic,
        String post,
        String academicTitle,
        String academicDegree,
        String experience,
        String professionalExperience,
        LocalDate dateOfBirth,
        String email,
        String imageUrl,
        String createdBy,
        String createdDate,
        String plan,
        boolean hasLessons
) {
}
