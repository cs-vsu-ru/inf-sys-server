package vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record StaticPageResponse(

        @Schema(description = "Страница О кафедре", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentAbout,
        @Schema(description = "Страница Образование", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentEducation,
        @Schema(
                description = "Страница Студентам и выпускникам",
                example = "\"<p><strong>Заведующий кафедрой </strong>"
        )
        String contentStudents,
        @Schema(description = "Страница Партнеры", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentPartners,
        @Schema(
                description = "Страница Политика конфиденциальности",
                example = "\"<p><strong>Заведующий кафедрой </strong>"
        )
        String contentConfidential,
        @Schema(description = "Страница Адреса и контакты", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentContacts
) {
}
