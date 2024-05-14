package vsu.cs.is.infsysserver.event.adapter.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record EventResponse(

        @Schema(description = "Идентификатор события", example = "1")
        Long id,

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
