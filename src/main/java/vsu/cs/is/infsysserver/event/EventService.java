package vsu.cs.is.infsysserver.event;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.event.adapter.EventMapper;
import vsu.cs.is.infsysserver.event.adapter.jpa.EventRepository;
import vsu.cs.is.infsysserver.event.adapter.jpa.entity.Event;
import vsu.cs.is.infsysserver.event.adapter.rest.dto.request.EventCreateRequest;
import vsu.cs.is.infsysserver.event.adapter.rest.dto.request.EventUpdateRequest;
import vsu.cs.is.infsysserver.event.adapter.rest.dto.response.EventResponse;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EventMapper eventMapper;

    public Collection<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::map)
                .toList();
    }

    public EventResponse getEventById(Long id) {
        return eventMapper.map(findByIdOrThrow(id));
    }

    public EventResponse createEvent(EventCreateRequest eventCreateRequest) {
        var savedNews = eventRepository.save(eventMapper.map(eventCreateRequest));

        return eventMapper.map(savedNews);
    }

    @Transactional
    public EventResponse updateEventById(Long id, EventUpdateRequest eventUpdateRequest) {
        var event = findByIdOrThrow(id);
        event.updateFromRequest(eventUpdateRequest);

        var upadatedEvent = eventRepository.save(event);

        return eventMapper.map(upadatedEvent);
    }

    public void deleteEventById(Long id) {
        eventRepository.delete(findByIdOrThrow(id));
    }

    private Event findByIdOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("По идентификатору: " + id + " не найдено ни одного события")
        );
    }

}
