package vsu.cs.is.infsysserver.upload.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vsu.cs.is.infsysserver.upload.UploadService;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/upload-file")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) {
        return ResponseEntity.ok().body(uploadService.uploadFile(file));
    }
}
