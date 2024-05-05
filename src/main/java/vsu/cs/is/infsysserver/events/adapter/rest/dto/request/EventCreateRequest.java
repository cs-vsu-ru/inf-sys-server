package vsu.cs.is.infsysserver.events.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record EventCreateRequest(

        @Schema(description = "Заголовок события", example = "Прием задолженностей")
        String title,

        @Schema(description = "Контент события", example = "Прием задолженностей по дисциплинам "
                + "кафедры информационных систем")
        String content,

        @Schema(description = "Дата и время начала события", example = "02.05.2024 13:00")
        Date startDateTime,

        @Schema(description = "Дата и время окончания события", example = "02.05.2024 14:30")
        Date endDateTime
) {
}
