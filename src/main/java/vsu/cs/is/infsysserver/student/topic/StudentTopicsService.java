package vsu.cs.is.infsysserver.student.topic;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import vsu.cs.is.infsysserver.employee.adapter.jpa.EmployeeRepository;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.StudentTopicAssignmentRepository;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity.StudentTopicAssignment;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response.StudentTopicsImportErrorResponse;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response.StudentTopicsImportResponse;
import vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response.StudentTopicsResponse;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StudentTopicsService {

    private static final String HEADER_STUDENT_LOGIN = "Логин студента";
    private static final String HEADER_STUDENT_FULL_NAME = "ФИО студента";
    private static final String HEADER_STUDENT_ID = "ID студента";
    private static final String HEADER_COURSE_WORK_TOPIC = "Тема курсовой";
    private static final String HEADER_THESIS_TOPIC = "Тема ВКР";
    private static final String HEADER_SUPERVISOR_LOGIN = "Логин научного руководителя";
    private static final String HEADER_SUPERVISOR_FULL_NAME = "ФИО научного руководителя";
    private static final String HEADER_SUPERVISOR_ID = "ID научного руководителя";
    private static final String GOOGLE_SHEETS_HOST = "docs.google.com";
    private static final List<String> STUDENT_ID_HEADERS = List.of(
            HEADER_STUDENT_ID,
            "student_id",
            "id студента",
            "ид студента"
    );
    private static final List<String> STUDENT_LOGIN_HEADERS = List.of(
            HEADER_STUDENT_LOGIN,
            "login студента",
            "student_login"
    );
    private static final List<String> STUDENT_FULL_NAME_HEADERS = List.of(
            HEADER_STUDENT_FULL_NAME,
            "фио студента",
            "студент",
            "фио обучающегося"
    );
    private static final List<String> COURSE_WORK_TOPIC_HEADERS = List.of(
            HEADER_COURSE_WORK_TOPIC,
            "тема курсовой работы",
            "тема курсовой работы (3 курс)",
            "тема"
    );
    private static final List<String> THESIS_TOPIC_HEADERS = List.of(
            HEADER_THESIS_TOPIC,
            "тема выпускной квалификационной работы",
            "тема дипломной работы",
            "тема диплома"
    );
    private static final List<String> SUPERVISOR_ID_HEADERS = List.of(
            HEADER_SUPERVISOR_ID,
            "supervisor_id",
            "id руководителя",
            "id научного руководителя"
    );
    private static final List<String> SUPERVISOR_LOGIN_HEADERS = List.of(
            HEADER_SUPERVISOR_LOGIN,
            "login научного руководителя",
            "supervisor_login"
    );
    private static final List<String> SUPERVISOR_FULL_NAME_HEADERS = List.of(
            HEADER_SUPERVISOR_FULL_NAME,
            "фио преподавателя",
            "преподаватель",
            "научный руководитель",
            "руководитель"
    );
    private static final List<Charset> CSV_CHARSETS = List.of(
            StandardCharsets.UTF_8,
            Charset.forName("windows-1251")
    );

    private final StudentTopicAssignmentRepository studentTopicAssignmentRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;
    private final RestTemplate restTemplate;

    public StudentTopicsImportResponse importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Файл не должен быть пустым"
            );
        }

        return importRows(parseRows(file));
    }

    public StudentTopicsImportResponse importGoogleSheet(String url) {
        URI exportUri = buildGoogleSheetCsvExportUri(url);
        byte[] content = downloadGoogleSheet(exportUri);
        return importRows(parseCsv(content));
    }

    public Optional<StudentTopicsResponse> getCurrentStudentTopics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String studentLogin = normalizeLogin(String.valueOf(authentication.getPrincipal()));
        return getTopicsByStudentLogin(studentLogin);
    }

    public Optional<StudentTopicsResponse> getTopicsByStudentLogin(String studentLogin) {
        String normalizedLogin = normalizeLogin(studentLogin);
        return studentTopicAssignmentRepository.findByStudent_User_Login(normalizedLogin)
                .or(() -> studentTopicAssignmentRepository.findByStudentLogin(normalizedLogin))
                .map(StudentTopicsResponse::from);
    }

    public Optional<StudentTopicsResponse> getTopicsByStudentId(Long studentId) {
        return studentTopicAssignmentRepository.findByStudent_Id(studentId)
                .map(StudentTopicsResponse::from);
    }

    private StudentTopicsImportResponse importRows(List<ParsedStudentTopicRow> rows) {
        Set<String> seenStudentLogins = new HashSet<>();
        Set<Long> seenStudentIds = new HashSet<>();
        List<StudentTopicsImportErrorResponse> errors = new ArrayList<>();
        int processedRows = 0;
        int createdRows = 0;
        int updatedRows = 0;

        for (ParsedStudentTopicRow row : rows) {
            if (!row.hasStudentReference() && !row.hasSupervisorReference()) {
                continue;
            }

            processedRows++;
            String studentLogin = normalizeLogin(row.studentLogin());
            Long studentId = parseLong(row.studentId());
            String validationError = validateRow(row, studentLogin, studentId);
            if (validationError != null) {
                errors.add(new StudentTopicsImportErrorResponse(
                        row.rowNumber(),
                        studentLogin,
                        validationError
                ));
                continue;
            }

            String duplicateKey = studentId != null ? "ID " + studentId : studentLogin;
            boolean duplicate = studentId != null
                    ? !seenStudentIds.add(studentId)
                    : StringUtils.hasText(studentLogin) && !seenStudentLogins.add(studentLogin);
            if (duplicate) {
                errors.add(new StudentTopicsImportErrorResponse(
                        row.rowNumber(),
                        studentLogin,
                        "Дублирующийся студент в файле: " + duplicateKey
                ));
                continue;
            }

            try {
                Student student = findStudent(row, studentLogin, studentId);
                Employee supervisor = findSupervisor(row);

                Optional<StudentTopicAssignment> existingAssignment = findExistingAssignment(student, studentLogin);
                StudentTopicAssignment assignment = existingAssignment.orElseGet(StudentTopicAssignment::new);

                assignment.setStudent(student);
                assignment.setStudentLogin(normalizeLogin(student.getUser().getLogin()));
                assignment.setStudentFullName(normalizeText(resolveStudentFullName(row, student)));
                assignment.setCourseWorkTopic(normalizeNullableText(row.courseWorkTopic()));
                assignment.setThesisTopic(normalizeNullableText(row.thesisTopic()));
                assignment.setSupervisorLogin(normalizeLogin(supervisor.getUser().getLogin()));
                assignment.setSupervisorFullName(normalizeText(resolveEmployeeFullName(row, supervisor)));
                assignment.setSupervisorEmployee(supervisor);
                assignment.setImportedAt(LocalDateTime.now());

                studentTopicAssignmentRepository.save(assignment);

                if (existingAssignment.isPresent()) {
                    updatedRows++;
                } else {
                    createdRows++;
                }
            } catch (Exception exception) {
                errors.add(new StudentTopicsImportErrorResponse(
                        row.rowNumber(),
                        studentLogin,
                        "Не удалось сохранить строку: " + rootCauseMessage(exception)
                ));
            }
        }

        return new StudentTopicsImportResponse(
                processedRows,
                createdRows,
                updatedRows,
                errors.size(),
                List.copyOf(errors)
        );
    }

    private Optional<StudentTopicAssignment> findExistingAssignment(Student student, String studentLogin) {
        return studentTopicAssignmentRepository.findByStudent_Id(student.getId())
                .or(() -> StringUtils.hasText(studentLogin)
                        ? studentTopicAssignmentRepository.findByStudentLogin(studentLogin)
                        : Optional.empty());
    }

    private List<ParsedStudentTopicRow> parseRows(MultipartFile file) {
        String fileName = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .orElse("");

        if (fileName.endsWith(".xlsx")) {
            return parseXlsx(file);
        }
        if (fileName.endsWith(".csv")) {
            return parseCsv(file);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Поддерживаются только файлы .xlsx и .csv"
        );
    }

    private List<ParsedStudentTopicRow> parseCsv(MultipartFile file) {
        try {
            return parseCsv(file.getBytes());
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось прочитать CSV-файл",
                    exception
            );
        }
    }

    private List<ParsedStudentTopicRow> parseCsv(byte[] bytes) {
        String utf8Content = new String(bytes, StandardCharsets.UTF_8);
        if (!utf8Content.contains("\uFFFD")) {
            return parseCsv(bytes, StandardCharsets.UTF_8);
        }

        ResponseStatusException lastException = null;

        for (Charset charset : CSV_CHARSETS) {
            try {
                return parseCsv(bytes, charset);
            } catch (ResponseStatusException exception) {
                lastException = exception;
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Не удалось прочитать CSV-файл"
        );
    }

    private List<ParsedStudentTopicRow> parseCsv(byte[] bytes, Charset charset) {
        String content = new String(bytes, charset);
        if (!StringUtils.hasText(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV-файл пуст");
        }

        char delimiter = detectDelimiter(content);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .setDelimiter(delimiter)
                .build();

        try (CSVParser parser = csvFormat.parse(new StringReader(content))) {
            Map<String, Integer> headerPositions = normalizeHeaderPositions(parser.getHeaderMap());
            ensureUsableHeaders(headerPositions);

            List<ParsedStudentTopicRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                ParsedStudentTopicRow row = new ParsedStudentTopicRow(
                        (int) record.getRecordNumber() + 1,
                        csvValue(record, headerPositions, STUDENT_ID_HEADERS),
                        csvValue(record, headerPositions, STUDENT_LOGIN_HEADERS),
                        csvValue(record, headerPositions, STUDENT_FULL_NAME_HEADERS),
                        csvValue(record, headerPositions, COURSE_WORK_TOPIC_HEADERS),
                        csvValue(record, headerPositions, THESIS_TOPIC_HEADERS),
                        csvValue(record, headerPositions, SUPERVISOR_ID_HEADERS),
                        csvValue(record, headerPositions, SUPERVISOR_LOGIN_HEADERS),
                        csvValue(record, headerPositions, SUPERVISOR_FULL_NAME_HEADERS)
                );
                if (!row.isBlank()) {
                    rows.add(row);
                }
            }
            return rows;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось прочитать CSV-файл",
                    exception
            );
        }
    }

    private List<ParsedStudentTopicRow> parseXlsx(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "В Excel-файле нет листов");
            }

            Sheet sheet = workbook.getSheetAt(0);
            HeaderMapping headerMapping = findHeaderMapping(sheet, new DataFormatter());
            if (headerMapping == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "В Excel-файле не найдены колонки студента, темы и научного руководителя"
                );
            }

            List<ParsedStudentTopicRow> rows = new ArrayList<>();
            for (int rowIndex = headerMapping.headerRowIndex() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                ParsedStudentTopicRow parsedRow = new ParsedStudentTopicRow(
                        rowIndex + 1,
                        xlsxValue(row, headerMapping.studentIdColumn(), headerMapping.formatter()),
                        xlsxValue(row, headerMapping.studentLoginColumn(), headerMapping.formatter()),
                        xlsxStudentFullName(row, headerMapping),
                        xlsxValue(row, headerMapping.courseWorkTopicColumn(), headerMapping.formatter()),
                        xlsxValue(row, headerMapping.thesisTopicColumn(), headerMapping.formatter()),
                        xlsxValue(row, headerMapping.supervisorIdColumn(), headerMapping.formatter()),
                        xlsxValue(row, headerMapping.supervisorLoginColumn(), headerMapping.formatter()),
                        xlsxValue(row, headerMapping.supervisorFullNameColumn(), headerMapping.formatter())
                );
                if (!parsedRow.isBlank()) {
                    rows.add(parsedRow);
                }
            }
            return rows;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось прочитать Excel-файл",
                    exception
            );
        }
    }

    private static String csvValue(CSVRecord record, Map<String, Integer> headerPositions, List<String> headerNames) {
        Integer position = findColumn(headerPositions, headerNames);
        if (position == null || position >= record.size()) {
            return "";
        }
        return record.get(position);
    }

    private static String xlsxValue(Row row, Integer position, DataFormatter formatter) {
        if (row == null || position == null) {
            return "";
        }
        if (row.getCell(position) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(position));
    }

    private static String xlsxStudentFullName(Row row, HeaderMapping headerMapping) {
        String fullName = xlsxValue(row, headerMapping.studentFullNameColumn(), headerMapping.formatter());
        if (!headerMapping.studentNameContinuesInNextColumn()) {
            return fullName;
        }
        String nextPart = xlsxValue(
                row,
                headerMapping.studentFullNameColumn() + 1,
                headerMapping.formatter()
        );
        return normalizeText(fullName + " " + nextPart);
    }

    private static Map<String, Integer> normalizeHeaderPositions(Map<String, Integer> rawHeaderPositions) {
        return rawHeaderPositions.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> normalizeHeaderKey(entry.getKey()),
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private static void ensureUsableHeaders(Map<String, Integer> headerPositions) {
        if (findColumn(headerPositions, STUDENT_ID_HEADERS) == null
                && findColumn(headerPositions, STUDENT_LOGIN_HEADERS) == null
                && findColumn(headerPositions, STUDENT_FULL_NAME_HEADERS) == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "В файле должна быть колонка ID, логина или ФИО студента"
            );
        }
        if (findColumn(headerPositions, COURSE_WORK_TOPIC_HEADERS) == null
                && findColumn(headerPositions, THESIS_TOPIC_HEADERS) == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "В файле должна быть колонка темы курсовой или ВКР"
            );
        }
        if (findColumn(headerPositions, SUPERVISOR_ID_HEADERS) == null
                && findColumn(headerPositions, SUPERVISOR_LOGIN_HEADERS) == null
                && findColumn(headerPositions, SUPERVISOR_FULL_NAME_HEADERS) == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "В файле должна быть колонка ID, логина или ФИО научного руководителя"
            );
        }
        if (findColumn(headerPositions, SUPERVISOR_LOGIN_HEADERS) != null
                && findColumn(headerPositions, SUPERVISOR_ID_HEADERS) == null
                && findColumn(headerPositions, SUPERVISOR_FULL_NAME_HEADERS) == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "В файле отсутствует обязательная колонка: " + HEADER_SUPERVISOR_FULL_NAME
            );
        }
    }

    private static Integer findColumn(Map<String, Integer> headerPositions, List<String> headerNames) {
        for (String headerName : headerNames) {
            Integer position = headerPositions.get(normalizeHeaderKey(headerName));
            if (position != null) {
                return position;
            }
        }
        return null;
    }

    private static HeaderMapping findHeaderMapping(Sheet sheet, DataFormatter formatter) {
        int lastCandidateRow = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + 25);
        return Stream.iterate(sheet.getFirstRowNum(), rowIndex -> rowIndex + 1)
                .limit(lastCandidateRow - sheet.getFirstRowNum() + 1L)
                .map(rowIndex -> buildHeaderMapping(sheet.getRow(rowIndex), rowIndex, formatter))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingInt(HeaderMapping::score))
                .orElse(null);
    }

    private static Optional<HeaderMapping> buildHeaderMapping(Row headerRow, int rowIndex, DataFormatter formatter) {
        if (headerRow == null) {
            return Optional.empty();
        }

        Map<String, Integer> headerPositions = new LinkedHashMap<>();
        headerRow.forEach(cell -> headerPositions.put(
                normalizeHeaderKey(formatter.formatCellValue(cell)),
                cell.getColumnIndex()
        ));

        Integer studentFullNameColumn = findColumn(headerPositions, STUDENT_FULL_NAME_HEADERS);
        Integer courseWorkTopicColumn = findColumn(headerPositions, COURSE_WORK_TOPIC_HEADERS);
        Integer thesisTopicColumn = findColumn(headerPositions, THESIS_TOPIC_HEADERS);
        Integer supervisorFullNameColumn = findColumn(headerPositions, SUPERVISOR_FULL_NAME_HEADERS);
        Integer studentIdColumn = findColumn(headerPositions, STUDENT_ID_HEADERS);
        Integer studentLoginColumn = findColumn(headerPositions, STUDENT_LOGIN_HEADERS);
        Integer supervisorIdColumn = findColumn(headerPositions, SUPERVISOR_ID_HEADERS);
        Integer supervisorLoginColumn = findColumn(headerPositions, SUPERVISOR_LOGIN_HEADERS);

        HeaderMapping mapping = new HeaderMapping(
                rowIndex,
                studentIdColumn,
                studentLoginColumn,
                studentFullNameColumn,
                courseWorkTopicColumn,
                thesisTopicColumn,
                supervisorIdColumn,
                supervisorLoginColumn,
                supervisorFullNameColumn,
                studentFullNameColumn != null
                        && "студент".equals(normalizeHeaderKey(formatter.formatCellValue(
                        headerRow.getCell(studentFullNameColumn)
                )))
                        && (courseWorkTopicColumn == null || studentFullNameColumn + 1 < courseWorkTopicColumn),
                formatter
        );

        try {
            ensureUsableHeaders(headerPositions);
            return Optional.of(mapping);
        } catch (ResponseStatusException ignored) {
            return Optional.empty();
        }
    }

    private static char detectDelimiter(String content) {
        String firstNonBlankLine = Arrays.stream(content.split("\\R"))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");

        long semicolonCount = firstNonBlankLine.chars().filter(symbol -> symbol == ';').count();
        long commaCount = firstNonBlankLine.chars().filter(symbol -> symbol == ',').count();
        return semicolonCount > commaCount ? ';' : ',';
    }

    private static String validateRow(ParsedStudentTopicRow row, String studentLogin, Long studentId) {
        if (studentId == null && !StringUtils.hasText(studentLogin) && !StringUtils.hasText(row.studentFullName())) {
            return "Не заполнен ID, логин или ФИО студента";
        }
        if (!StringUtils.hasText(row.supervisorId())
                && !StringUtils.hasText(row.supervisorLogin())
                && !StringUtils.hasText(row.supervisorFullName())) {
            return "Не заполнен ID, логин или ФИО научного руководителя";
        }
        if (!StringUtils.hasText(row.courseWorkTopic()) && !StringUtils.hasText(row.thesisTopic())) {
            return "Не заполнены ни тема курсовой, ни тема ВКР";
        }
        return null;
    }

    private Student findStudent(ParsedStudentTopicRow row, String studentLogin, Long studentId) {
        if (studentId != null) {
            return studentRepository.findById(studentId).orElseThrow(
                    () -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Студент с ID '" + studentId + "' не найден"
                    )
            );
        }
        if (StringUtils.hasText(studentLogin)) {
            return studentRepository.findByUser_Login(studentLogin).orElseThrow(
                    () -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Студент с логином '" + studentLogin + "' не найден"
                    )
            );
        }

        String normalizedFullName = normalizePersonName(row.studentFullName());
        List<Student> matches = studentRepository.findAll().stream()
                .filter(student -> normalizePersonName(studentFullName(student))
                        .equals(normalizedFullName))
                .toList();
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Студент с ФИО '" + normalizeText(row.studentFullName()) + "' не найден"
            );
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Найдено несколько студентов с ФИО '" + normalizeText(row.studentFullName())
                        + "'. Добавьте в файл ID или логин студента"
        );
    }

    private Employee findSupervisor(ParsedStudentTopicRow row) {
        Long supervisorId = parseLong(row.supervisorId());
        if (supervisorId != null) {
            return employeeRepository.findById(supervisorId).orElseThrow(
                    () -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Научный руководитель с ID '" + supervisorId + "' не найден"
                    )
            );
        }

        String normalizedLogin = normalizeLogin(row.supervisorLogin());
        if (StringUtils.hasText(normalizedLogin)) {
            return employeeRepository.findByUserLogin(normalizedLogin).orElseThrow(
                    () -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Научный руководитель с логином '"
                                    + normalizedLogin
                                    + "' не найден"
                    )
            );
        }

        String normalizedFullName = normalizePersonName(row.supervisorFullName());
        List<Employee> matches = employeeRepository.findAll().stream()
                .filter(employee -> normalizePersonName(employeeFullName(employee))
                        .equals(normalizedFullName)
                        || normalizePersonName(toShortEmployeeName(employee)).equals(normalizedFullName))
                .toList();
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Научный руководитель с ФИО '" + normalizeText(row.supervisorFullName()) + "' не найден"
            );
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Найдено несколько научных руководителей с ФИО '" + normalizeText(row.supervisorFullName())
                        + "'. Добавьте в файл ID или логин научного руководителя"
        );
    }

    private URI buildGoogleSheetCsvExportUri(String url) {
        if (!StringUtils.hasText(url)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ссылка на Google Sheets не должна быть пустой"
            );
        }

        URI sourceUri = parseUri(url.trim());
        String host = Optional.ofNullable(sourceUri.getHost()).orElse("");
        if (!GOOGLE_SHEETS_HOST.equalsIgnoreCase(host)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Поддерживаются только ссылки на docs.google.com"
            );
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
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Не удалось получить данные из Google Sheets"
                );
            }
            return body;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось скачать Google Sheets по ссылке",
                    exception
            );
        }
    }

    private static URI parseUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректная ссылка на Google Sheets",
                    exception
            );
        }
    }

    private static String extractSpreadsheetId(String path) {
        String[] parts = path.split("/");
        for (int index = 0; index < parts.length - 1; index++) {
            if ("d".equals(parts[index]) && StringUtils.hasText(parts[index + 1])) {
                return parts[index + 1];
            }
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Не удалось определить идентификатор Google Sheets из ссылки"
        );
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

    private static String normalizeHeader(String value) {
        return Optional.ofNullable(value)
                .map(text -> text.replace("\uFEFF", ""))
                .map(String::trim)
                .map(text -> text.replaceAll("\\s+", " "))
                .orElse("");
    }

    private static String normalizeHeaderKey(String value) {
        return normalizeHeader(value).toLowerCase(Locale.ROOT);
    }

    private static String normalizeLogin(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .map(text -> text.toLowerCase(Locale.ROOT))
                .orElse("");
    }

    private static String normalizeText(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .map(text -> text.replaceAll("\\s+", " "))
                .orElse("");
    }

    private static String normalizeNullableText(String value) {
        String normalized = normalizeText(value);
        return normalized.isBlank() ? null : normalized;
    }

    private static String normalizePersonName(String value) {
        return normalizeText(value)
                .replace(".", " ")
                .replace(",", " ")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static Long parseLong(String value) {
        String normalized = normalizeText(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        try {
            return Long.parseLong(normalized.replaceAll("\\.0$", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String resolveStudentFullName(ParsedStudentTopicRow row, Student student) {
        if (StringUtils.hasText(row.studentFullName())) {
            return row.studentFullName();
        }
        return studentFullName(student);
    }

    private static String studentFullName(Student student) {
        return normalizeText(Stream.of(
                        student.getUser().getLastName(),
                        student.getUser().getFirstName(),
                        student.getPatronymic()
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" ")));
    }

    private static String resolveEmployeeFullName(ParsedStudentTopicRow row, Employee employee) {
        if (StringUtils.hasText(row.supervisorFullName())) {
            return row.supervisorFullName();
        }
        return employeeFullName(employee);
    }

    private static String employeeFullName(Employee employee) {
        return normalizeText(Stream.of(
                        employee.getUser().getLastName(),
                        employee.getUser().getFirstName(),
                        employee.getPatronymic()
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" ")));
    }

    private static String toShortEmployeeName(Employee employee) {
        String firstNameInitial = firstLetter(employee.getUser().getFirstName());
        String patronymicInitial = firstLetter(employee.getPatronymic());
        return normalizeText(Stream.of(
                        employee.getUser().getLastName(),
                        firstNameInitial,
                        patronymicInitial
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" ")));
    }

    private static String firstLetter(String value) {
        String normalized = normalizeText(value);
        return normalized.isEmpty() ? "" : normalized.substring(0, 1);
    }

    private static String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return Optional.ofNullable(current.getMessage()).orElse(current.getClass().getSimpleName());
    }

    private record ParsedStudentTopicRow(
            int rowNumber,
            String studentId,
            String studentLogin,
            String studentFullName,
            String courseWorkTopic,
            String thesisTopic,
            String supervisorId,
            String supervisorLogin,
            String supervisorFullName
    ) {
        private boolean isBlank() {
            return Stream.of(
                    studentId,
                    studentLogin,
                    studentFullName,
                    courseWorkTopic,
                    thesisTopic,
                    supervisorId,
                    supervisorLogin,
                    supervisorFullName
            ).allMatch(value -> value == null || value.isBlank());
        }

        private boolean hasStudentReference() {
            return Stream.of(studentId, studentLogin, studentFullName)
                    .anyMatch(StringUtils::hasText);
        }

        private boolean hasSupervisorReference() {
            return Stream.of(supervisorId, supervisorLogin, supervisorFullName)
                    .anyMatch(StringUtils::hasText);
        }
    }

    private record HeaderMapping(
            int headerRowIndex,
            Integer studentIdColumn,
            Integer studentLoginColumn,
            Integer studentFullNameColumn,
            Integer courseWorkTopicColumn,
            Integer thesisTopicColumn,
            Integer supervisorIdColumn,
            Integer supervisorLoginColumn,
            Integer supervisorFullNameColumn,
            boolean studentNameContinuesInNextColumn,
            DataFormatter formatter
    ) {
        private int score() {
            int filledColumns = Stream.of(
                            studentIdColumn,
                            studentLoginColumn,
                            studentFullNameColumn,
                            courseWorkTopicColumn,
                            thesisTopicColumn,
                            supervisorIdColumn,
                            supervisorLoginColumn,
                            supervisorFullNameColumn
                    )
                    .mapToInt(column -> column != null ? 1 : 0)
                    .sum();
            int preciseStudentColumn = studentNameContinuesInNextColumn ? 0 : 1;
            return filledColumns * 10 + preciseStudentColumn;
        }
    }
}
