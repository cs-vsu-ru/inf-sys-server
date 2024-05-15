package vsu.cs.is.infsysserver.employee.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import vsu.cs.is.infsysserver.security.entity.temp.Role;

import java.util.Date;

public record EmployeeCreateRequest(

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
        Date experience,
        @Schema(description = "Дата начала профессионального опыта", example = "2010-01-01")
        Date professionalExperience,
        @Schema(description = "Электронная почта", example = "kudinov_im@cs.vsu.ru")
        String email,
        @Schema(description = "Логин пользователя", example = "kudinov_i_m")
        String login,
        @Schema(description = "Ссылка на фото", example = "https://i.imgur.com/fn32s4s.jpeg")
        String imageUrl,
        @Schema(description = "Ссылка на индивидуальный план работы преподавателя", example
                = "http://www.cs.vsu.ru:80/is/api/files/52d649cf-65dc-4834-962c-72fbec74f396Зуев.pdf")
        String plan,
        @Schema(description = "Проводит ли занятия", example = "false")
        Boolean hasLessons,
        @Schema(description = "Роль пользователя", example = "MODERATOR")
        Role role
) {
}
