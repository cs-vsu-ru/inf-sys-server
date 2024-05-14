package vsu.cs.is.infsysserver.employee.adapter.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;


@Builder
public record EmployeeResponse(
        @Schema(description = "ID сотрудника", example = "1")
        Long id,
        @Schema(description = "Имя сотрудника", example = "Иван")
        String firstName,
        @Schema(description = "Фамилия сотрудника", example = "Иванов")
        String lastName,
        @Schema(description = "Отчество сотрудника", example = "Иванович")
        String patronymic,
        @Schema(description = "Имя сотрудника", example = "Иван")
        String post,
        @Schema(description = "Имя сотрудника", example = "Иван")
        String academicTitle,
        @Schema(description = "", example = "Иван")
        String academicDegree,
        @Schema(description = "", example = "")
        String experience,
        @Schema(description = "", example = "")
        String professionalExperience,
        @Schema(description = "", example = "")
        String email,
        @Schema(description = "", example = "")
        String imageUrl,
        @Schema(description = "", example = "")
        Set<String> specialities,
        @Schema(description = "", example = "")
        String plan
) {
}
