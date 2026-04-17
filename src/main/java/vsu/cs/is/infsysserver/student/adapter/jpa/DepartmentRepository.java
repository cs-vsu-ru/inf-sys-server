package vsu.cs.is.infsysserver.student.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
