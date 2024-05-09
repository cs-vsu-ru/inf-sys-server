package vsu.cs.is.infsysserver.news.adapter.rest;

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
import vsu.cs.is.infsysserver.news.NewsService;
import vsu.cs.is.infsysserver.news.adapter.rest.api.NewsAPI;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.request.NewsCreateRequest;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.request.NewsUpdateRequest;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.response.NewsResponse;

import java.util.Collection;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RequestMapping("/api/news")
@RestController
@CrossOrigin
public class NewsController implements NewsAPI {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<Collection<NewsResponse>> getAllNews() {
        return ok(newsService.getAllNews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> getNewsById(@PathVariable Long id) {
        return ok(newsService.getNewsById(id));
    }

    @PostMapping
    public ResponseEntity<NewsResponse> createNews(@RequestBody NewsCreateRequest createRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newsService.createNews(createRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsResponse> updateNewsById(@PathVariable Long id,
                                                       @RequestBody NewsUpdateRequest updateRequest) {
        return ok(newsService.updateNewsById(id, updateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNewsById(@PathVariable Long id) {
        newsService.deleteNewsById(id);

        return ok().build();
    }
}
