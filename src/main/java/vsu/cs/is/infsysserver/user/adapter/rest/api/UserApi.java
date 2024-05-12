package vsu.cs.is.infsysserver.user.adapter.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

@Tag(name = "User API", description = "API для работы с аутентифицированным пользователем")
public interface UserApi {

    @ApiResponse(responseCode = "200",
            description = "Успешный возврат данных аутентифицированного пользователя",
            content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeResponse.class))
                    )
            }
    )
    @Operation(summary = "Отдает данные аутентифицированного пользователя")
    ResponseEntity<EmployeeResponse> getAccountInfo(@Parameter(description = "Аутентифицированный пользователь. " +
            "Не передается клиентом, а получется в контроллере на сервере") String authUserEmail);
}
