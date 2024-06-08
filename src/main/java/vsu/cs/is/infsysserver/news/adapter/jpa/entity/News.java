package vsu.cs.is.infsysserver.news.adapter.jpa.entity;

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
import vsu.cs.is.infsysserver.news.adapter.rest.dto.request.NewsUpdateRequest;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "news")
@Entity
public class News {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "news_sequence"
    )
    @SequenceGenerator(
            name = "news_sequence",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "image_link", nullable = false)
    private String imageLink;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "publication_at")
    private LocalDateTime publicationAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateFromRequest(NewsUpdateRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.imageLink = request.imageLink() == null ? this.imageLink : request.imageLink();
        this.updatedAt = LocalDateTime.now();
    }
}
