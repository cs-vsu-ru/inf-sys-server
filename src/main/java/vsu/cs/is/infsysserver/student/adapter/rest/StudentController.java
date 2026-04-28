package vsu.cs.is.infsysserver.student.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vsu.cs.is.infsysserver.security.entity.dto.response.StudentAuthenticationResponse;
import vsu.cs.is.infsysserver.security.service.AuthenticationService;
import vsu.cs.is.infsysserver.security.service.JwtService;
import vsu.cs.is.infsysserver.student.adapter.StudentService;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentEditRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentImportResponse;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.List;
import java.util.Optional;

@RequestMapping("/api")
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;
    private final AuthenticationService authenticationService;
    private final StudentService studentService;
    private final JwtService jwtService;

    @GetMapping("/students")
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::new)
                .toList();
    }

    @PostMapping("/students")
    public StudentAuthenticationResponse create(@RequestBody StudentRequest studentRequest) {
        return authenticationService.registerStudent(studentRequest);
    }

    @PutMapping("/students/{id}")
    public StudentResponse update(@PathVariable Long id,
                                  @RequestBody StudentEditRequest studentEditRequest) {

        return studentService.editStudent(id, studentEditRequest);
    }

    @DeleteMapping("/students/{id}")
    public void delete(@PathVariable Long id) {
        studentRepository.deleteById(id);
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        Optional<Student> optionalStudent = studentRepository.findById(id);

        return optionalStudent.map(student ->
                        ResponseEntity.ok(new StudentResponse(student)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/student/account")
    public StudentResponse getCurrentStudent() {
        return studentService.getCurrentStudent();
    }

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
