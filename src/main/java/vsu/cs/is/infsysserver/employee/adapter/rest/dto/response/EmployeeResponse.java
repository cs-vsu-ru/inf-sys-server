package vsu.cs.is.infsysserver.employee.adapter.rest.dto.response;

import lombok.Builder;

import java.util.Set;


@Builder
public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String patronymic,
        String post,
        String academicTitle,
        String academicDegree,
        String experience,
        String professionalExperience,
        String email,
        String imageUrl,
        Set<String> specialities,
        String plan
) {
}
