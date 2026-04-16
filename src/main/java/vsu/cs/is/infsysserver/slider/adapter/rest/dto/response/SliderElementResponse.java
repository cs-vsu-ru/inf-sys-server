package vsu.cs.is.infsysserver.slider.adapter.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SliderElementResponse(
        @Schema(description = "Идентификатор элемента слайдера'", example = "1")
        Long id,
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
