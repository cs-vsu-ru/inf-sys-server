package vsu.cs.is.infsysserver.staticPage.adapter.rest.api;

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
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.request.StaticPageCreateRequest;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.request.StaticPageUpdateRequest;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.response.StaticPageResponse;

import java.util.Collection;

@Tag(name = "StaticPage API", description = "API для работы со статическими страницами")
public interface StaticPageApi {

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат всех статических страниц",
            content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = StaticPageResponse.class))
                    )
            }
    )
    @Operation(summary = "Отдает все статические страницы")
    ResponseEntity<Collection<StaticPageResponse>> getAllStaticPages();

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат статической страницы",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StaticPageResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует статическая страница по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Отдает статическую страницу по идентификатору")
    ResponseEntity<StaticPageResponse> getStaticPageById(
            @Parameter(description = "Идентификатор статической страницы") Long id
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное создание статической страницы",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StaticPageResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Создает статическую страницу из переданных параметров")
    ResponseEntity<StaticPageResponse> createStaticPage(
            @RequestBody(description = "Параметры для создания статической страницы")
            StaticPageCreateRequest staticPageCreateRequest);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление статической страницы",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StaticPageResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует статическая страница по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновляет статическую страницу по переданному идентификатору")
    ResponseEntity<StaticPageResponse> updateStaticPageById(
            @Parameter(description = "Уникальной идентификатор статической страницы") Long id,
            @RequestBody(description = "Параметры для обновления") StaticPageUpdateRequest staticPageUpdateRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное удаление статической страницы"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует статическая страница по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Удаляет статическую страницу по переданному идентификатору")
    ResponseEntity<Void> deleteStaticPageById(
            @Parameter(description = "Уникальный идентификатор удаляемой статической страницы") Long id
    );
}
