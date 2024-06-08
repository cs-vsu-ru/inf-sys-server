package vsu.cs.is.infsysserver.employee.adapter.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParserEmployeeRequest(
        @JsonProperty("employee_id")
        Long employeeId
) {
}
