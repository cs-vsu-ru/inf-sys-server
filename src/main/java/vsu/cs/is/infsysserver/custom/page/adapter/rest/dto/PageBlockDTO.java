package vsu.cs.is.infsysserver.custom.page.adapter.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record PageBlockDTO(

        @Schema(description = "Тип блока (текстовый, объект, список объектов)", example = "LIST_OF_OBJECT")
        String type,
        @Schema(description = "Json описывающий тип полей объектов блока (по названию поля хранит его тип)")
        Map<String, String> elementsType,
        @Schema(description = "Флаг необходимости хранить картинку для объекта блока", example = "true")
        Boolean needsImage,
        @Schema(description = "Порядковый номер блока на странице", example = "1")
        Integer position,
        @Schema(description = "Список объектов блока")
        List<PageElementDTO> elements
) {
}
