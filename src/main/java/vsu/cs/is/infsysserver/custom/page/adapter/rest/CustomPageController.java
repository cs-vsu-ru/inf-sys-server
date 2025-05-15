package vsu.cs.is.infsysserver.custom.page.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.custom.page.CustomPageService;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.api.CustomPageApi;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageDTO;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class CustomPageController implements CustomPageApi {
    private final CustomPageService customPageService;

    @Override
    @GetMapping
    public ResponseEntity<List<PageDTO>> getAllPages() {
        return customPageService.getAllPages();
    }

    @Override
    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllPageNames() {
        return customPageService.getAllPageNames();
    }

    @Override
    @GetMapping("/{name}")
    public ResponseEntity<PageDTO> getPageByName(@PathVariable String name) {
        return customPageService.getPageByName(name);
    }

    @Override
    @PostMapping
    public ResponseEntity<PageDTO> createPage(@RequestBody PageDTO page) {
        return customPageService.createPage(page);
    }

    @Override
    @PatchMapping
    public ResponseEntity<PageDTO> updatePageByName(@RequestBody PageDTO page) {
        return customPageService.updatePageByName(page);
    }

    @Override
    @DeleteMapping("/{name}")
    public ResponseEntity<PageDTO> deletePageByName(@PathVariable String name) {
        customPageService.deletePageByName(name);
        return ok().build();
    }
}
