package vsu.cs.is.infsysserver.custom.page.adapter.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record PageElementDTO(

        @Schema(description = "Json значение объекта блока")
        Map<String, String> value,
        @Schema(description = "Порядковый номер объекта в блоке", example = "1")
        Integer position
) {
}
