package vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record NavigationTabUpdateRequest(
        @Schema(description = "Название вкладки", example = "О кафедре")
        String name,
        @Schema(description = "URL вкладки", example = "/about")
        String url,
        @Schema(description = "Порядок отображения", example = "1")
        Integer sortOrder,
        @Schema(description = "Видимость вкладки", example = "true")
        Boolean visible
) {
}
