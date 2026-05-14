package vsu.cs.is.infsysserver.student.nir.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vsu.cs.is.infsysserver.student.nir.StudentNirImportService;
import vsu.cs.is.infsysserver.student.nir.adapter.rest.dto.response.StudentNirImportResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentNirController {

    private final StudentNirImportService studentNirImportService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/students/nir/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentNirImportResponse importNir(@RequestParam("file") MultipartFile file) {
        return studentNirImportService.importFile(file);
    }
}
