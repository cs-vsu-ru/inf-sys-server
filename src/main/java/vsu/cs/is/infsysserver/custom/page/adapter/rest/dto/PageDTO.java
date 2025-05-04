package vsu.cs.is.infsysserver.custom.page.adapter.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PageDTO(

        @Schema(description = "Название кастомной вкладки", example = "Студенты")
        String name,
        @Schema(description = "Заголовок кастомной вкладки", example = "Список студентов")
        String title,
        @Schema(description = "Список блоков вкладки")
        List<PageBlockDTO> blocks
) {
}
