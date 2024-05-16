package vsu.cs.is.infsysserver.staticpage.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vsu.cs.is.infsysserver.staticpage.adapter.rest.dto.request.StaticPageUpdateRequest;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "static_pages")
@Entity
public class StaticPage {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "static_pages_sequence"
    )
    @SequenceGenerator(
            name = "static_pages_sequence",
            allocationSize = 1
    )
    private Long id;

    @Column(columnDefinition="TEXT")
    private String contentAbout;

    @Column(columnDefinition="TEXT")
    private String contentEducation;

    @Column(columnDefinition="TEXT")
    private String contentStudents;

    @Column(columnDefinition="TEXT")
    private String contentPartners;

    @Column(columnDefinition="TEXT")
    private String contentConfidential;

    @Column(columnDefinition="TEXT")
    private String contentContacts;

    public void updateFromRequest(StaticPageUpdateRequest request) {
        this.contentAbout = request.contentAbout();
        this.contentEducation = request.contentEducation();
        this.contentStudents = request.contentStudents();
        this.contentPartners = request.contentPartners();
        this.contentConfidential = request.contentConfidential();
        this.contentContacts = request.contentContacts();

    }
}
