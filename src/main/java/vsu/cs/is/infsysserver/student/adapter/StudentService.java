package vsu.cs.is.infsysserver.student.adapter;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.student.adapter.jpa.DepartmentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentEditRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentImportResponse;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public List<StudentResponse> getAllStudents(boolean isActive) {
        return studentRepository.findByIsDisabled(!isActive).stream()
                .map(StudentResponse::new)
                .toList();
    }

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

    public StudentImportResponse importStudents(MultipartFile file) {
        StudentImportResponse result = new StudentImportResponse();

        if (file == null || file.isEmpty()) {
            result.addError(1, "file", "Файл пустой");
            return result;
        }

        if (file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            result.addError(1, "file", "Поддерживается только формат .xlsx");
            return result;
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                result.addError(1, "file", "Файл пустой");
                return result;
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<Integer, String> headerMap = buildHeaderMap(headerRow);

            validateRequiredHeaders(headerMap, result);

            if (!result.getErrors().isEmpty()) {
                return result;
            }

            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                try {
                    Map<String, String> data = readRow(row, headerMap);

                    String firstName = required(data, "firstName", "Имя");
                    String lastName = required(data, "lastName", "Фамилия");
                    String login = required(data, "login", "Логин");
                    String email = required(data, "email", "Адрес электронной почты");

                    String group = trimToNull(data.get("group"));

                    User user = userRepository.findByLogin(login).orElse(null);
                    boolean created = false;

                    if (user == null) {
                        user = new User();
                        user.setLogin(login);
                        user.setRole(Role.USER);
                        created = true;
                    }

                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);

                    user = userRepository.save(user);

                    Student student = studentRepository.findByUser_Id(user.getId());

                    if (student == null) {
                        student = new Student();
                        student.setUser(user);
                        created = true;
                    }

                    student.setGroup(group);
                    studentRepository.save(student);

                    if (created) {
                        result.incrementCreated();
                    } else {
                        result.incrementUpdated();
                    }

                } catch (Exception exception) {
                    result.addError(i + 1, "row", exception.getMessage());
                }
            }

        } catch (Exception exception) {
            result.addError(1, "file", "Не удалось прочитать файл: " + exception.getMessage());
        }

        return result;
    }

    private Map<Integer, String> buildHeaderMap(Row headerRow) {
        Map<Integer, String> result = new HashMap<>();

        if (headerRow == null) {
            return result;
        }

        for (Cell cell : headerRow) {
            String normalized = normalizeHeader(cellToString(cell));

            switch (normalized) {
                case "имя" -> result.put(cell.getColumnIndex(), "firstName");
                case "фамилия" -> result.put(cell.getColumnIndex(), "lastName");
                case "логин" -> result.put(cell.getColumnIndex(), "login");
                case "адресэлектроннойпочты", "email", "почта" -> result.put(cell.getColumnIndex(), "email");
                case "группы", "группа" -> result.put(cell.getColumnIndex(), "group");
            }
        }

        return result;
    }

    private void validateRequiredHeaders(
            Map<Integer, String> headerMap,
            StudentImportResponse result
    ) {
        Set<String> fields = new HashSet<>(headerMap.values());

        Map<String, String> required = Map.of(
                "firstName", "Имя",
                "lastName", "Фамилия",
                "login", "Логин",
                "email", "Адрес электронной почты"
        );

        for (Map.Entry<String, String> entry : required.entrySet()) {
            if (!fields.contains(entry.getKey())) {
                result.addError(1, "headers", "Отсутствует обязательная колонка: " + entry.getValue());
            }
        }
    }

    private Map<String, String> readRow(Row row, Map<Integer, String> headerMap) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
            Cell cell = row.getCell(entry.getKey());
            result.put(entry.getValue(), trimToNull(cellToString(cell)));
        }

        return result;
    }

    private String required(Map<String, String> data, String field, String label) {
        String value = trimToNull(data.get(field));

        if (value == null) {
            throw new IllegalArgumentException("Поле \"" + label + "\" обязательно");
        }

        return value;
    }

    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (trimToNull(cellToString(cell)) != null) {
                return false;
            }
        }

        return true;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("<[^>]*>", "")
                .toLowerCase()
                .replaceAll("[^a-zа-яё0-9]+", "");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private static <T> void setIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }
}
