package vsu.cs.is.infsysserver.student.adapter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import vsu.cs.is.infsysserver.student.adapter.jpa.DepartmentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentImportResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты импорта студентов")
class StudentServiceImportTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private StudentService studentService;

    private static final Object[] HEADER = {"Имя", "Фамилия", "Логин", "Адрес электронной почты", "Группы"};

    private MockMultipartFile xlsx(Object[][] rows) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < rows[r].length; c++) {
                    Cell cell = row.createCell(c);
                    Object v = rows[r][c];
                    if (v != null) cell.setCellValue(v.toString());
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return new MockMultipartFile("file", "p.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", baos.toByteArray());
        }
    }

    @Test
    @DisplayName("Строка без группы — попадает в skipped, не в created/updated")
    void importStudents_RowWithoutGroup_Skipped() throws Exception {
        var file = xlsx(new Object[][]{
                HEADER,
                {"Артём", "Атанов", "atanov_av", "atanov@cs.vsu.ru", null},
        });

        StudentImportResponse result = studentService.importStudents(file);

        assertEquals(0, result.getCreated());
        assertEquals(0, result.getUpdated());
        assertEquals(1, result.getSkipped());
        assertTrue(result.getErrors().isEmpty());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Имя с отчеством — split по первому пробелу")
    void importStudents_SplitsFirstNameAndPatronymic() throws Exception {
        var file = xlsx(new Object[][]{
                HEADER,
                {"Гагик Арманович", "Асатрян", "16250362", "gagik@gmail.com", "2025_1к_ФКН_09.04.02_Оч_0_25"},
        });
        when(userRepository.findByLogin("16250362")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findByUser_Id(any())).thenReturn(null);

        studentService.importStudents(file);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("Гагик", saved.getFirstName());
        assertEquals("Асатрян", saved.getLastName());

        ArgumentCaptor<Student> studCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studCaptor.capture());
        Student stud = studCaptor.getValue();
        assertEquals("Арманович", stud.getPatronymic());
        assertEquals(2025, stud.getStartYear());
        assertEquals(1, stud.getCourse());
        assertEquals("0", stud.getGroup());
    }

    @Test
    @DisplayName("Бакалаврская группа '2025_4к_ФКН_09.03.02_Оч_3_22' — group=3, start_year=2022, course=4")
    void importStudents_BachelorGroup_ParsesNumberAndAdmissionYear() throws Exception {
        var file = xlsx(new Object[][]{
                HEADER,
                {"Иван", "Иванов", "12345", "iv@cs.vsu.ru", "2025_4к_ФКН_09.03.02_Оч_3_22"},
        });
        when(userRepository.findByLogin("12345")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findByUser_Id(any())).thenReturn(null);

        studentService.importStudents(file);

        ArgumentCaptor<Student> studCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studCaptor.capture());
        Student stud = studCaptor.getValue();
        assertEquals("3", stud.getGroup());
        assertEquals(2022, stud.getStartYear());
        assertEquals(4, stud.getCourse());
    }

    @Test
    @DisplayName("Имя без отчества — patronymic пустой")
    void importStudents_SingleWordName_NoPatronymic() throws Exception {
        var file = xlsx(new Object[][]{
                HEADER,
                {"Иван", "Иванов", "12345", "iv@cs.vsu.ru", "2024_2к_ФКН_09.03.02_Оч_0_24"},
        });
        when(userRepository.findByLogin("12345")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findByUser_Id(any())).thenReturn(null);

        studentService.importStudents(file);

        ArgumentCaptor<Student> studCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studCaptor.capture());
        assertNull(studCaptor.getValue().getPatronymic());
    }

    @Test
    @DisplayName("Группа в нестандартном формате — start_year/course = null, импорт не падает")
    void importStudents_NonStandardGroup_StartYearNull() throws Exception {
        var file = xlsx(new Object[][]{
                HEADER,
                {"Иван", "Иванов", "12345", "iv@cs.vsu.ru", "Random_Group_Format"},
        });
        when(userRepository.findByLogin("12345")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findByUser_Id(any())).thenReturn(null);

        StudentImportResponse result = studentService.importStudents(file);

        assertEquals(1, result.getCreated());

        ArgumentCaptor<Student> studCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studCaptor.capture());
        Student stud = studCaptor.getValue();
        assertNull(stud.getStartYear());
        assertNull(stud.getCourse());
        assertEquals("Random_Group_Format", stud.getGroup());
    }

    @Test
    @DisplayName("Новый user создаётся с password=\"\" (не null)")
    void importStudents_NewUser_HasEmptyPassword() throws Exception {
        var file = xlsx(new Object[][]{
                HEADER,
                {"Иван", "Иванов", "12345", "iv@cs.vsu.ru", "2024_2к_ФКН_09.03.02_Оч_0_24"},
        });
        when(userRepository.findByLogin("12345")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findByUser_Id(any())).thenReturn(null);

        studentService.importStudents(file);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("", userCaptor.getValue().getPassword());
    }

    @Test
    @DisplayName("CSV файл — парсится так же, как xlsx")
    void importStudents_CsvFile_ParsesSameWayAsXlsx() {
        String csv = "Имя,Фамилия,Логин,Адрес электронной почты,Группы\n"
                + "Гагик Арманович,Асатрян,16250362,gagik@gmail.com,2025_1к_ФКН_09.04.02_Оч_0_25\n";
        var file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csv.getBytes()
        );
        when(userRepository.findByLogin("16250362")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findByUser_Id(any())).thenReturn(null);

        StudentImportResponse result = studentService.importStudents(file);

        assertEquals(1, result.getCreated());
        assertEquals(0, result.getUpdated());
        assertEquals(0, result.getSkipped());

        ArgumentCaptor<Student> studCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studCaptor.capture());
        Student stud = studCaptor.getValue();
        assertEquals("Арманович", stud.getPatronymic());
        assertEquals(2025, stud.getStartYear());
        assertEquals(1, stud.getCourse());
    }

    @Test
    @DisplayName("Не поддерживаемое расширение — ошибка с указанием на .xlsx и .csv")
    void importStudents_UnsupportedExtension_AddsError() {
        var file = new MockMultipartFile(
                "file", "list.txt", "text/plain", "garbage".getBytes()
        );

        StudentImportResponse result = studentService.importStudents(file);

        assertEquals(0, result.getCreated());
        assertEquals(0, result.getUpdated());
        assertEquals(1, result.getErrors().size());
        verify(userRepository, never()).save(any());
    }
}
