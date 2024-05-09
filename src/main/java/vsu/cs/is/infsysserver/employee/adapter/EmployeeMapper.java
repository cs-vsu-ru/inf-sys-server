package vsu.cs.is.infsysserver.employee.adapter;

import org.mapstruct.Mapper;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

@Mapper
public interface EmployeeMapper {
    Employee map(EmployeeResponse employeeResponse);

    EmployeeResponse map(Employee employee);

    Employee map(EmployeeCreateRequest employeeCreateRequest);

    Employee map(EmployeeUpdateRequest employeeUpdateRequest);
}
