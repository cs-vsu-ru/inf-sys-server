package vsu.cs.is.infsysserver.events.adapter.rest;

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
import vsu.cs.is.infsysserver.events.EventService;
import vsu.cs.is.infsysserver.events.adapter.rest.api.EventApi;
import vsu.cs.is.infsysserver.events.adapter.rest.dto.request.EventCreateRequest;
import vsu.cs.is.infsysserver.events.adapter.rest.dto.request.EventUpdateRequest;
import vsu.cs.is.infsysserver.events.adapter.rest.dto.response.EventResponse;

import java.util.Collection;

import static org.springframework.http.ResponseEntity.ok;

@RequestMapping("/api/events")
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class EventController implements EventApi {

    private final EventService eventService;

    @Override
    @GetMapping
    public ResponseEntity<Collection<EventResponse>> getAllEvents() {
        return ok(eventService.getAllEvents());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ok(eventService.getEventById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventCreateRequest eventCreateRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.createEvent(eventCreateRequest));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEventById(@PathVariable Long id,
                                                         @RequestBody EventUpdateRequest eventUpdateRequest) {
        return ok(eventService.updateEventById(id, eventUpdateRequest));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@PathVariable Long id) {
        eventService.deleteEventById(id);

        return ok().build();
    }
}
