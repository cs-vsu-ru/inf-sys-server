package vsu.cs.is.infsysserver.employee.adapter.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import vsu.cs.is.infsysserver.security.entity.temp.Role;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record EmployeeResponse(

        @Schema(description = "ID сотрудника", example = "1")
        Long id,
        @Schema(description = "Имя сотрудника", example = "Иван")
        String firstName,
        @Schema(description = "Фамилия сотрудника", example = "Кудинов")
        String lastName,
        @Schema(description = "Отчество сотрудника", example = "Михайлович")
        String patronymic,
        @Schema(description = "Должность", example = "Старший преподаватель")
        String post,
        @Schema(description = "Ученое звание", example = "Доцент")
        String academicTitle,
        @Schema(description = "Ученая степень", example = "Кандидат наук")
        String academicDegree,
        @Schema(description = "Дата начала опыта", example = "2005-01-01")
        LocalDate experience,
        @Schema(description = "Дата начала профессионального опыта", example = "2010-01-01")
        LocalDate professionalExperience,
        @Schema(description = "Электронная почта", example = "kudinov_im@cs.vsu.ru")
        String email,
        @Schema(description = "Ссылка на фото", example = "https://i.imgur.com/fn32s4s.jpeg")
        String imageUrl,
        @Schema(description = "Ссылка на индивидуальный план работы преподавателя", example
                = "http://www.cs.vsu.ru:80/is/api/files/52d649cf-65dc-4834-962c-72fbec74f396Зуев.pdf")
        String plan,
        @Schema(description = "Список специальностей")
        Set<String> specialities,
        @Schema(description = "Проводит ли занятия", example = "false")
        Boolean hasLessons,
        @Schema(description = "Роль пользователя", example = "MODERATOR")
        Role role
) {
}
