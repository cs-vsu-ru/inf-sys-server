package vsu.cs.is.infsysserver.student.adapter.jpa.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

@Entity
@Data
@Table(name = "students")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "course")
    private Integer course;

    @Column(name = "group_nm")
    private String group;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @ManyToOne
    @JoinColumn(name = "supervisor")
    private User supervisor;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "scientific_supervisor")
    private User scientificSupervisor;

    @Column(name = "course_job")
    private String courseJob;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_disabled", nullable = false)
    @Builder.Default
    private boolean isDisabled = false;
}
