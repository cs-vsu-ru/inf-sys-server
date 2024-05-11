package vsu.cs.is.infsysserver.news.adapter.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import vsu.cs.is.infsysserver.exception.message.ErrorMessage;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.request.NewsCreateRequest;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.request.NewsUpdateRequest;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.response.NewsResponse;

import java.util.Collection;

@Tag(name = "News API", description = "API для работы с новостями")
public interface NewsAPI {

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат всех новостей",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = NewsResponse.class)
                            )
                    )
            }
    )
    @Operation(summary = "Отдает все новости")
    ResponseEntity<Collection<NewsResponse>> getAllNews();

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат новости",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NewsResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствие новости по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Отдает новость по идентификатору")
    ResponseEntity<NewsResponse> getNewsById(@Parameter(description = "Идентификатор новости") Long id);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное создание новости",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NewsResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Создает новость по переданной дто")
    ResponseEntity<NewsResponse> createNews(
            @RequestBody(description = "DTO для создания новости") NewsCreateRequest createRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление новости",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NewsResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствие новости по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновляет новость по переданному идентификатору")
    ResponseEntity<NewsResponse> updateNewsById(
            @Parameter(description = "id обновляемой новости") Long id,
            @RequestBody(description = "DTO с обновленными параметрами") NewsUpdateRequest updateRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное удаление новости"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствие новости по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Удаляет новость по переданному идентификатору")
    ResponseEntity<Void> deleteNewsById(
            @Parameter(description = "Идентификатор удаляемой новости") Long id
    );
}
