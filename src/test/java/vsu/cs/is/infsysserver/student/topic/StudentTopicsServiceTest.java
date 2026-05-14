package vsu.cs.is.infsysserver.student.topic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
    @DisplayName("Импорт XLSX кафедрального формата связывает тему со студентом по ФИО")
    void importFile_WhenDepartmentXlsxIsValid_CreatesAssignment() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "department-topics.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                departmentTemplateXlsx()
        );

        Employee employee = new Employee();
        employee.setId(15L);
        employee.setPatronymic("Владимирович");
        employee.setUser(User.builder()
                .login("sychev_a_v")
                .lastName("Сычев")
                .firstName("Андрей")
                .build());

        Student student = Student.builder()
                .id(10L)
                .patronymic("Иванович")
                .user(User.builder()
                        .login("ivanov_i_i")
                        .lastName("Иванов")
                        .firstName("Иван")
                        .build())
                .build();

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(studentTopicAssignmentRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(studentTopicAssignmentRepository.save(any(StudentTopicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(1, response.processedRows(), response.toString());
        assertEquals(1, response.createdRows(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());

        ArgumentCaptor<StudentTopicAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(StudentTopicAssignment.class);
        verify(studentTopicAssignmentRepository).save(assignmentCaptor.capture());

        StudentTopicAssignment savedAssignment = assignmentCaptor.getValue();
        assertEquals(10L, savedAssignment.getStudent().getId());
        assertEquals("ivanov_i_i", savedAssignment.getStudentLogin());
        assertEquals("Иванов Иван Иванович", savedAssignment.getStudentFullName());
        assertEquals("Классификация текстов", savedAssignment.getCourseWorkTopic());
        assertEquals("Классификация текстов с помощью BERT", savedAssignment.getThesisTopic());
        assertEquals("sychev_a_v", savedAssignment.getSupervisorLogin());
        assertEquals(15L, savedAssignment.getSupervisorEmployee().getId());
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
    @DisplayName("Получение тем по ID студента возвращает DTO")
    void getTopicsByStudentId_WhenAssignmentExists_ReturnsResponse() {
        StudentTopicAssignment assignment = StudentTopicAssignment.builder()
                .student(Student.builder().id(10L).build())
                .studentLogin("ivanov_i_i")
                .studentFullName("Иванов Иван Иванович")
                .courseWorkTopic("Тема курсовой")
                .supervisorFullName("Петров Алексей Алексеевич")
                .build();

        when(studentTopicAssignmentRepository.findByStudent_Id(10L))
                .thenReturn(Optional.of(assignment));

        Optional<StudentTopicsResponse> response = studentTopicsService.getTopicsByStudentId(10L);

        assertTrue(response.isPresent());
        assertEquals(10L, response.get().studentId());
        assertEquals("Иванов Иван Иванович", response.get().studentFullName());
        assertEquals("Тема курсовой", response.get().courseWorkTopic());
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
    @DisplayName("При коллизии ФИО email разруливает в правильного студента")
    void importFile_WhenFullNameCollides_EmailTiebreakerPicksRightStudent() {
        String csvHeader =
                "ФИО студента;email;Тема курсовой;Тема ВКР;ФИО научного руководителя";
        String csv = String.join(
                "\n",
                csvHeader,
                "Иванов Иван Иванович;ivanov.target@cs.vsu.ru;Тема курсовой;;Петров Алексей Алексеевич"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "topics.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Student firstHomonym = Student.builder()
                .id(10L)
                .user(User.builder()
                        .login("ivanov_i_i_1")
                        .lastName("Иванов")
                        .firstName("Иван")
                        .email("ivanov.other@cs.vsu.ru")
                        .build())
                .patronymic("Иванович")
                .build();
        Student secondHomonym = Student.builder()
                .id(11L)
                .user(User.builder()
                        .login("ivanov_i_i_2")
                        .lastName("Иванов")
                        .firstName("Иван")
                        .email("ivanov.target@cs.vsu.ru")
                        .build())
                .patronymic("Иванович")
                .build();

        Employee supervisor = new Employee();
        supervisor.setId(15L);
        supervisor.setPatronymic("Алексеевич");
        supervisor.setUser(User.builder()
                .login("petrov_a_a")
                .lastName("Петров")
                .firstName("Алексей")
                .build());

        when(studentRepository.findAll()).thenReturn(List.of(firstHomonym, secondHomonym));
        when(studentTopicAssignmentRepository.findByStudent_Id(11L)).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(List.of(supervisor));
        when(studentTopicAssignmentRepository.save(any(StudentTopicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(1, response.processedRows(), response.toString());
        assertEquals(1, response.createdRows(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());

        ArgumentCaptor<StudentTopicAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(StudentTopicAssignment.class);
        verify(studentTopicAssignmentRepository).save(assignmentCaptor.capture());
        assertEquals(11L, assignmentCaptor.getValue().getStudent().getId());
        assertEquals("ivanov_i_i_2", assignmentCaptor.getValue().getStudentLogin());
    }

    @Test
    @DisplayName("Если ФИО не найдено, fallback по email находит студента")
    void importFile_WhenFullNameMisses_EmailFallbackFindsStudent() {
        String csvHeader =
                "ФИО студента;email;Тема курсовой;Тема ВКР;ФИО научного руководителя";
        String csv = String.join(
                "\n",
                csvHeader,
                "Сидоров С С;sidorov_s_s@cs.vsu.ru;Тема курсовой;;Петров Алексей Алексеевич"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "topics.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Student sidorov = Student.builder()
                .id(20L)
                .user(User.builder()
                        .login("sidorov_s_s")
                        .lastName("Сидоров")
                        .firstName("Степан")
                        .email("sidorov_s_s@cs.vsu.ru")
                        .build())
                .patronymic("Сергеевич")
                .build();

        Employee supervisor = new Employee();
        supervisor.setId(15L);
        supervisor.setPatronymic("Алексеевич");
        supervisor.setUser(User.builder()
                .login("petrov_a_a")
                .lastName("Петров")
                .firstName("Алексей")
                .build());

        when(studentRepository.findAll()).thenReturn(List.of());
        when(studentRepository.findByUser_EmailIgnoreCase("sidorov_s_s@cs.vsu.ru"))
                .thenReturn(Optional.of(sidorov));
        when(studentTopicAssignmentRepository.findByStudent_Id(20L)).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(List.of(supervisor));
        when(studentTopicAssignmentRepository.save(any(StudentTopicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(1, response.processedRows(), response.toString());
        assertEquals(1, response.createdRows(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());

        ArgumentCaptor<StudentTopicAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(StudentTopicAssignment.class);
        verify(studentTopicAssignmentRepository).save(assignmentCaptor.capture());
        assertEquals(20L, assignmentCaptor.getValue().getStudent().getId());
        assertEquals("sidorov_s_s", assignmentCaptor.getValue().getStudentLogin());
    }

    @Test
    @DisplayName("Коллизия ФИО без email возвращает понятную ошибку")
    void importFile_WhenFullNameCollidesWithoutEmail_ReportsError() {
        String csvHeader =
                "ФИО студента;Тема курсовой;Тема ВКР;ФИО научного руководителя";
        String csv = String.join(
                "\n",
                csvHeader,
                "Иванов Иван Иванович;Тема курсовой;;Петров Алексей Алексеевич"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "topics.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        Student firstHomonym = Student.builder()
                .id(10L)
                .user(User.builder()
                        .login("ivanov_i_i_1")
                        .lastName("Иванов")
                        .firstName("Иван")
                        .build())
                .patronymic("Иванович")
                .build();
        Student secondHomonym = Student.builder()
                .id(11L)
                .user(User.builder()
                        .login("ivanov_i_i_2")
                        .lastName("Иванов")
                        .firstName("Иван")
                        .build())
                .patronymic("Иванович")
                .build();

        when(studentRepository.findAll()).thenReturn(List.of(firstHomonym, secondHomonym));

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(1, response.processedRows());
        assertEquals(0, response.createdRows());
        assertEquals(1, response.skippedRows());
        assertEquals(1, response.errors().size());
        assertTrue(
                response.errors().get(0).message().contains("Найдено несколько студентов"),
                response.errors().get(0).message()
        );
        assertTrue(
                response.errors().get(0).message().contains("email"),
                response.errors().get(0).message()
        );
    }

    @Test
    @DisplayName("ВКР-формат с повторной шапкой внутри листа импортирует все секции без фейковых ошибок")
    void importFile_WhenThesisTemplateHasRepeatingHeaders_ImportsAllSections() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "thesis-topics.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                thesisTemplateWithTwoSectionsXlsx()
        );

        Student ivanov = Student.builder()
                .id(10L)
                .patronymic("Иванович")
                .user(User.builder()
                        .login("ivanov_i_i")
                        .lastName("Иванов")
                        .firstName("Иван")
                        .build())
                .build();
        Student petrov = Student.builder()
                .id(11L)
                .patronymic("Петрович")
                .user(User.builder()
                        .login("petrov_p_p")
                        .lastName("Петров")
                        .firstName("Пётр")
                        .build())
                .build();
        Student sidorov = Student.builder()
                .id(12L)
                .patronymic("Сидорович")
                .user(User.builder()
                        .login("sidorov_s_s")
                        .lastName("Сидоров")
                        .firstName("Сидор")
                        .build())
                .build();

        Employee sychev = new Employee();
        sychev.setId(20L);
        sychev.setPatronymic("Владимирович");
        sychev.setUser(User.builder()
                .login("sychev_a_v")
                .lastName("Сычев")
                .firstName("Андрей")
                .build());
        Employee malyhin = new Employee();
        malyhin.setId(21L);
        malyhin.setPatronymic("Юрьевич");
        malyhin.setUser(User.builder()
                .login("malyhin_a_yu")
                .lastName("Малыхин")
                .firstName("Алексей")
                .build());

        when(studentRepository.findAll()).thenReturn(List.of(ivanov, petrov, sidorov));
        when(studentTopicAssignmentRepository.findByStudent_Id(any())).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(List.of(sychev, malyhin));
        when(studentTopicAssignmentRepository.save(any(StudentTopicAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentTopicsImportResponse response = studentTopicsService.importFile(file);

        assertEquals(3, response.processedRows(), response.toString());
        assertEquals(3, response.createdRows(), response.toString());
        assertEquals(0, response.skippedRows(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());

        ArgumentCaptor<StudentTopicAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(StudentTopicAssignment.class);
        verify(studentTopicAssignmentRepository, org.mockito.Mockito.times(3))
                .save(assignmentCaptor.capture());

        List<StudentTopicAssignment> saved = assignmentCaptor.getAllValues();
        assertEquals(
                java.util.Set.of(10L, 11L, 12L),
                saved.stream().map(a -> a.getStudent().getId()).collect(java.util.stream.Collectors.toSet())
        );
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

    private static byte[] thesisTemplateWithTwoSectionsXlsx() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("2024");

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(2).setCellValue("2024");
            Row sectionRow1 = sheet.createRow(1);
            sectionRow1.createCell(1).setCellValue("4 курс, ВКР");
            sectionRow1.createCell(3).setCellValue("Информационные системы и сетевые технологии");

            Row headerRow1 = sheet.createRow(2);
            headerRow1.createCell(1).setCellValue("студент");
            headerRow1.createCell(3).setCellValue("ТЕМА курсовой работы (3 курс)");
            headerRow1.createCell(4).setCellValue("ФИО научного руководителя");
            headerRow1.createCell(5).setCellValue("ТЕМА ВКР");

            Row dataRow1 = sheet.createRow(3);
            dataRow1.createCell(0).setCellValue(1);
            dataRow1.createCell(1).setCellValue("Иванов");
            dataRow1.createCell(2).setCellValue("Иван Иванович");
            dataRow1.createCell(3).setCellValue("Курсовая Иванова");
            dataRow1.createCell(4).setCellValue("Сычев А.В.");
            dataRow1.createCell(5).setCellValue("ВКР Иванова");

            Row dataRow2 = sheet.createRow(4);
            dataRow2.createCell(0).setCellValue(2);
            dataRow2.createCell(1).setCellValue("Петров");
            dataRow2.createCell(2).setCellValue("Пётр Петрович");
            dataRow2.createCell(4).setCellValue("Сычев А.В.");
            dataRow2.createCell(5).setCellValue("ВКР Петрова");

            Row sectionRow2 = sheet.createRow(6);
            sectionRow2.createCell(3).setCellValue("Информационные системы в телекоммуникациях");

            Row headerRow2 = sheet.createRow(7);
            headerRow2.createCell(1).setCellValue("студент");
            headerRow2.createCell(3).setCellValue("ТЕМА");
            headerRow2.createCell(4).setCellValue("ФИО научного руководителя");
            headerRow2.createCell(5).setCellValue("ТЕМА ВКР");

            Row dataRow3 = sheet.createRow(8);
            dataRow3.createCell(0).setCellValue(1);
            dataRow3.createCell(1).setCellValue("Сидоров");
            dataRow3.createCell(2).setCellValue("Сидор Сидорович");
            dataRow3.createCell(3).setCellValue("Курсовая Сидорова");
            dataRow3.createCell(4).setCellValue("Малыхин А.Ю.");
            dataRow3.createCell(5).setCellValue("ВКР Сидорова");

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private static byte[] departmentTemplateXlsx() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("2024");
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(2).setCellValue("2024");
            Row groupRow = sheet.createRow(1);
            groupRow.createCell(1).setCellValue("4 курс, ВКР");

            Row headerRow = sheet.createRow(2);
            headerRow.createCell(1).setCellValue("студент");
            headerRow.createCell(3).setCellValue("ТЕМА курсовой работы (3 курс)");
            headerRow.createCell(4).setCellValue("ФИО научного руководителя");
            headerRow.createCell(5).setCellValue("ТЕМА ВКР");

            Row dataRow = sheet.createRow(3);
            dataRow.createCell(0).setCellValue(1);
            dataRow.createCell(1).setCellValue("Иванов");
            dataRow.createCell(2).setCellValue("Иван Иванович");
            dataRow.createCell(3).setCellValue("Классификация текстов");
            dataRow.createCell(4).setCellValue("Сычев А.В.");
            dataRow.createCell(5).setCellValue("Классификация текстов с помощью BERT");

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
