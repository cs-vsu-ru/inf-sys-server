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
import vsu.cs.is.infsysserver.security.util.UserMapper;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    private final String parserUrl = "";

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(employeeMapper::map).toList();
    }

    public EmployeeResponse getEmployeeById(long id) {
        return employeeMapper.map(findByIdOrThrow(id));
    }

    public EmployeeAdminResponse getEmployeeAdminById(long id) {
        return employeeMapper.mapAdmin(findByIdOrThrow(id));
    }

    public EmployeeResponse getEmployeeByLogin(String login) {
        return employeeMapper.map(findByLoginOrThrow(login));
    }

    public EmployeeResponse createEmployee(EmployeeCreateRequest employeeCreateRequest, String authUserLogin) {
        User user = UserMapper.mapEmployeeCreateRequestToUser(employeeCreateRequest);
        user = userRepository.save(user);

        Employee employee = employeeMapper.map(employeeCreateRequest);
        employee.setLastModifiedBy(findByLoginOrThrow(authUserLogin).getUser());
        employee.setUser(user);
        employee = employeeRepository.save(employee);

        if (employee.isHasLessons()) {
            //todo: call parser to create lessons
        }
        return employeeMapper.map(employee);
    }

    @Transactional
    public EmployeeAdminResponse updateEmployeeById(long id, EmployeeUpdateRequest employeeUpdateRequest,
                                                    String authUserLogin) {
        //todo: handle parser lessons if hasLessons changed
        Employee employee = findByIdOrThrow(id);
        employee.updateFromRequest(employeeUpdateRequest, findByLoginOrThrow(authUserLogin).getUser());
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

    private Employee findByLoginOrThrow(String login) {
        return employeeRepository.findByUserLogin(login).orElseThrow(
                () -> new EntityNotFoundException("По логину: " + login + " не найдено ни одного сотрудника")
        );
    }
}
