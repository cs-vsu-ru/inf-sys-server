package vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TabContentResponse(
        @Schema(description = "Идентификатор вкладки", example = "1")
        Long tabId,
        @Schema(description = "HTML-контент вкладки", example = "<p>Текст страницы</p>")
        String content
) {
}