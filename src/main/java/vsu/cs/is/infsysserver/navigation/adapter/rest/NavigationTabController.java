package vsu.cs.is.infsysserver.navigation.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.navigation.NavigationTabService;
import vsu.cs.is.infsysserver.navigation.adapter.rest.api.NavigationTabAPI;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabCreateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabReorderRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabUpdateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.TabContentUpdateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response.NavigationTabResponse;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response.TabContentResponse;

import java.util.Collection;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RequestMapping("/api/tabs")
@RestController
public class NavigationTabController implements NavigationTabAPI {

    private final NavigationTabService navigationTabService;

    @GetMapping
    public ResponseEntity<Collection<NavigationTabResponse>> getAllTabs() {
        return ok(navigationTabService.getAllTabs());
    }

    @GetMapping("/visible")
    public ResponseEntity<Collection<NavigationTabResponse>> getVisibleTabs() {
        return ok(navigationTabService.getVisibleTabs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NavigationTabResponse> getTabById(@PathVariable Long id) {
        return ok(navigationTabService.getTabById(id));
    }

    @PostMapping
    public ResponseEntity<NavigationTabResponse> createTab(
            @RequestBody NavigationTabCreateRequest createRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(navigationTabService.createTab(createRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NavigationTabResponse> updateTabById(
            @PathVariable Long id,
            @RequestBody NavigationTabUpdateRequest updateRequest) {
        return ok(navigationTabService.updateTabById(id, updateRequest));
    }

    @PutMapping("/reorder")
    public ResponseEntity<Collection<NavigationTabResponse>> reorderTabs(
            @RequestBody NavigationTabReorderRequest reorderRequest) {
        return ok(navigationTabService.reorderTabs(reorderRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTabById(@PathVariable Long id) {
        navigationTabService.deleteTabById(id);
        return ok().build();
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<TabContentResponse> getTabContent(@PathVariable Long id) {
        return ok(navigationTabService.getTabContent(id));
    }

    @PutMapping("/{id}/content")
    public ResponseEntity<TabContentResponse> updateTabContent(
            @PathVariable Long id,
            @RequestBody TabContentUpdateRequest request) {
        return ok(navigationTabService.updateTabContent(id, request));
    }
}
