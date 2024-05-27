package vsu.cs.is.infsysserver.staticpage.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record StaticPageCreateRequest(

        @Schema(description = "Страница О кафедре", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentAbout,
        @Schema(description = "Страница Образование", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentEducation,
        @Schema(
                description = "Страница Студентам",
                example = "\"<p><strong>Заведующий кафедрой </strong>"
        )
        String contentStudents,
        @Schema(description = "Страница Важное", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentImportant,
        @Schema(description = "Страница Консультации и экзамены", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentExams,
        @Schema(
                description = "Страница Политика конфиденциальности",
                example = "\"<p><strong>Заведующий кафедрой </strong>"
        )
        String contentConfidential,
        @Schema(description = "Страница Адреса и контакты", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentContacts,
        @Schema(description = "Страница Разное", example = "\"<p><strong>Заведующий кафедрой </strong>")
        String contentMiscellaneous
) {
}
