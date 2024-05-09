package vsu.cs.is.infsysserver.employee.adapter.rest;

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
import vsu.cs.is.infsysserver.exception.message.ErrorMessage;

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
    ResponseEntity<List<EmployeeResponse>> getAllEmployees();

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
                                    schema = @Schema(implementation = ErrorMessage.class)
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
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Возвращает сотрудника по ID")
    ResponseEntity<EmployeeAdminResponse> getAdminEmployeeById(
            @Parameter(description = "ID искомого сотрудника") Long id);

    ResponseEntity<EmployeeAdminResponse> getEmployeeAdminById(Long id);

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
            @Parameter(description = "Данные создаваемого сотрудника") EmployeeCreateRequest employeeCreateRequest);

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
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Обновляет сотрудника с переданным ID")
    ResponseEntity<EmployeeAdminResponse> updateEmployeeById(
            @Parameter(description = "ID обновляемого сотрудника") Long id,
            @Parameter(description = "Данные для обновления") EmployeeUpdateRequest employeeUpdateRequest);

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
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(summary = "Удаляет сотрудника с переданным ID")
    ResponseEntity<EmployeeResponse> deleteEmployeeById(
            @Parameter(description = "ID удаляемого сотрудника") Long id);
}
