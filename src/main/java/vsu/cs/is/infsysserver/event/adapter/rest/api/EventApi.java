package vsu.cs.is.infsysserver.event.adapter.rest.api;

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
import vsu.cs.is.infsysserver.event.adapter.rest.dto.request.EventCreateRequest;
import vsu.cs.is.infsysserver.event.adapter.rest.dto.request.EventUpdateRequest;
import vsu.cs.is.infsysserver.event.adapter.rest.dto.response.EventResponse;

import java.util.Collection;

@Tag(name = "Events API", description = "API для работы с событиями")
public interface EventApi {

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат всех событий",
            content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EventResponse.class))
                    )
            }
    )
    @Operation(summary = "Отдает все события")
    ResponseEntity<Collection<EventResponse>> getAllEvents();

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат события",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EventResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует событие по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Отдает событие по идентификатору")
    ResponseEntity<EventResponse> getEventById(@Parameter(description = "Идентификатор события") Long id);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное создание события",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EventResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Создает событие из переданных параметров")
    ResponseEntity<EventResponse> createEvent(
            @RequestBody(description = "Параметры для создания события") EventCreateRequest eventCreateRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление события",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EventResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует событие по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновляет новость по переданному идентификатору")
    ResponseEntity<EventResponse> updateEventById(
            @Parameter(description = "Уникальной идентификатор обновляемого события") Long id,
            @RequestBody(description = "Параметры для обновления") EventUpdateRequest eventUpdateRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное удаление события"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует событие по переданному идентификатору",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Удаляет событие по переданному идентификатору")
    ResponseEntity<Void> deleteEventById(
            @Parameter(description = "Уникальный идентификатор удаляемого события") Long id
    );
}
