package vsu.cs.is.infsysserver.slider.adapter.jpa.entity;

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
import vsu.cs.is.infsysserver.slider.adapter.rest.dto.request.SliderElementUpdateRequest;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sliders")
@Entity
public class SliderElement {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "slider_sequence"
    )
    @SequenceGenerator(
            name = "slider_sequence",
            allocationSize = 1
    )
    private Long id;

    private String imageURL;

    private String urlTo;

    private String title;

    @CreationTimestamp(source = SourceType.DB)
    private LocalDateTime createAt;

    private LocalDateTime updatedAt;

    public void updateFromRequest(SliderElementUpdateRequest request) {
        this.title = request.title();
        this.imageURL = request.imageURL();
        this.urlTo = request.urlTo();
        this.updatedAt = LocalDateTime.now();
    }
}
