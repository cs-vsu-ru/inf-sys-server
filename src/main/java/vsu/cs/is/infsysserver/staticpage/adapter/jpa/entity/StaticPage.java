package vsu.cs.is.infsysserver.staticpage.adapter.jpa.entity;

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
import vsu.cs.is.infsysserver.staticpage.adapter.rest.dto.request.StaticPageUpdateRequest;

import java.util.stream.Stream;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "static_pages")
@Entity
@SuppressWarnings("")
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

    @Column(columnDefinition = "TEXT")
    private String contentAbout;

    @Column(columnDefinition = "TEXT")
    private String contentEducation;

    @Column(columnDefinition = "TEXT")
    private String contentStudents;

    @Column(columnDefinition = "TEXT")
    private String contentImportant;

    @Column(columnDefinition = "TEXT")
    private String contentExams;

    @Column(columnDefinition = "TEXT")
    private String contentConfidential;

    @Column(columnDefinition = "TEXT")
    private String contentContacts;

    @Column(columnDefinition = "TEXT")
    private String contentMiscellaneous;

    public void updateFromRequest(StaticPageUpdateRequest request) {
        if (request.isAllFieldsNull()) {
            Stream.of(contentAbout, contentEducation, contentStudents,
                    contentImportant, contentConfidential, contentExams, contentContacts,
                    contentMiscellaneous).map(item -> null);
        } else {
            this.contentAbout = request.contentAbout() != null
                    ? request.contentAbout() : this.contentAbout;
            this.contentEducation = request.contentEducation() != null
                    ? request.contentEducation() : this.contentEducation;
            this.contentStudents = request.contentStudents() != null
                    ? request.contentStudents() : this.contentStudents;
            this.contentImportant  = request.contentImportant() != null
                    ? request.contentImportant() : this.contentImportant;
            this.contentExams  = request.contentExams() != null
                    ? request.contentExams() : this.contentExams;
            this.contentConfidential = request.contentConfidential() != null
                    ? request.contentConfidential() : this.contentConfidential;
            this.contentContacts = request.contentContacts() != null
                    ? request.contentContacts() : this.contentContacts;
            this.contentMiscellaneous = request.contentMiscellaneous() != null
                    ? request.contentMiscellaneous() : this.contentMiscellaneous;
        }
    }
}
