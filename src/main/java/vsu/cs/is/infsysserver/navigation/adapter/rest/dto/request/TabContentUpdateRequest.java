package vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record TabContentUpdateRequest(
        @Schema(description = "HTML-контент вкладки", example = "<p>Обновлённый текст</p>")
        String content
) {
}