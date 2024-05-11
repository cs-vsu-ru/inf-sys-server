package vsu.cs.is.infsysserver.slider.adapter.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.ResponseEntity;
import vsu.cs.is.infsysserver.slider.adapter.rest.dto.request.SliderElementCreateRequest;
import vsu.cs.is.infsysserver.slider.adapter.rest.dto.request.SliderElementUpdateRequest;
import vsu.cs.is.infsysserver.slider.adapter.rest.dto.response.SliderElementResponse;

import java.util.Collection;

@Tag(name = "Slider API", description = "API для работы со слайдером на главной странице")
public interface SliderApi {

    @ApiResponse(responseCode = "200",
            description = "Успешный возврат всех элементов слайдера",
            content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SliderElementResponse.class))
                    )
            }
    )
    @Operation(summary = "Отдает все элементы слайдера")
    ResponseEntity<Collection<SliderElementResponse>> getSlider();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Успешный возврат элемента слайдера",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SliderElementResponse.class))
                    }
            ),
            @ApiResponse(responseCode = "404",
                    description = "Отсутствует элемент слайдера по переданному идентификатору",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class))
                    }
            )
    })
    @Operation(summary = "Отдает элемент слайдера по идентификатору")
    ResponseEntity<SliderElementResponse> getSliderElementById(
            @Parameter(description = "Идентификатор элемента слайдера") Long id);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Успешное создание элемента слайдера",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SliderElementResponse.class))
                    }
            )
    })
    @Operation(summary = "Создает элемент слайдера из переданных параметров")
    ResponseEntity<SliderElementResponse> createSliderElement(
            @RequestBody(description = "Параметры для создания элемента слайдера'")
            SliderElementCreateRequest sliderElementCreateRequest
    );

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Успешное обновление элемента слайдера",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SliderElementResponse.class))
                    }
            ),
            @ApiResponse(responseCode = "404",
                    description = "Отсутствует элемент слайдера по переданному идентификатору",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class))
                    }
            )
    })
    @Operation(summary = "Обновляет элемент слайдера по переданному идентификатору")
    ResponseEntity<SliderElementResponse> updateSliderElementById(
            @Parameter(description = "Уникальной идентификатор обновляемого элемента слайдера") Long id,
            @RequestBody(description = "Параметры для обновления элемента слайдера")
            SliderElementUpdateRequest sliderElementUpdateRequest);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Успешное удаление элемента слайдера"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Отсутствует элемент слайдера по переданному идентификатору",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class))
                    }
            )
    })
    @Operation(summary = "Удаляет элемент слайдера по переданному идентификатору")
    ResponseEntity<Void> deleteSliderElementById(
            @Parameter(description = "Уникальный идентификатор элемента слайдера") Long id
    );

}
