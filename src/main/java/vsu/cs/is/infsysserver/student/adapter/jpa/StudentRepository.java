package vsu.cs.is.infsysserver.student.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Student findByUser_Id(Long userId);
}
