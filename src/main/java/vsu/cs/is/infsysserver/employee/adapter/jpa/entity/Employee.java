package vsu.cs.is.infsysserver.employee.adapter.jpa.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "employees")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "employee_sequence")
    @SequenceGenerator(
            name = "employee_sequence",
            allocationSize = 1)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true)
    private User user;

    private String patronymic;
    private String post;
    private String academicTitle;
    private String academicDegree;
    private Date experience;
    private Date professionalExperience;
    private String plan;
    private boolean hasLessons;

    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "last_modified_by_id")
    private User lastModifiedBy;
    private Date lastModifiedAt;

    public void updateFromRequest(EmployeeUpdateRequest request, User updater) {
        this.user.setFirstName(request.firstName());
        this.user.setLastName(request.lastName());
        this.user.setEmail(request.email());
        this.user.setLogin(request.login());
        this.user.setRole(request.role());
        this.patronymic = request.patronymic();
        this.post = request.post();
        this.academicTitle = request.academicTitle();
        this.academicDegree = request.academicDegree();
        this.experience = request.experience();
        this.professionalExperience = request.professionalExperience();
        this.plan = request.plan();
        this.hasLessons = request.hasLessons();
        this.lastModifiedBy = updater;
        this.lastModifiedAt = new Date();
    }
}
