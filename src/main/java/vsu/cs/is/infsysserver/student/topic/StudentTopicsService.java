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
    private static final String HEADER_COURSE_WORK_TOPIC = "Тема курсовой";
    private static final String HEADER_THESIS_TOPIC = "Тема ВКР";
    private static final String HEADER_SUPERVISOR_LOGIN = "Логин научного руководителя";
    private static final String HEADER_SUPERVISOR_FULL_NAME = "ФИО научного руководителя";
    private static final String GOOGLE_SHEETS_HOST = "docs.google.com";
    private static final List<String> REQUIRED_HEADERS = List.of(
            HEADER_STUDENT_LOGIN,
            HEADER_STUDENT_FULL_NAME,
            HEADER_COURSE_WORK_TOPIC,
            HEADER_THESIS_TOPIC,
            HEADER_SUPERVISOR_LOGIN,
            HEADER_SUPERVISOR_FULL_NAME
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

    private StudentTopicsImportResponse importRows(List<ParsedStudentTopicRow> rows) {
        Set<String> seenStudentLogins = new HashSet<>();
        List<StudentTopicsImportErrorResponse> errors = new ArrayList<>();
        int createdRows = 0;
        int updatedRows = 0;

        for (ParsedStudentTopicRow row : rows) {
            String studentLogin = normalizeLogin(row.studentLogin());
            String validationError = validateRow(row, studentLogin);
            if (validationError != null) {
                errors.add(new StudentTopicsImportErrorResponse(
                        row.rowNumber(),
                        studentLogin,
                        validationError
                ));
                continue;
            }
            if (!seenStudentLogins.add(studentLogin)) {
                errors.add(new StudentTopicsImportErrorResponse(
                        row.rowNumber(),
                        studentLogin,
                        "Дублирующийся логин студента в файле"
                ));
                continue;
            }

            try {
                Student student = findStudentByLogin(studentLogin);
                Employee supervisor = findSupervisorByLogin(row.supervisorLogin());

                Optional<StudentTopicAssignment> existingAssignment = findExistingAssignment(student, studentLogin);
                StudentTopicAssignment assignment = existingAssignment.orElseGet(StudentTopicAssignment::new);

                assignment.setStudent(student);
                assignment.setStudentLogin(normalizeLogin(student.getUser().getLogin()));
                assignment.setStudentFullName(normalizeText(row.studentFullName()));
                assignment.setCourseWorkTopic(normalizeNullableText(row.courseWorkTopic()));
                assignment.setThesisTopic(normalizeNullableText(row.thesisTopic()));
                assignment.setSupervisorLogin(normalizeLogin(supervisor.getUser().getLogin()));
                assignment.setSupervisorFullName(normalizeText(row.supervisorFullName()));
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
                rows.size(),
                createdRows,
                updatedRows,
                errors.size(),
                List.copyOf(errors)
        );
    }

    private Optional<StudentTopicAssignment> findExistingAssignment(Student student, String studentLogin) {
        return studentTopicAssignmentRepository.findByStudent_Id(student.getId())
                .or(() -> studentTopicAssignmentRepository.findByStudentLogin(studentLogin));
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
            ensureRequiredHeaders(headerPositions.keySet());

            List<ParsedStudentTopicRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                ParsedStudentTopicRow row = new ParsedStudentTopicRow(
                        (int) record.getRecordNumber() + 1,
                        csvValue(record, headerPositions, HEADER_STUDENT_LOGIN),
                        csvValue(record, headerPositions, HEADER_STUDENT_FULL_NAME),
                        csvValue(record, headerPositions, HEADER_COURSE_WORK_TOPIC),
                        csvValue(record, headerPositions, HEADER_THESIS_TOPIC),
                        csvValue(record, headerPositions, HEADER_SUPERVISOR_LOGIN),
                        csvValue(record, headerPositions, HEADER_SUPERVISOR_FULL_NAME)
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
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "В Excel-файле отсутствует строка заголовков"
                );
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerPositions = new LinkedHashMap<>();
            headerRow.forEach(cell -> headerPositions.put(
                    normalizeHeader(formatter.formatCellValue(cell)),
                    cell.getColumnIndex()
            ));
            ensureRequiredHeaders(headerPositions.keySet());

            List<ParsedStudentTopicRow> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                ParsedStudentTopicRow parsedRow = new ParsedStudentTopicRow(
                        rowIndex + 1,
                        xlsxValue(row, headerPositions, HEADER_STUDENT_LOGIN, formatter),
                        xlsxValue(row, headerPositions, HEADER_STUDENT_FULL_NAME, formatter),
                        xlsxValue(row, headerPositions, HEADER_COURSE_WORK_TOPIC, formatter),
                        xlsxValue(row, headerPositions, HEADER_THESIS_TOPIC, formatter),
                        xlsxValue(row, headerPositions, HEADER_SUPERVISOR_LOGIN, formatter),
                        xlsxValue(row, headerPositions, HEADER_SUPERVISOR_FULL_NAME, formatter)
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

    private static String csvValue(CSVRecord record, Map<String, Integer> headerPositions, String headerName) {
        Integer position = headerPositions.get(normalizeHeader(headerName));
        if (position == null || position >= record.size()) {
            return "";
        }
        return record.get(position);
    }

    private static String xlsxValue(Row row, Map<String, Integer> headerPositions, String headerName,
                                    DataFormatter formatter) {
        if (row == null) {
            return "";
        }
        Integer position = headerPositions.get(normalizeHeader(headerName));
        if (position == null || row.getCell(position) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(position));
    }

    private static Map<String, Integer> normalizeHeaderPositions(Map<String, Integer> rawHeaderPositions) {
        return rawHeaderPositions.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> normalizeHeader(entry.getKey()),
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private static void ensureRequiredHeaders(Set<String> availableHeaders) {
        List<String> missingHeaders = REQUIRED_HEADERS.stream()
                .filter(header -> !availableHeaders.contains(normalizeHeader(header)))
                .toList();

        if (!missingHeaders.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "В файле отсутствуют обязательные колонки: "
                            + String.join(", ", missingHeaders)
            );
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

    private static String validateRow(ParsedStudentTopicRow row, String studentLogin) {
        if (!StringUtils.hasText(studentLogin)) {
            return "Не заполнен логин студента";
        }
        if (!StringUtils.hasText(row.studentFullName())) {
            return "Не заполнено ФИО студента";
        }
        if (!StringUtils.hasText(row.supervisorLogin())) {
            return "Не заполнен логин научного руководителя";
        }
        if (!StringUtils.hasText(row.supervisorFullName())) {
            return "Не заполнено ФИО научного руководителя";
        }
        if (!StringUtils.hasText(row.courseWorkTopic()) && !StringUtils.hasText(row.thesisTopic())) {
            return "Не заполнены ни тема курсовой, ни тема ВКР";
        }
        return null;
    }

    private Student findStudentByLogin(String studentLogin) {
        return studentRepository.findByUser_Login(studentLogin).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Студент с логином '" + studentLogin + "' не найден"
                )
        );
    }

    private Employee findSupervisorByLogin(String supervisorLogin) {
        String normalizedLogin = normalizeLogin(supervisorLogin);
        return employeeRepository.findByUserLogin(normalizedLogin).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Научный руководитель с логином '"
                                + normalizedLogin
                                + "' не найден"
                )
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

    private static String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return Optional.ofNullable(current.getMessage()).orElse(current.getClass().getSimpleName());
    }

    private record ParsedStudentTopicRow(
            int rowNumber,
            String studentLogin,
            String studentFullName,
            String courseWorkTopic,
            String thesisTopic,
            String supervisorLogin,
            String supervisorFullName
    ) {
        private boolean isBlank() {
            return Stream.of(
                    studentLogin,
                    studentFullName,
                    courseWorkTopic,
                    thesisTopic,
                    supervisorLogin,
                    supervisorFullName
            ).allMatch(value -> value == null || value.isBlank());
        }
    }
}
