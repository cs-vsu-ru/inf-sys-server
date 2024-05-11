package vsu.cs.is.infsysserver.employee;

import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> getAllEmployees();

    EmployeeResponse getEmployeeById(long id);

    EmployeeResponse createEmployee(EmployeeCreateRequest employeeCreateRequest);

    EmployeeResponse updateEmployeeById(long id, EmployeeUpdateRequest employeeUpdateRequest);

    void deleteEmployeeById(long id);
}
