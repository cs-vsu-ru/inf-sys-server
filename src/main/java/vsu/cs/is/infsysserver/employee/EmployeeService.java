package vsu.cs.is.infsysserver.employee;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.employee.adapter.EmployeeMapper;
import vsu.cs.is.infsysserver.employee.adapter.jpa.EmployeeRepository;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(employeeMapper::map).toList();
    }

    public EmployeeResponse getEmployeeById(long id) {
        return employeeMapper.map(findByIdOrThrow(id));
    }

    public EmployeeAdminResponse getEmployeeAdminById(long id) {
        return employeeMapper.mapAdmin(findByIdOrThrow(id));
    }

    public EmployeeResponse getEmployeeByEmail(String email) {
        return employeeMapper.map(employeeRepository.findByUserEmail(email).orElseThrow(
                () -> new EntityNotFoundException("По email: " + email + " не найдено ни одного сотрудника")
        ));
    }

    public EmployeeResponse createEmployee(EmployeeCreateRequest employeeCreateRequest) {
        Employee employee = employeeRepository.save(
                employeeMapper.map(employeeCreateRequest));

        if (employee.isHasLessons()) {
            //todo: call parser to create lessons
        }
        return employeeMapper.map(employee);
    }

    @Transactional
    public EmployeeAdminResponse updateEmployeeById(long id, EmployeeUpdateRequest employeeUpdateRequest) {
        //todo: handle parser lessons if hasLessons changed
        Employee employee = findByIdOrThrow(id);
        employee.updateFromRequest(employeeUpdateRequest);
        return employeeMapper.mapAdmin(
                employeeRepository.save(employee));
    }

    public void deleteEmployeeById(long id) {
        employeeRepository.delete(findByIdOrThrow(id));
    }

    private Employee findByIdOrThrow(Long id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("По id: " + id + " не найдено ни одного сотрудника")
        );
    }
}
