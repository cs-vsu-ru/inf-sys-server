package vsu.cs.is.infsysserver.student.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.security.entity.dto.response.StudentAuthenticationResponse;
import vsu.cs.is.infsysserver.security.service.AuthenticationService;
import vsu.cs.is.infsysserver.student.adapter.StudentService;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentEditRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import java.util.Optional;

@RequestMapping("/api")
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;
    private final AuthenticationService authenticationService;
    private final StudentService studentService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/students")
    public java.util.List<StudentResponse> getAll() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::new)
                .toList();
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
        studentRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        Optional<Student> optionalStudent = studentRepository.findById(id);

        return optionalStudent.map(student ->
                        ResponseEntity.ok(new StudentResponse(student)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/student/account")
    public StudentResponse getCurrentStudent() {
        return studentService.getCurrentStudent();
    }
}
