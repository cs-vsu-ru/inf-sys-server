package vsu.cs.is.infsysserver.employee;

import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponse> getAllEmployees();

    EmployeeResponse getEmployeeById(long id);

    EmployeeAdminResponse getEmployeeAdminById(long id);

    EmployeeResponse getEmployeeByEmail(String email);

    EmployeeResponse createEmployee(EmployeeCreateRequest employeeCreateRequest);

    EmployeeAdminResponse updateEmployeeById(long id, EmployeeUpdateRequest employeeUpdateRequest);

    void deleteEmployeeById(long id);

}
