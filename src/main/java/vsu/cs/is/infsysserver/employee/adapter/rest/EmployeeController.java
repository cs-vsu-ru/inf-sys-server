package vsu.cs.is.infsysserver.employee.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.employee.EmployeeServiceMock;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin
public class EmployeeController implements EmployeeApi {
    private final EmployeeServiceMock employeeService;

    @Override
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    @GetMapping("/admin/{id}")
    public ResponseEntity<EmployeeAdminResponse> getEmployeeAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(EmployeeAdminResponse.builder().build());
    }

    @Override
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeCreateRequest employeeCreateRequest) {
        return ResponseEntity.ok(employeeService.createEmployee(employeeCreateRequest));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeAdminResponse> updateEmployeeById(
            @PathVariable Long id,
            @RequestBody EmployeeUpdateRequest employeeUpdateRequest
    ) {
        return ResponseEntity.ok(EmployeeAdminResponse.builder().build());
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeResponse> deleteEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
