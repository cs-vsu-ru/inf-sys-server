package vsu.cs.is.infsysserver.navigation.adapter.rest.api;

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
import vsu.cs.is.infsysserver.exception.dto.ErrorResponse;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabCreateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabReorderRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabUpdateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.TabContentUpdateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response.NavigationTabResponse;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response.TabContentResponse;

import java.util.Collection;

@Tag(name = "Navigation Tabs API", description = "API для управления вкладками навигации")
public interface NavigationTabAPI {

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат всех вкладок навигации",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = NavigationTabResponse.class)
                            )
                    )
            }
    )
    @Operation(summary = "Отдает все вкладки навигации, отсортированные по порядку")
    ResponseEntity<Collection<NavigationTabResponse>> getAllTabs();

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат видимых вкладок навигации",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = NavigationTabResponse.class)
                            )
                    )
            }
    )
    @Operation(summary = "Отдает только видимые вкладки навигации для отображения в шапке сайта")
    ResponseEntity<Collection<NavigationTabResponse>> getVisibleTabs();

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат вкладки",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NavigationTabResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Вкладка по переданному идентификатору не найдена",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Отдает вкладку навигации по идентификатору")
    ResponseEntity<NavigationTabResponse> getTabById(
            @Parameter(description = "Идентификатор вкладки") Long id
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Успешное создание вкладки навигации",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NavigationTabResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Создает вкладку навигации")
    ResponseEntity<NavigationTabResponse> createTab(
            @RequestBody(description = "DTO для создания вкладки")
            NavigationTabCreateRequest createRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление вкладки навигации",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NavigationTabResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Вкладка по переданному идентификатору не найдена",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновляет вкладку навигации по идентификатору")
    ResponseEntity<NavigationTabResponse> updateTabById(
            @Parameter(description = "Идентификатор обновляемой вкладки") Long id,
            @RequestBody(description = "DTO с обновленными параметрами")
            NavigationTabUpdateRequest updateRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление порядка вкладок",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(
                                                    implementation = NavigationTabResponse.class
                                            )
                                    )
                            )
                    }
            )
    })
    @Operation(summary = "Массовое обновление порядка вкладок (drag-and-drop)")
    ResponseEntity<Collection<NavigationTabResponse>> reorderTabs(
            @RequestBody(description = "Список вкладок с новым порядком")
            NavigationTabReorderRequest reorderRequest
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное удаление вкладки навигации"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Вкладка по переданному идентификатору не найдена",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Удаляет вкладку навигации по идентификатору")
    ResponseEntity<Void> deleteTabById(
            @Parameter(description = "Идентификатор удаляемой вкладки") Long id
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контент вкладки успешно получен",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = TabContentResponse.class
                                    )
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Вкладка навигации не найдена",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Получить контент вкладки навигации по её идентификатору")
    ResponseEntity<TabContentResponse> getTabContent(
            @Parameter(description = "Идентификатор вкладки навигации", required = true)
            Long id
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контент вкладки успешно обновлён",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = TabContentResponse.class
                                    )
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Вкладка навигации не найдена",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновить контент вкладки навигации")
    ResponseEntity<TabContentResponse> updateTabContent(
            @Parameter(description = "Идентификатор вкладки навигации", required = true)
            Long id,
            @RequestBody(description = "DTO с обновлённым контентом")
            TabContentUpdateRequest request
    );
}
