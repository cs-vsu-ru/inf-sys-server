package vsu.cs.is.infsysserver.student.adapter;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.student.adapter.jpa.DepartmentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.request.StudentEditRequest;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentImportResponse;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.StudentTopicAssignmentRepository;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity.StudentTopicAssignment;
import vsu.cs.is.infsysserver.user.adapter.jpa.UserRepository;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentService {

    private static final Pattern START_YEAR_PATTERN = Pattern.compile("^(\\d{4})_");
    private static final Pattern COURSE_PATTERN = Pattern.compile("_(\\d+)к_");
    private static final String GOOGLE_SHEETS_HOST = "docs.google.com";
    private static final List<Charset> CSV_CHARSETS = List.of(
            StandardCharsets.UTF_8,
            Charset.forName("windows-1251")
    );

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentTopicAssignmentRepository studentTopicAssignmentRepository;
    private final RestTemplate restTemplate;

    public List<StudentResponse> getAllStudents() {
        List<Student> students = studentRepository.findAll(
                Sort.by(Sort.Order.asc("isDisabled"), Sort.Order.asc("id"))
        );
        if (students.isEmpty()) {
            return List.of();
        }

        Set<Long> ids = students.stream()
                .map(Student::getId)
                .collect(Collectors.toSet());

        Map<Long, StudentTopicAssignment> assignments = studentTopicAssignmentRepository
                .findAllByStudent_IdIn(ids)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getStudent().getId(),
                        Function.identity()
                ));

        return students.stream()
                .map(s -> StudentResponse.fromStudentAndTopics(
                        s, Optional.ofNullable(assignments.get(s.getId()))
                ))
                .toList();
    }

    public Optional<StudentResponse> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(student -> StudentResponse.fromStudentAndTopics(
                        student,
                        studentTopicAssignmentRepository.findByStudent_Id(student.getId())
                ));
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

        return StudentResponse.fromStudentAndTopics(
                student,
                studentTopicAssignmentRepository.findByStudent_Id(student.getId())
        );
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

        return StudentResponse.fromStudentAndTopics(
                student,
                studentTopicAssignmentRepository.findByStudent_Id(student.getId())
        );
    }

    public StudentImportResponse importStudents(MultipartFile file) {
        StudentImportResponse result = new StudentImportResponse();

        if (file == null || file.isEmpty()) {
            result.addError(1, "file", "Файл пустой");
            return result;
        }

        String fileName = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .orElse("");

        try {
            List<ParsedStudentRow> rows;
            if (fileName.endsWith(".xlsx")) {
                rows = parseXlsx(file);
            } else if (fileName.endsWith(".csv")) {
                rows = parseCsv(file.getBytes());
            } else {
                result.addError(1, "file", "Поддерживаются только файлы .xlsx и .csv");
                return result;
            }
            return importRows(rows, result);
        } catch (HeadersValidationException exception) {
            result.addError(1, "headers", exception.getMessage());
            return result;
        } catch (EmptyFileException exception) {
            result.addError(1, "file", "Файл пустой");
            return result;
        } catch (Exception exception) {
            result.addError(1, "file", "Не удалось прочитать файл: " + exception.getMessage());
            return result;
        }
    }

    public StudentImportResponse importStudentsFromGoogleSheet(String url) {
        StudentImportResponse result = new StudentImportResponse();
        try {
            URI exportUri = buildGoogleSheetCsvExportUri(url);
            byte[] content = downloadGoogleSheet(exportUri);
            return importRows(parseCsv(content), result);
        } catch (HeadersValidationException exception) {
            result.addError(1, "headers", exception.getMessage());
            return result;
        } catch (EmptyFileException exception) {
            result.addError(1, "file", "Таблица пустая");
            return result;
        } catch (IllegalArgumentException exception) {
            result.addError(1, "url", exception.getMessage());
            return result;
        } catch (Exception exception) {
            result.addError(1, "file", "Не удалось прочитать таблицу: " + exception.getMessage());
            return result;
        }
    }

    private StudentImportResponse importRows(List<ParsedStudentRow> rows, StudentImportResponse result) {
        for (ParsedStudentRow row : rows) {
            try {
                String firstNameFull = requireValue(row.firstNameFull(), "Имя");
                String lastName = requireValue(row.lastName(), "Фамилия");
                String login = requireValue(row.login(), "Логин");
                String email = requireValue(row.email(), "Адрес электронной почты");

                String group = trimToNull(row.group());

                if (group == null) {
                    result.incrementSkipped();
                    continue;
                }

                String[] nameParts = firstNameFull.split("\\s+", 2);
                String firstName = nameParts[0];
                String patronymic = nameParts.length > 1 ? nameParts[1] : null;

                Integer startYear = null;
                Integer course = null;
                Matcher startYearMatcher = START_YEAR_PATTERN.matcher(group);
                if (startYearMatcher.find()) {
                    try {
                        startYear = Integer.parseInt(startYearMatcher.group(1));
                    } catch (NumberFormatException ignored) {
                    }
                }
                Matcher courseMatcher = COURSE_PATTERN.matcher(group);
                if (courseMatcher.find()) {
                    try {
                        course = Integer.parseInt(courseMatcher.group(1));
                    } catch (NumberFormatException ignored) {
                    }
                }

                User user = userRepository.findByLogin(login).orElse(null);
                boolean created = false;

                Optional<User> userWithSameEmail = userRepository.findByEmail(email);
                if (userWithSameEmail.isPresent()
                        && (user == null || !userWithSameEmail.get().getId().equals(user.getId()))) {
                    String otherLogin = userWithSameEmail.get().getLogin();
                    throw new IllegalArgumentException(
                            "Email '" + email + "' уже используется другим пользователем (логин: "
                                    + otherLogin + ")"
                    );
                }

                if (user == null) {
                    user = new User();
                    user.setLogin(login);
                    user.setRole(Role.USER);
                    user.setPassword("");
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

                student.setPatronymic(patronymic);
                student.setGroup(group);
                student.setStartYear(startYear);
                student.setCourse(course);
                studentRepository.save(student);

                if (created) {
                    result.incrementCreated();
                } else {
                    result.incrementUpdated();
                }
            } catch (Exception exception) {
                result.addError(row.rowNumber(), "row", exception.getMessage());
            }
        }
        return result;
    }

    private List<ParsedStudentRow> parseXlsx(MultipartFile file) throws java.io.IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new EmptyFileException();
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<Integer, String> headerMap = buildHeaderMap(headerRow);
            validateRequiredHeadersOrThrow(headerMap);

            List<ParsedStudentRow> rows = new ArrayList<>();
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                Map<String, String> data = readRow(row, headerMap);
                rows.add(new ParsedStudentRow(
                        i + 1,
                        data.get("firstName"),
                        data.get("lastName"),
                        data.get("login"),
                        data.get("email"),
                        data.get("group")
                ));
            }
            return rows;
        }
    }

    private List<ParsedStudentRow> parseCsv(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new EmptyFileException();
        }

        // Пробуем UTF-8, при наличии маркера невалидной кодировки — windows-1251.
        String utf8Content = new String(bytes, StandardCharsets.UTF_8);
        Charset chosenCharset = utf8Content.contains("�")
                ? Charset.forName("windows-1251")
                : StandardCharsets.UTF_8;

        IllegalStateException lastException = null;
        for (Charset charset : chosenCharset == StandardCharsets.UTF_8
                ? List.of(StandardCharsets.UTF_8)
                : CSV_CHARSETS) {
            try {
                return parseCsvWithCharset(bytes, charset);
            } catch (IllegalStateException exception) {
                lastException = exception;
            }
        }
        throw lastException != null
                ? lastException
                : new IllegalStateException("Не удалось прочитать CSV-файл");
    }

    private List<ParsedStudentRow> parseCsvWithCharset(byte[] bytes, Charset charset) {
        String content = new String(bytes, charset);
        if (!StringUtils.hasText(content)) {
            throw new EmptyFileException();
        }

        char delimiter = detectCsvDelimiter(content);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .setDelimiter(delimiter)
                .build();

        try (CSVParser parser = csvFormat.parse(new StringReader(content))) {
            Map<Integer, String> headerMap = buildHeaderMapFromCsv(parser.getHeaderMap());
            validateRequiredHeadersOrThrow(headerMap);

            List<ParsedStudentRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> data = readCsvRow(record, headerMap);
                if (data.values().stream().allMatch(v -> v == null || v.isBlank())) {
                    continue;
                }
                rows.add(new ParsedStudentRow(
                        (int) record.getRecordNumber() + 1,
                        data.get("firstName"),
                        data.get("lastName"),
                        data.get("login"),
                        data.get("email"),
                        data.get("group")
                ));
            }
            return rows;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Не удалось прочитать CSV-файл", exception);
        }
    }

    private Map<Integer, String> buildHeaderMapFromCsv(Map<String, Integer> rawHeaderPositions) {
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : rawHeaderPositions.entrySet()) {
            String normalized = normalizeHeader(entry.getKey());
            String field = HEADER_FIELD_MAP.get(normalized);
            if (field != null) {
                result.put(entry.getValue(), field);
            }
        }
        return result;
    }

    private static char detectCsvDelimiter(String content) {
        String firstNonBlankLine = Arrays.stream(content.split("\\R"))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
        long semicolonCount = firstNonBlankLine.chars().filter(c -> c == ';').count();
        long commaCount = firstNonBlankLine.chars().filter(c -> c == ',').count();
        return semicolonCount > commaCount ? ';' : ',';
    }

    private Map<String, String> readCsvRow(CSVRecord record, Map<Integer, String> headerMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
            int index = entry.getKey();
            if (index < record.size()) {
                result.put(entry.getValue(), record.get(index));
            }
        }
        return result;
    }

    private void validateRequiredHeadersOrThrow(Map<Integer, String> headerMap) {
        Set<String> fields = new HashSet<>(headerMap.values());
        Map<String, String> required = Map.of(
                "firstName", "Имя",
                "lastName", "Фамилия",
                "login", "Логин",
                "email", "Адрес электронной почты"
        );
        for (Map.Entry<String, String> entry : required.entrySet()) {
            if (!fields.contains(entry.getKey())) {
                throw new HeadersValidationException(
                        "Отсутствует обязательная колонка: " + entry.getValue()
                );
            }
        }
    }

    /**
     * TODO: вынести вместе с downloadGoogleSheet в общий хелпер
     * (используется и в StudentTopicsService).
     */
    private URI buildGoogleSheetCsvExportUri(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("Ссылка на Google Sheets не должна быть пустой");
        }

        URI sourceUri;
        try {
            sourceUri = new URI(url.trim());
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Некорректная ссылка на Google Sheets", exception);
        }

        String host = Optional.ofNullable(sourceUri.getHost()).orElse("");
        if (!GOOGLE_SHEETS_HOST.equalsIgnoreCase(host)) {
            throw new IllegalArgumentException("Поддерживаются только ссылки на docs.google.com");
        }

        String spreadsheetId = extractSpreadsheetId(Optional.ofNullable(sourceUri.getPath()).orElse(""));
        String gid = extractGid(sourceUri);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://" + GOOGLE_SHEETS_HOST + "/spreadsheets/d/" + spreadsheetId + "/export")
                .queryParam("format", "csv");
        if (StringUtils.hasText(gid)) {
            builder.queryParam("gid", gid);
        }
        return builder.build(true).toUri();
    }

    private byte[] downloadGoogleSheet(URI uri) {
        try {
            byte[] body = restTemplate.getForObject(uri, byte[].class);
            if (body == null || body.length == 0) {
                throw new IllegalStateException("Не удалось получить данные из Google Sheets");
            }
            return body;
        } catch (RestClientException exception) {
            throw new IllegalStateException("Не удалось скачать Google Sheets по ссылке", exception);
        }
    }

    private static String extractSpreadsheetId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("d".equals(parts[i]) && StringUtils.hasText(parts[i + 1])) {
                return parts[i + 1];
            }
        }
        throw new IllegalArgumentException("Не удалось определить идентификатор Google Sheets из ссылки");
    }

    private static String extractGid(URI uri) {
        Map<String, String> queryParams = splitParams(uri.getQuery());
        if (queryParams.containsKey("gid")) {
            return queryParams.get("gid");
        }
        return splitParams(uri.getFragment()).getOrDefault("gid", "");
    }

    private static Map<String, String> splitParams(String rawParams) {
        Map<String, String> params = new LinkedHashMap<>();
        if (!StringUtils.hasText(rawParams)) {
            return params;
        }
        for (String param : rawParams.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && StringUtils.hasText(pair[0])) {
                params.put(pair[0], pair[1]);
            }
        }
        return params;
    }

    private static String requireValue(String value, String label) {
        String trimmed = value == null ? null : value.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            throw new IllegalArgumentException("Поле \"" + label + "\" обязательно");
        }
        return trimmed;
    }

    private record ParsedStudentRow(
            int rowNumber,
            String firstNameFull,
            String lastName,
            String login,
            String email,
            String group
    ) {
    }

    private static class HeadersValidationException extends RuntimeException {
        HeadersValidationException(String message) {
            super(message);
        }
    }

    private static class EmptyFileException extends RuntimeException {
    }

    private static final Map<String, String> HEADER_FIELD_MAP = Map.of(
            "имя", "firstName",
            "фамилия", "lastName",
            "логин", "login",
            "адресэлектроннойпочты", "email",
            "email", "email",
            "почта", "email",
            "группы", "group",
            "группа", "group"
    );

    private Map<Integer, String> buildHeaderMap(Row headerRow) {
        Map<Integer, String> result = new HashMap<>();

        if (headerRow == null) {
            return result;
        }

        for (Cell cell : headerRow) {
            String normalized = normalizeHeader(cellToString(cell));
            String field = HEADER_FIELD_MAP.get(normalized);
            if (field != null) {
                result.put(cell.getColumnIndex(), field);
            }
        }

        return result;
    }

    private Map<String, String> readRow(Row row, Map<Integer, String> headerMap) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
            Cell cell = row.getCell(entry.getKey());
            result.put(entry.getValue(), trimToNull(cellToString(cell)));
        }

        return result;
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
