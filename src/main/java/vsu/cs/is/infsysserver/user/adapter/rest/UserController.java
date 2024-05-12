package vsu.cs.is.infsysserver.user.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.employee.EmployeeServiceImplementation;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;
import vsu.cs.is.infsysserver.user.adapter.rest.api.UserApi;

import static org.springframework.http.ResponseEntity.ok;

@RequestMapping("/api/account")
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class UserController implements UserApi {

    private final EmployeeServiceImplementation employeeService;

    @Override
    @GetMapping
    public ResponseEntity<EmployeeResponse> getAccountInfo(@AuthenticationPrincipal String authUserEmail) {
        return ok(employeeService.getEmployeeByEmail(authUserEmail));
    }
}
