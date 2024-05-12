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
public class EmployeeServiceImplementation implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(employeeMapper::map).toList();
    }

    @Override
    public EmployeeResponse getEmployeeById(long id) {
        return employeeMapper.map(findByIdOrThrow(id));
    }

    @Override
    public EmployeeAdminResponse getEmployeeAdminById(long id) {
        return employeeMapper.mapAdmin(findByIdOrThrow(id));
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeCreateRequest employeeCreateRequest) {
        Employee employee = employeeRepository.save(
                employeeMapper.map(employeeCreateRequest));

        if (employee.isHasLessons()) {
            //todo: call parser to create lessons
        }
        return employeeMapper.map(employee);
    }

    @Override
    @Transactional
    public EmployeeAdminResponse updateEmployeeById(long id, EmployeeUpdateRequest employeeUpdateRequest) {
        //todo: handle parser lessons if hasLessons changed
        Employee employee = findByIdOrThrow(id);
        employee.updateFromRequest(employeeUpdateRequest);
        return employeeMapper.mapAdmin(
                employeeRepository.save(employee));
    }

    @Override
    public void deleteEmployeeById(long id) {
        employeeRepository.delete(findByIdOrThrow(id));
    }

    private Employee findByIdOrThrow(Long id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("По id: " + id + " не найдено ни одного сотрудника")
        );
    }
}
