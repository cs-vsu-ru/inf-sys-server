package vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record NavigationTabReorderItem(
        @Schema(description = "Идентификатор вкладки", example = "1")
        Long id,
        @Schema(description = "Новый порядок отображения", example = "3")
        Integer sortOrder
) {
}
