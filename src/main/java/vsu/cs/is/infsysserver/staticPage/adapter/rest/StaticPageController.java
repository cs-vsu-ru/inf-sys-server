package vsu.cs.is.infsysserver.staticPage.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.staticPage.StaticPageService;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.api.StaticPageApi;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.request.StaticPageCreateRequest;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.request.StaticPageUpdateRequest;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.response.StaticPageResponse;

import java.util.Collection;

import static org.springframework.http.ResponseEntity.ok;

@RequestMapping("/api/static-pages")
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class StaticPageController implements StaticPageApi {

    private final StaticPageService staticPageService;


    @Override
    @GetMapping
    public ResponseEntity<Collection<StaticPageResponse>> getAllStaticPages() {
        return ok(staticPageService.getAllStaticPages());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<StaticPageResponse> getStaticPageById(@PathVariable Long id) {
        return ok(staticPageService.getStaticPageById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<StaticPageResponse> createStaticPage(
            @RequestBody StaticPageCreateRequest staticPageCreateRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(staticPageService.createStaticPage(staticPageCreateRequest));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<StaticPageResponse> updateStaticPageById(
            @PathVariable Long id,
            @RequestBody StaticPageUpdateRequest staticPageUpdateRequest
    ) {
        return ok(staticPageService.updateStaticPageById(id, staticPageUpdateRequest));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaticPageById(@PathVariable Long id) {
        staticPageService.deleteStaticPageById(id);

        return ok().build();
    }
}
