package vsu.cs.is.infsysserver.student.adapter;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.student.adapter.jpa.DepartmentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentEditRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.Optional;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentResponse editStudent(Long id, StudentEditRequest edit) {
        Student student = studentRepository.findById(id).orElseThrow();
        User user = student.getUser();

        setIfNotNull(edit.getFirstName(), user::setFirstName);
        setIfNotNull(edit.getLastName(), user::setLastName);
        setIfNotNull(edit.getEmail(), user::setEmail);
        setIfNotNull(edit.getPatronymic(), student::setPatronymic);
        setIfNotNull(edit.getCourse(), student::setCourse);
        setIfNotNull(edit.getImageUrl(), student::setImageUrl);
        //:)
        setIfNotNull(edit.getStartYear(), student::setStartYear);
        setIfNotNull(edit.getEndYear(), student::setEndYear);
        setIfNotNull(edit.getGroup(), student::setGroup);
        setIfNotNull(edit.getCourseJob(), student::setCourseJob);

        if (edit.getSupervisor() != null) {
            Optional<User> sup = userRepository.findById(edit.getSupervisor());
            sup.ifPresent(value -> setIfNotNull(value, student::setSupervisor));

        }

        if (edit.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(edit.getPassword()));
        }

        if(edit.getDepartment() != null) {
            departmentRepository.findById(edit.getDepartment()).ifPresent(
                    value -> setIfNotNull(value, student::setDepartment)
            );
        }

        if(edit.getScientificSupervisor() != null) {
            userRepository.findById(edit.getScientificSupervisor()).ifPresent(
                    value -> setIfNotNull(value, student::setScientificSupervisor)
            );
        }

        user = userRepository.save(user);
        student.setUser(user);
        studentRepository.save(student);

        return new StudentResponse(student);
    }

    public void disableStudent(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("По id: " + id + " не найдено ни одного студента")
        );
        student.setDisabled(!student.isDisabled());
        studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("По id: " + id + " не найдено ни одного студента")
        );
        User user = student.getUser();
        studentRepository.delete(student);
        studentRepository.flush();
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public StudentResponse getCurrentStudent() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByLogin(principal).orElseThrow();

        Student student = studentRepository.findByUser_Id(user.getId());

        return new StudentResponse(student);
    }

    private static <T> void setIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }
}
