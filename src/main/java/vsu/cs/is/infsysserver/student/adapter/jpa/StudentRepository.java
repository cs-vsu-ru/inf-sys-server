package vsu.cs.is.infsysserver.student.adapter.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Student findByUser_Id(Long userId);

    List<Student> findBySupervisorId(Long supervisorId);

    @Transactional
    @Modifying
    @Query("UPDATE Student s SET s.supervisor = null WHERE s.supervisor.id = :supervisorId")
    void clearSupervisorForStudents(Long supervisorId);
}
