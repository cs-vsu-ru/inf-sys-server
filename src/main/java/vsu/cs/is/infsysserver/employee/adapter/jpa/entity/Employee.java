package vsu.cs.is.infsysserver.employee.adapter.jpa.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "employee")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Employee implements Serializable {
    @Id
    private Long id;

    private String post;
    private String academicTitle;
    private String academicDegree;
    private String experience;
    private String professionalExperience;
    private String patronymic;
    private LocalDate dateOfBirth;

    @OneToOne(optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private SimpleUser user;

    private String plan;
    private boolean hasLessons;
    private LocalDateTime createdAt;
//
//    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
//    @JsonIgnoreProperties(value = { "employee", "accessModes" }, allowSetters = true)
//    @ToString.Exclude
//    private Set<Articles> articles = new HashSet<>();
//
//    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
//    @JsonIgnoreProperties(value = { "employee", "accessModes" }, allowSetters = true)
//    @ToString.Exclude
//    private Set<Events> events = new HashSet<>();
//
//    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
//    @JsonIgnoreProperties(value = { "subject", "eduSchedulePlace", "schedule", "employee" }, allowSetters = true)
//    @ToString.Exclude
//    private Set<Lesson> lessons = new HashSet<>();
//
//    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
//    @JsonIgnoreProperties(value = { "employee", "accessModes" }, allowSetters = true)
//    @ToString.Exclude
//    private Set<Pages> pages = new HashSet<>();
//
//    @ManyToMany
//    @JoinTable(name = "employee_specialities", joinColumns = @JoinColumn(name = "employee_id"),
//    inverseJoinColumns = @JoinColumn(name = "specialities_id"))
//    @ToString.Exclude
//    private Set<Specialities> specialities = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Employee employee = (Employee) o;
        return getId() != null && Objects.equals(getId(), employee.getId());
    }

    @Override
    public final int hashCode() {
        if (this instanceof HibernateProxy) {
            return ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode();
        } else {
            return getClass().hashCode();
        }
    }
}
