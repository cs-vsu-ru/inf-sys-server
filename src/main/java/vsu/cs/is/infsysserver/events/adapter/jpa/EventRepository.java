package vsu.cs.is.infsysserver.events.adapter.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import vsu.cs.is.infsysserver.events.adapter.jpa.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
