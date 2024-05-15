package vsu.cs.is.infsysserver.employee.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.employee.EmployeeService;
import vsu.cs.is.infsysserver.employee.adapter.rest.api.EmployeeApi;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin
public class EmployeeController implements EmployeeApi {
    private final EmployeeService employeeService;

    @Override
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ok(employeeService.getAllEmployees());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ok(employeeService.getEmployeeById(id));
    }

    @Override
    @GetMapping("/admin/{id}")
    public ResponseEntity<EmployeeAdminResponse> getEmployeeAdminById(@PathVariable Long id) {
        return ok(employeeService.getEmployeeAdminById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @RequestBody EmployeeCreateRequest employeeCreateRequest,
            @AuthenticationPrincipal String authUserEmail) {
        return ok(employeeService.createEmployee(employeeCreateRequest, authUserEmail));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeAdminResponse> updateEmployeeById(
            @PathVariable Long id,
            @RequestBody EmployeeUpdateRequest employeeUpdateRequest,
            @AuthenticationPrincipal String authUserEmail
    ) {
        return ok(employeeService.updateEmployeeById(id, employeeUpdateRequest, authUserEmail));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeResponse> deleteEmployeeById(@PathVariable Long id) {
        employeeService.deleteEmployeeById(id);
        return ok().build();
    }
}
