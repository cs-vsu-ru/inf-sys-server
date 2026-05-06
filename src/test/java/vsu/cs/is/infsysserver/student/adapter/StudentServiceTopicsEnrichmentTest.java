package vsu.cs.is.infsysserver.student.adapter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.student.adapter.jpa.DepartmentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentEditRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.StudentTopicAssignmentRepository;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity.StudentTopicAssignment;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Обогащение StudentResponse темами и руководителем")
class StudentServiceTopicsEnrichmentTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private StudentTopicAssignmentRepository studentTopicAssignmentRepository;

    @InjectMocks private StudentService studentService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getAllStudents — студент с assignment'ом получает все 4 новых поля")
    void getAllStudents_WithAssignment_FillsAllNewFields() {
        Student student = buildStudent(1L, "ivanov_i_i");
        Employee supervisor = new Employee();
        supervisor.setId(7L);
        StudentTopicAssignment assignment = StudentTopicAssignment.builder()
                .student(student)
                .supervisorFullName("Петров Пётр Петрович")
                .supervisorEmployee(supervisor)
                .courseWorkTopic("Курсовая ABC")
                .thesisTopic("ВКР XYZ")
                .build();

        when(studentRepository.findAll(any(Sort.class))).thenReturn(List.of(student));
        when(studentTopicAssignmentRepository.findAllByStudent_IdIn(Set.of(1L)))
                .thenReturn(List.of(assignment));

        List<StudentResponse> result = studentService.getAllStudents();

        assertEquals(1, result.size());
        StudentResponse response = result.get(0);
        assertEquals("Петров Пётр Петрович", response.getSupervisorFullName());
        assertEquals(7L, response.getSupervisorEmployeeId());
        assertEquals("Курсовая ABC", response.getCourseWorkTopic());
        assertEquals("ВКР XYZ", response.getThesisTopic());
    }

    @Test
    @DisplayName("getAllStudents — студент без assignment'а: все 4 новых поля null")
    void getAllStudents_WithoutAssignment_NewFieldsNull() {
        Student student = buildStudent(2L, "petrov_p_p");

        when(studentRepository.findAll(any(Sort.class))).thenReturn(List.of(student));
        when(studentTopicAssignmentRepository.findAllByStudent_IdIn(Set.of(2L)))
                .thenReturn(List.of());

        List<StudentResponse> result = studentService.getAllStudents();

        assertEquals(1, result.size());
        assertNull(result.get(0).getSupervisorFullName());
        assertNull(result.get(0).getSupervisorEmployeeId());
        assertNull(result.get(0).getCourseWorkTopic());
        assertNull(result.get(0).getThesisTopic());
    }

    @Test
    @DisplayName("getAllStudents — пустой список: возвращается пустой результат без падения")
    void getAllStudents_EmptyList_ReturnsEmpty() {
        when(studentRepository.findAll(any(Sort.class))).thenReturn(List.of());

        List<StudentResponse> result = studentService.getAllStudents();

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("getCurrentStudent — assignment подтянут и поля заполнены")
    void getCurrentStudent_WithAssignment_FillsFields() {
        Student student = buildStudent(1L, "ivanov_i_i");
        StudentTopicAssignment assignment = StudentTopicAssignment.builder()
                .student(student)
                .courseWorkTopic("Курсовая 4 курс")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivanov_i_i", null)
        );

        when(userRepository.findByLogin("ivanov_i_i")).thenReturn(Optional.of(student.getUser()));
        when(studentRepository.findByUser_Id(1L)).thenReturn(student);
        when(studentTopicAssignmentRepository.findByStudent_Id(1L))
                .thenReturn(Optional.of(assignment));

        StudentResponse response = studentService.getCurrentStudent();

        assertEquals("Курсовая 4 курс", response.getCourseWorkTopic());
    }

    @Test
    @DisplayName("getCurrentStudent — без assignment'а: новые поля null")
    void getCurrentStudent_WithoutAssignment_NewFieldsNull() {
        Student student = buildStudent(2L, "petrov_p_p");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("petrov_p_p", null)
        );

        when(userRepository.findByLogin("petrov_p_p")).thenReturn(Optional.of(student.getUser()));
        when(studentRepository.findByUser_Id(2L)).thenReturn(student);
        when(studentTopicAssignmentRepository.findByStudent_Id(2L))
                .thenReturn(Optional.empty());

        StudentResponse response = studentService.getCurrentStudent();

        assertNull(response.getCourseWorkTopic());
        assertNull(response.getThesisTopic());
        assertNull(response.getSupervisorFullName());
    }

    @Test
    @DisplayName("getStudentById — найден с assignment'ом: возвращает обогащённый Optional")
    void getStudentById_WithAssignment_ReturnsEnriched() {
        Student student = buildStudent(3L, "ivanov_i_i");
        StudentTopicAssignment assignment = StudentTopicAssignment.builder()
                .student(student)
                .thesisTopic("ВКР тема")
                .build();

        when(studentRepository.findById(3L)).thenReturn(Optional.of(student));
        when(studentTopicAssignmentRepository.findByStudent_Id(3L))
                .thenReturn(Optional.of(assignment));

        Optional<StudentResponse> result = studentService.getStudentById(3L);

        assertTrue(result.isPresent());
        assertEquals("ВКР тема", result.get().getThesisTopic());
    }

    @Test
    @DisplayName("getStudentById — не найден: возвращает Optional.empty()")
    void getStudentById_NotFound_ReturnsEmpty() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<StudentResponse> result = studentService.getStudentById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("editStudent — после save возвращает обогащённый response")
    void editStudent_ReturnsEnrichedResponse() {
        Student student = buildStudent(4L, "sidorov_s_s");
        StudentTopicAssignment assignment = StudentTopicAssignment.builder()
                .student(student)
                .courseWorkTopic("Курсовая после edit")
                .build();

        when(studentRepository.findById(4L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentTopicAssignmentRepository.findByStudent_Id(4L))
                .thenReturn(Optional.of(assignment));

        StudentEditRequest edit = new StudentEditRequest();
        edit.setFirstName("Сидор");

        StudentResponse response = studentService.editStudent(4L, edit);

        assertEquals("Курсовая после edit", response.getCourseWorkTopic());
    }

    private static Student buildStudent(long id, String login) {
        User user = User.builder()
                .id(id)
                .login(login)
                .firstName("Имя")
                .lastName("Фамилия")
                .build();
        return Student.builder()
                .id(id)
                .user(user)
                .build();
    }
}
