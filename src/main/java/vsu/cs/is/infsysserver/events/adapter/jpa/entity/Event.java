package vsu.cs.is.infsysserver.events.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import vsu.cs.is.infsysserver.events.adapter.rest.dto.request.EventUpdateRequest;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
@Entity
public class Event {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "events_sequence"
    )
    @SequenceGenerator(
            name = "events_sequence",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;

    private Date startDateTime;
    private Date endDateTime;

    @CreationTimestamp(source = SourceType.DB)
    private Date publicationDateTime;
    private Date lastModifiedDateTime;

    public void updateFromRequest(EventUpdateRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.startDateTime = request.startDateTime();
        this.endDateTime = request.endDateTime();
        this.lastModifiedDateTime = new Date();
    }

}
