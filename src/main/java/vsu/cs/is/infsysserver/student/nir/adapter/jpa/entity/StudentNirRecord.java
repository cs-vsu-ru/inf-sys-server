package vsu.cs.is.infsysserver.student.nir.adapter.jpa.entity;

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
@Table(name = "student_nir_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentNirRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "student_login", nullable = false)
    private String studentLogin;

    @Column(name = "student_full_name", nullable = false)
    private String studentFullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_employee_id")
    private Employee supervisorEmployee;

    @Column(name = "supervisor_full_name")
    private String supervisorFullName;

    @Column(name = "thesis_topic", columnDefinition = "TEXT")
    private String thesisTopic;

    @Column(name = "nir7_grade", length = 64)
    private String nir7Grade;

    @Column(name = "nir7_docs")
    private String nir7Docs;

    @Column(name = "predefense1_grade", length = 64)
    private String predefense1Grade;

    @Column(name = "predefense2_grade", length = 64)
    private String predefense2Grade;

    @Column(name = "nir8_grade", length = 64)
    private String nir8Grade;

    @Column(name = "nir8_docs")
    private String nir8Docs;

    @Column(name = "is_debtor", nullable = false)
    private boolean debtor;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;
}
