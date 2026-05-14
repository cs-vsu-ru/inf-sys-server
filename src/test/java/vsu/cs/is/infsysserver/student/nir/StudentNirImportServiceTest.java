package vsu.cs.is.infsysserver.student.nir;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import vsu.cs.is.infsysserver.employee.adapter.jpa.EmployeeRepository;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.nir.adapter.jpa.StudentNirRecordRepository;
import vsu.cs.is.infsysserver.student.nir.adapter.jpa.entity.StudentNirRecord;
import vsu.cs.is.infsysserver.student.nir.adapter.rest.dto.response.StudentNirImportResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты StudentNirImportService")
class StudentNirImportServiceTest {

    @Mock
    private StudentNirRecordRepository nirRecordRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private StudentNirImportService service;

    @BeforeEach
    void setUp() {
        service = new StudentNirImportService(nirRecordRepository, studentRepository, employeeRepository);
    }

    @Test
    @DisplayName("Импорт основного листа создаёт запись, матчит руководителя по формату 'Доц. Ф.И. Фамилия'")
    void importFile_WhenBaseSheetIsValid_CreatesRecord() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nir.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                singleSheetTemplateXlsx()
        );

        Student student = Student.builder()
                .id(10L)
                .patronymic("Иванович")
                .user(User.builder()
                        .login("ivanov_i_i")
                        .lastName("Иванов")
                        .firstName("Иван")
                        .build())
                .build();
        Employee chernyshov = new Employee();
        chernyshov.setId(20L);
        chernyshov.setPatronymic("Константинович");
        chernyshov.setUser(User.builder()
                .login("chernyshov_m_k")
                .lastName("Чернышов")
                .firstName("Михаил")
                .build());

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(employeeRepository.findAll()).thenReturn(List.of(chernyshov));
        when(nirRecordRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(nirRecordRepository.save(any(StudentNirRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentNirImportResponse response = service.importFile(file);

        assertEquals(1, response.processedRows(), response.toString());
        assertEquals(1, response.createdRows(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());

        ArgumentCaptor<StudentNirRecord> captor = ArgumentCaptor.forClass(StudentNirRecord.class);
        verify(nirRecordRepository).save(captor.capture());
        StudentNirRecord saved = captor.getValue();
        assertEquals(10L, saved.getStudent().getId());
        assertEquals("ivanov_i_i", saved.getStudentLogin());
        assertEquals("Иванов Иван Иванович", saved.getStudentFullName());
        assertNotNull(saved.getSupervisorEmployee());
        assertEquals(20L, saved.getSupervisorEmployee().getId());
        assertEquals("Создание пользовательского шейдера на языке HLSL", saved.getThesisTopic());
        assertEquals("3", saved.getNir7Grade());
        assertEquals("есть", saved.getNir7Docs());
        assertFalse(saved.isDebtor());
    }

    @Test
    @DisplayName("Лист с 'задолж' помечает записи как debtor=true")
    void importFile_WhenSheetIsDebtors_MarksDebtorFlag() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nir.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                debtorsSheetTemplateXlsx()
        );

        Student student = Student.builder()
                .id(11L)
                .patronymic("Петрович")
                .user(User.builder()
                        .login("petrov_p_p")
                        .lastName("Петров")
                        .firstName("Пётр")
                        .build())
                .build();

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(employeeRepository.findAll()).thenReturn(List.of());
        when(nirRecordRepository.findByStudent_Id(11L)).thenReturn(Optional.empty());
        when(nirRecordRepository.save(any(StudentNirRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentNirImportResponse response = service.importFile(file);

        assertEquals(1, response.processedRows(), response.toString());
        assertEquals(1, response.createdRows(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());

        ArgumentCaptor<StudentNirRecord> captor = ArgumentCaptor.forClass(StudentNirRecord.class);
        verify(nirRecordRepository).save(captor.capture());
        StudentNirRecord saved = captor.getValue();
        assertTrue(saved.isDebtor(), "Студент с листа задолженников должен быть помечен как debtor");
        assertNull(saved.getSupervisorEmployee(), "Если руководитель не найден — FK остаётся null");
        assertEquals("Доц. Петров П.П.", saved.getSupervisorFullName());
    }

    @Test
    @DisplayName("Строки-сводки (ВСЕГО / N группа) пропускаются и не попадают в processedRows")
    void importFile_WhenSheetHasRollupRows_SkipsThemSilently() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nir.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                templateWithRollupRowsXlsx()
        );

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
        when(employeeRepository.findAll()).thenReturn(List.of());
        when(nirRecordRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(nirRecordRepository.save(any(StudentNirRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentNirImportResponse response = service.importFile(file);

        assertEquals(1, response.processedRows(), response.toString());
        assertEquals(1, response.createdRows(), response.toString());
        assertTrue(response.errors().isEmpty(), response.errors().toString());
    }

    @Test
    @DisplayName("Если студент не найден — строка попадает в errors, но импорт продолжается")
    void importFile_WhenStudentNotFound_ReportsError() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nir.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                singleSheetTemplateXlsx()
        );

        when(studentRepository.findAll()).thenReturn(List.of());
        when(employeeRepository.findAll()).thenReturn(List.of());

        StudentNirImportResponse response = service.importFile(file);

        assertEquals(1, response.processedRows());
        assertEquals(0, response.createdRows());
        assertEquals(1, response.skippedRows());
        assertEquals(1, response.errors().size());
        assertTrue(
                response.errors().get(0).message().contains("не найден"),
                response.errors().get(0).message()
        );
    }

    private static byte[] singleSheetTemplateXlsx() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("2022 год набора");
            writeHeader(sheet, 1);
            Row data = sheet.createRow(2);
            data.createCell(0).setCellValue(1);
            data.createCell(1).setCellValue("Иванов Иван Иванович");
            data.createCell(2).setCellValue("Доц. М.К. Чернышов");
            data.createCell(3).setCellValue("Создание пользовательского шейдера на языке HLSL");
            data.createCell(4).setCellValue("3");
            data.createCell(5).setCellValue("есть");
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static byte[] debtorsSheetTemplateXlsx() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("2022 год набора (задолженники)");
            writeHeader(sheet, 1);
            Row data = sheet.createRow(2);
            data.createCell(0).setCellValue(1);
            data.createCell(1).setCellValue("Петров Пётр Петрович");
            data.createCell(2).setCellValue("Доц. Петров П.П.");
            data.createCell(3).setCellValue("Старая тема");
            data.createCell(4).setCellValue("н/я");
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static byte[] templateWithRollupRowsXlsx() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("2022 год набора");
            writeHeader(sheet, 1);

            Row dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue(1);
            dataRow.createCell(1).setCellValue("Иванов Иван Иванович");
            dataRow.createCell(2).setCellValue("Доц. Иванов И.И.");
            dataRow.createCell(3).setCellValue("Тема");
            dataRow.createCell(4).setCellValue("3");

            Row rollupTotal = sheet.createRow(4);
            rollupTotal.createCell(1).setCellValue("ВСЕГО");
            rollupTotal.createCell(2).setCellValue("56");

            Row rollupGroup = sheet.createRow(5);
            rollupGroup.createCell(0).setCellValue(1);
            rollupGroup.createCell(1).setCellValue("1 группа");
            rollupGroup.createCell(2).setCellValue("19");

            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static void writeHeader(Sheet sheet, int headerRowIndex) {
        Row header = sheet.createRow(headerRowIndex);
        header.createCell(0).setCellValue("№ п/п");
        header.createCell(1).setCellValue("ФИО студента");
        header.createCell(2).setCellValue("ФИО научного руководителя");
        header.createCell(3).setCellValue("ТЕМА ВКР");
        header.createCell(4).setCellValue("Практика НИР, 7 семестр");
        header.createCell(5).setCellValue("наличие документов");
        header.createCell(6).setCellValue("Предзащита 1 (апрель)");
        header.createCell(7).setCellValue("Предзащита 2 (май)");
        header.createCell(8).setCellValue("Практика НИР, 8 семестр");
        header.createCell(9).setCellValue("наличие документов");
    }
}
