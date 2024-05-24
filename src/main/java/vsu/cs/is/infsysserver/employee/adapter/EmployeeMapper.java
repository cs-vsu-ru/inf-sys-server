package vsu.cs.is.infsysserver.employee.adapter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

@Mapper
public interface EmployeeMapper {

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.role", target = "mainRole")
    EmployeeResponse map(Employee employee);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "user.role", target = "mainRole")
    EmployeeAdminResponse mapAdmin(Employee employee);

    Employee map(EmployeeCreateRequest employeeCreateRequest);
}
