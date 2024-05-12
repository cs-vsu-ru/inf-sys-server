package vsu.cs.is.infsysserver.event.adapter;

import org.mapstruct.Mapper;
import vsu.cs.is.infsysserver.event.adapter.jpa.entity.Event;
import vsu.cs.is.infsysserver.event.adapter.rest.dto.request.EventCreateRequest;
import vsu.cs.is.infsysserver.event.adapter.rest.dto.response.EventResponse;

@Mapper
public interface EventMapper {

    EventResponse map(Event event);

    Event map(EventCreateRequest eventCreateRequest);
}
