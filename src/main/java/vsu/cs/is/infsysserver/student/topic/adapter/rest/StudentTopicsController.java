package vsu.cs.is.infsysserver.student.topic.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vsu.cs.is.infsysserver.student.topic.StudentTopicsService;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.request.StudentTopicsImportUrlRequest;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response.StudentTopicsImportResponse;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response.StudentTopicsResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentTopicsController {

    private final StudentTopicsService studentTopicsService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/students/topics/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentTopicsImportResponse importTopics(@RequestParam("file") MultipartFile file) {
        return studentTopicsService.importFile(file);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/students/topics/import/google-sheet")
    public StudentTopicsImportResponse importTopicsFromGoogleSheet(
            @RequestBody StudentTopicsImportUrlRequest request
    ) {
        return studentTopicsService.importGoogleSheet(request.url());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/students/topics/{studentLogin}")
    public ResponseEntity<StudentTopicsResponse> getTopicsByStudentLogin(@PathVariable String studentLogin) {
        return studentTopicsService.getTopicsByStudentLogin(studentLogin)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/student/account/topics")
    public ResponseEntity<StudentTopicsResponse> getCurrentStudentTopics() {
        return studentTopicsService.getCurrentStudentTopics()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
