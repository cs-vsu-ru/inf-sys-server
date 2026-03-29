package vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_topic_assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTopicAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_login", nullable = false, unique = true)
    private String studentLogin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "student_full_name", nullable = false)
    private String studentFullName;

    @Column(name = "course_work_topic")
    private String courseWorkTopic;

    @Column(name = "thesis_topic")
    private String thesisTopic;

    @Column(name = "supervisor_login", nullable = false)
    private String supervisorLogin;

    @Column(name = "supervisor_full_name", nullable = false)
    private String supervisorFullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_employee_id")
    private Employee supervisorEmployee;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;
}
