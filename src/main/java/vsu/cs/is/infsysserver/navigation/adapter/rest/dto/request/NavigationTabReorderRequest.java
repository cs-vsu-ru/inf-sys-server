package vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Запрос на изменение порядка вкладок")
public record NavigationTabReorderRequest(
        @Schema(description = "Список вкладок с новым порядком")
        List<NavigationTabReorderItem> items
) {
}
