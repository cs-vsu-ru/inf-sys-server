package vsu.cs.is.infsysserver.slider.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SliderElementCreateRequest(
        @Schema(description = "Заголовок элемента слайдера", example = "Лаборатория электротехники, "
                + "электроники и схемотехники")
        String title,
        @Schema(description = "Описание элемента слайдера", example = "Описание")
        String description,
        @Schema(description = "Ссылка на изображение элемента слайдера", example = "https://cs.vsu.ru/image/123")
        String imageURL,
        @Schema(description = "Ссылка на страницу")
        String urlTo
) {
}
