package vsu.cs.is.infsysserver.employee;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vsu.cs.is.infsysserver.configuration.properties.ApplicationProperties;
import vsu.cs.is.infsysserver.employee.adapter.EmployeeMapper;
import vsu.cs.is.infsysserver.employee.adapter.jpa.EmployeeRepository;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.ParserEmployeeRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;
import vsu.cs.is.infsysserver.security.util.UserMapper;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    private final RestTemplate restTemplate;
    private final ApplicationProperties properties;

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAllActiveEmployees().stream().map(employeeMapper::map).toList();
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
        employee.setCreatedBy(findByLoginOrThrow(authUserLogin).getUser());
        employee.setUser(user);
        employee = employeeRepository.save(employee);

        if (employee.isHasLessons()) {
            doLessonsOperationForEmployee(LessonsOperation.CREATE_EMPTY, employee);
        }
        return employeeMapper.map(employee);
    }

    @Transactional
    public EmployeeAdminResponse updateEmployeeById(long id, EmployeeUpdateRequest employeeUpdateRequest,
                                                    String authUserLogin) {
        Employee employee = findByIdOrThrow(id);
        if (!employeeUpdateRequest.isPlanUpdate()) {
            if (!employee.isHasLessons() && employeeUpdateRequest.hasLessons()) {
                doLessonsOperationForEmployee(LessonsOperation.CREATE_EMPTY, employee);
            } else if (employee.isHasLessons() && !employeeUpdateRequest.hasLessons()) {
                doLessonsOperationForEmployee(LessonsOperation.DELETE, employee);
            }
            employee.updateFromRequest(employeeUpdateRequest, findByLoginOrThrow(authUserLogin).getUser());
        } else employee.setPlan(employeeUpdateRequest.plan());
        return employeeMapper.mapAdmin(
                employeeRepository.save(employee));
    }

    public void deleteEmployeeById(long id) {
        Employee employee = findByIdOrThrow(id);
        if (employee.isHasLessons()) {
            try {
                deleteEmployeeLessons(employee);
            } catch (Exception e) {
                log.error("lessons deletion failed", e);
                throw e;
            }
        }
        employee.setDisabled(true);
        employeeRepository.save(employee);
    }

    private Employee findByIdOrThrow(Long id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("По id: " + id + " не найдено ни одного сотрудника")
        );
    }

    private void doLessonsOperationForEmployee(LessonsOperation operation, Employee employee) {
        ParserEmployeeRequest request = new ParserEmployeeRequest(employee.getId());
        log.info("Employee id from method: " + request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Host", "parser_api:8000");
        headers.add("Content-Type", "application/json");

        HttpEntity<ParserEmployeeRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                properties.services().get("parser").baseUrl() + "/employees/"
                        + operation.getUrlPart(), entity, Void.class);
        response.getStatusCode();
    }

    private Employee findByLoginOrThrow(String login) {
        return employeeRepository.findByUserLogin(login).orElseThrow(
                () -> new EntityNotFoundException("По логину: " + login + " не найдено ни одного сотрудника")
        );
    }

    private void deleteEmployeeLessons(Employee employee) {
        doLessonsOperationForEmployee(LessonsOperation.CREATE_EMPTY, employee);
        doLessonsOperationForEmployee(LessonsOperation.DELETE, employee);
    }
}
