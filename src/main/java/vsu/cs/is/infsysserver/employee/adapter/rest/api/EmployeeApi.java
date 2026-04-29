package vsu.cs.is.infsysserver.employee.adapter.rest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;
import vsu.cs.is.infsysserver.exception.dto.ErrorResponse;

import java.util.List;

@Tag(name = "Employee API", description = "API для работы с сотрудниками")
public interface EmployeeApi {

    @ApiResponse(
            responseCode = "200",
            description = "Успешный возврат всех сотрудников",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = EmployeeResponse.class)
                            )
                    )
            }
    )
    @Operation(summary = "Возваращает всех сотрудников")
    ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @Parameter(description = "Фильтр по активности: true — только активные (по умолчанию), "
                    + "false — только отключённые") boolean isActive);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат сотрудника",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует сотрудник с переданным ID",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Возвращает сотрудника по ID")
    ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "ID искомого сотрудника") Long id);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат сотрудника",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeAdminResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует сотрудник с переданным ID",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Возвращает сотрудника с админ-полями по ID")
    ResponseEntity<EmployeeAdminResponse> getEmployeeAdminById(
            @Parameter(description = "ID искомого сотрудника") Long id);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное создание сотрудника",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Создает сотрудника с переданными параметрами")
    ResponseEntity<EmployeeResponse> createEmployee(
            @Parameter(description = "Данные создаваемого сотрудника") EmployeeCreateRequest employeeCreateRequest,
            @Parameter(description = "Аутентифицированный пользователь. "
                    + "Не передается клиентом, а получется в контроллере на сервере") String authUserLogin);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление сотрудника",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeAdminResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует сотрудник с переданным ID",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновляет сотрудника с переданным ID")
    ResponseEntity<EmployeeAdminResponse> updateEmployeeById(
            @Parameter(description = "ID обновляемого сотрудника") Long id,
            @Parameter(description = "Данные для обновления") EmployeeUpdateRequest employeeUpdateRequest,
            @Parameter(description = "Аутентифицированный пользователь. "
                    + "Не передается клиентом, а получется в контроллере на сервере") String authUserLogin);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное удаление сотрудника"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует сотрудник с переданным ID",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Полностью удаляет сотрудника и связанного пользователя с переданным ID")
    ResponseEntity<EmployeeResponse> deleteEmployeeById(
            @Parameter(description = "ID удаляемого сотрудника") Long id);

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное отключение сотрудника"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отсутствует сотрудник с переданным ID",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    }
            )
    })
    @Operation(summary = "Отключает сотрудника с переданным ID (soft-delete без удаления пользователя)")
    ResponseEntity<EmployeeResponse> disableEmployeeById(
            @Parameter(description = "ID отключаемого сотрудника") Long id);
}
