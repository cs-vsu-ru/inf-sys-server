package vsu.cs.is.infsysserver.staticPage.adapter.jpa.entity;

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
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.request.StaticPageUpdateRequest;

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

    private String contentAbout;

    private String contentEducation;

    private String contentStudents;

    private String contentPartners;

    private String contentConfidential;

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
