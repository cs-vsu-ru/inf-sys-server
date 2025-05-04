package vsu.cs.is.infsysserver.custom.page.adapter.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageDTO;
import vsu.cs.is.infsysserver.exception.message.ErrorMessage;

import java.util.List;

@Tag(name = "Custom Page API", description = "API для работы с кастомными вкладками")
public interface CustomPageApi {

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат всех вкладок",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = PageDTO.class)
                            )
                    )
            }
    )
    @Operation(summary = "Возваращает все кастомные вкладки")
    ResponseEntity<List<PageDTO>> getAllPages();

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат названий всех вкладок",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    @Operation(summary = "Возваращает названия всех кастомных вкладок")
    ResponseEntity<List<String>> getAllPageNames();

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат вкладки",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageDTO.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует вкладка с переданным name",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Возвращает кастомную вкладку по названию")
    ResponseEntity<PageDTO> getPageByName(
            @Parameter(description = "Название искомой вкладки") String name);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное создание вкладки",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageDTO.class)
                            )
                    }
            )
    })
    @Operation(summary = "Сохраняет переданную вкладку")
    ResponseEntity<PageDTO> createPage(
            @Parameter(description = "Созданная вкладка") PageDTO page);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление вкладки",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageDTO.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует вкладка с переданным названием",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновляет переданную вкладку")
    ResponseEntity<PageDTO> updatePageByName(
            @Parameter(description = "Обновляемая вкладка") PageDTO page);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное удаление вкладки"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует вкладка с переданным названием",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Удаляет вкладку с переданным названием")
    ResponseEntity<PageDTO> deletePageByName(
            @Parameter(description = "Название удаляемой вкладки") String name);
}
