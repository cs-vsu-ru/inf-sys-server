package vsu.cs.is.infsysserver.student.topic.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity.StudentTopicAssignment;

import java.util.Optional;

public interface StudentTopicAssignmentRepository extends JpaRepository<StudentTopicAssignment, Long> {

    Optional<StudentTopicAssignment> findByStudent_Id(Long studentId);

    Optional<StudentTopicAssignment> findByStudent_User_Login(String studentLogin);

    Optional<StudentTopicAssignment> findByStudentLogin(String studentLogin);
}
