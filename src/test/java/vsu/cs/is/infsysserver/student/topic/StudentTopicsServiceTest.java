package vsu.cs.is.infsysserver.student.topic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import vsu.cs.is.infsysserver.employee.adapter.jpa.EmployeeRepository;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.StudentTopicAssignmentRepository;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity.StudentTopicAssignment;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response.StudentTopicsImportResponse;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response.StudentTopicsResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты StudentTopicsService")
class StudentTopicsServiceTest {

    @Mock
    private StudentTopicAssignmentRepository studentTopicAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private StudentRepository studentRepository;

    private StudentTopicsService studentTopicsService;
    private RestTemplate restTemplate;
    private MockRestServiceServer mockRestServiceServer;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        studentTopicsService = new StudentTopicsService(
                studentTopicAssignmentRepository,
                studentRepository,
                employeeRepository,
                restTemplate
        );
    }

    @Test
    @DisplayName("Импорт CSV с разделителем ';' создаёт запись")
    void importFile_WhenCsvIsValid_CreatesAssignment() {
        String csvHeader =
                "Логин студента;ФИО студента;Тема курсовой;Тема ВКР;"
                        + "Логин научного руководителя;"
                        + "ФИО научного руководителя";
        String csvRow =
                "ivanov_i_i;Иванов Иван Иванович;Тема курсовой;Тема ВКР;"
                        + "petrov_a_a;Петров Алексей Алексеевич";
        String csv = String.join("\n", csvHeader, csvRow);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "topics.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Employee employee = new Employee();
        employee.setId(15L);
        employee.setUser(User.builder().login("petrov_a_a").build());

        Student student = Student.builder()
                .id(10L)
                .user(User.builder().login("ivanov_i_i").build())
                .build();

        when(studentRepository.findByUser_Login("ivanov_i_i")).thenReturn(Optional.of(student));
        when(studentTopicAssignmentRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(employeeRepository.findByUserLogin("petrov_a_a")).thenReturn(Optional.of(employee));
        when(studentTopicAssignmentRepository.save(any(StudentTopicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(1, response.processedRows());
        assertEquals(1, response.createdRows());
        assertEquals(0, response.updatedRows());
        assertEquals(0, response.skippedRows());
        assertTrue(response.errors().isEmpty());

        ArgumentCaptor<StudentTopicAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(StudentTopicAssignment.class);
        verify(studentTopicAssignmentRepository).save(assignmentCaptor.capture());

        StudentTopicAssignment savedAssignment = assignmentCaptor.getValue();
        assertEquals("ivanov_i_i", savedAssignment.getStudentLogin());
        assertEquals(10L, savedAssignment.getStudent().getId());
        assertEquals("Иванов Иван Иванович", savedAssignment.getStudentFullName());
        assertEquals("Тема курсовой", savedAssignment.getCourseWorkTopic());
        assertEquals("Тема ВКР", savedAssignment.getThesisTopic());
        assertEquals("petrov_a_a", savedAssignment.getSupervisorLogin());
        assertEquals("Петров Алексей Алексеевич", savedAssignment.getSupervisorFullName());
        assertEquals(15L, savedAssignment.getSupervisorEmployee().getId());
        assertNotNull(savedAssignment.getImportedAt());
    }

    @Test
    @DisplayName("Импорт CSV без обязательной колонки возвращает ошибку")
    void importFile_WhenRequiredHeaderIsMissing_ThrowsBadRequest() {
        String csvHeader =
                "Логин студента;ФИО студента;Тема курсовой;Тема ВКР;"
                        + "Логин научного руководителя";
        String csv = String.join(
                "\n",
                csvHeader,
                "ivanov_i_i;Иванов Иван Иванович;Тема курсовой;Тема ВКР;petrov_a_a"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "topics.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentTopicsService.importFile(file)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("ФИО научного руководителя"));
    }

    @Test
    @DisplayName(
            "Импорт CSV возвращает ошибку, "
                    + "если студент не найден по логину"
    )
    void importFile_WhenStudentIsMissing_ReportsError() {
        String csvHeader =
                "Логин студента;ФИО студента;Тема курсовой;Тема ВКР;"
                        + "Логин научного руководителя;"
                        + "ФИО научного руководителя";
        String csv = String.join(
                "\n",
                csvHeader,
                "ghost_login;Иванов Иван Иванович;Тема курсовой;;"
                        + "petrov_a_a;Петров Алексей Алексеевич"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "topics.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        when(studentRepository.findByUser_Login("ghost_login")).thenReturn(Optional.empty());

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(1, response.processedRows());
        assertEquals(0, response.createdRows());
        assertEquals(0, response.updatedRows());
        assertEquals(1, response.skippedRows());
        assertEquals(1, response.errors().size());
        String expectedStudentMessage = "Студент с логином 'ghost_login' не найден";
        assertTrue(response.errors().get(0).message().contains(expectedStudentMessage));
    }

    @Test
    @DisplayName(
            "Импорт CSV возвращает ошибку, "
                    + "если научрук не найден по логину"
    )
    void importFile_WhenSupervisorIsMissing_ReportsError() {
        String csvHeader =
                "Логин студента;ФИО студента;Тема курсовой;Тема ВКР;"
                        + "Логин научного руководителя;"
                        + "ФИО научного руководителя";
        String csv = String.join(
                "\n",
                csvHeader,
                "ivanov_i_i;Иванов Иван Иванович;Тема курсовой;;"
                        + "missing_supervisor;Петров Алексей Алексеевич"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "topics.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Student student = Student.builder()
                .id(10L)
                .user(User.builder().login("ivanov_i_i").build())
                .build();

        when(studentRepository.findByUser_Login("ivanov_i_i")).thenReturn(Optional.of(student));
        when(employeeRepository.findByUserLogin("missing_supervisor")).thenReturn(Optional.empty());

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(1, response.processedRows());
        assertEquals(0, response.createdRows());
        assertEquals(0, response.updatedRows());
        assertEquals(1, response.skippedRows());
        assertEquals(1, response.errors().size());
        String expectedSupervisorMessage =
                "Научный руководитель с логином 'missing_supervisor' не найден";
        assertTrue(
                response.errors().get(0).message()
                        .contains(expectedSupervisorMessage)
        );
    }

    @Test
    @DisplayName("Получение тем по логину студента возвращает DTO")
    void getTopicsByStudentLogin_WhenAssignmentExists_ReturnsResponse() {
        StudentTopicAssignment assignment = StudentTopicAssignment.builder()
                .studentLogin("ivanov_i_i")
                .courseWorkTopic("Тема курсовой")
                .thesisTopic("Тема ВКР")
                .supervisorLogin("petrov_a_a")
                .supervisorFullName("Петров Алексей Алексеевич")
                .build();

        when(studentTopicAssignmentRepository.findByStudent_User_Login("ivanov_i_i"))
                .thenReturn(Optional.empty());
        when(studentTopicAssignmentRepository.findByStudentLogin("ivanov_i_i"))
                .thenReturn(Optional.of(assignment));

        Optional<StudentTopicsResponse> response = studentTopicsService.getTopicsByStudentLogin("ivanov_i_i");

        assertTrue(response.isPresent());
        assertEquals("ivanov_i_i", response.get().studentLogin());
        assertEquals("Тема курсовой", response.get().courseWorkTopic());
        assertEquals("Тема ВКР", response.get().thesisTopic());
        assertEquals("petrov_a_a", response.get().supervisorLogin());
        assertEquals("Петров Алексей Алексеевич", response.get().supervisorFullName());
    }

    @Test
    @DisplayName("Импорт по ссылке Google Sheets запускает тот же импорт")
    void importGoogleSheet_WhenUrlIsValid_ImportsRows() {
        String googleSheetUrl = "https://docs.google.com/spreadsheets/d/test-sheet-id/edit#gid=12345";
        String csvHeader =
                "Логин студента;ФИО студента;Тема курсовой;Тема ВКР;"
                        + "Логин научного руководителя;"
                        + "ФИО научного руководителя";
        String csvRow =
                "ivanov_i_i;Иванов Иван Иванович;Тема курсовой;Тема ВКР;"
                        + "petrov_a_a;Петров Алексей Алексеевич";
        String csv = String.join("\n", csvHeader, csvRow);

        Student student = Student.builder()
                .id(10L)
                .user(User.builder().login("ivanov_i_i").build())
                .build();
        Employee employee = new Employee();
        employee.setId(15L);
        employee.setUser(User.builder().login("petrov_a_a").build());

        URI exportUri = URI.create(
                "https://docs.google.com/spreadsheets/d/test-sheet-id/export?format=csv&gid=12345"
        );
        mockRestServiceServer.expect(once(), requestTo(exportUri))
                .andRespond(withSuccess(csv, MediaType.parseMediaType("text/csv")));
        when(studentRepository.findByUser_Login("ivanov_i_i")).thenReturn(Optional.of(student));
        when(studentTopicAssignmentRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(employeeRepository.findByUserLogin("petrov_a_a")).thenReturn(Optional.of(employee));
        when(studentTopicAssignmentRepository.save(any(StudentTopicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentTopicsImportResponse response = studentTopicsService.importGoogleSheet(googleSheetUrl);

        assertEquals(1, response.processedRows());
        assertEquals(1, response.createdRows());
        assertEquals(0, response.updatedRows());
        assertEquals(0, response.skippedRows());
        assertTrue(response.errors().isEmpty());
        mockRestServiceServer.verify();
    }

    @Test
    @DisplayName("Импорт по ссылке отклоняет URL не на docs.google.com")
    void importGoogleSheet_WhenHostIsInvalid_ThrowsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentTopicsService.importGoogleSheet("https://example.com/sheet.csv")
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("docs.google.com"));
    }
}
