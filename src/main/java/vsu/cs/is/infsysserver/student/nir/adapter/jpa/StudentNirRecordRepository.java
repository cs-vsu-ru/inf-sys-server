package vsu.cs.is.infsysserver.student.nir.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vsu.cs.is.infsysserver.student.nir.adapter.jpa.entity.StudentNirRecord;

import java.util.Optional;

@Repository
public interface StudentNirRecordRepository extends JpaRepository<StudentNirRecord, Long> {

    Optional<StudentNirRecord> findByStudent_Id(Long studentId);

    Optional<StudentNirRecord> findByStudentLogin(String studentLogin);
}
