package vsu.cs.is.infsysserver.student.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vsu.cs.is.infsysserver.security.entity.dto.response.StudentAuthenticationResponse;
import vsu.cs.is.infsysserver.security.service.AuthenticationService;
import vsu.cs.is.infsysserver.student.adapter.StudentService;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentEditRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentImportResponse;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;

@RequestMapping("/api")
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class StudentController {

    private final AuthenticationService authenticationService;
    private final StudentService studentService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/students")
    public java.util.List<StudentResponse> getAll() {
        return studentService.getAllStudents();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/students")
    public StudentAuthenticationResponse create(@RequestBody StudentRequest studentRequest) {
        return authenticationService.registerStudent(studentRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/students/{id}")
    public StudentResponse update(@PathVariable Long id,
                                  @RequestBody StudentEditRequest studentEditRequest) {

        return studentService.editStudent(id, studentEditRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/students/{id}")
    public void delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/students/{id}/disable")
    public void disable(@PathVariable Long id) {
        studentService.disableStudent(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/student/account")
    public StudentResponse getCurrentStudent() {
        return studentService.getCurrentStudent();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/students/import")
    public ResponseEntity<StudentImportResponse> importStudents(
            @RequestParam("file") MultipartFile file
    ) {
        StudentImportResponse response = studentService.importStudents(file);

        if (response.getCreated() == 0
                && response.getUpdated() == 0
                && !response.getErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
