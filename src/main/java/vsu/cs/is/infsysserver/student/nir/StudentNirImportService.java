package vsu.cs.is.infsysserver.student.nir;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vsu.cs.is.infsysserver.employee.adapter.jpa.EmployeeRepository;
import vsu.cs.is.infsysserver.employee.adapter.jpa.entity.Employee;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.nir.adapter.jpa.StudentNirRecordRepository;
import vsu.cs.is.infsysserver.student.nir.adapter.jpa.entity.StudentNirRecord;
import vsu.cs.is.infsysserver.student.nir.adapter.rest.dto.response.StudentNirImportErrorResponse;
import vsu.cs.is.infsysserver.student.nir.adapter.rest.dto.response.StudentNirImportResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StudentNirImportService {

    private static final List<String> STUDENT_FULL_NAME_HEADERS = List.of(
            "фио студента", "студент", "фио обучающегося"
    );
    private static final List<String> SUPERVISOR_FULL_NAME_HEADERS = List.of(
            "фио научного руководителя", "научный руководитель", "руководитель"
    );
    private static final List<String> THESIS_TOPIC_HEADERS = List.of(
            "тема вкр", "тема выпускной квалификационной работы", "тема дипломной работы"
    );
    private static final List<String> NIR_SEM7_HEADERS = List.of(
            "практика нир, 7 семестр", "практика нир 7 семестр", "нир 7 семестр", "нир, 7 семестр"
    );
    private static final List<String> NIR_SEM8_HEADERS = List.of(
            "практика нир, 8 семестр", "практика нир 8 семестр", "нир 8 семестр", "нир, 8 семестр"
    );
    private static final List<String> PREDEFENSE1_HEADERS = List.of(
            "предзащита 1 (апрель)", "предзащита 1", "первая предзащита"
    );
    private static final List<String> PREDEFENSE2_HEADERS = List.of(
            "предзащита 2 (май)", "предзащита 2", "вторая предзащита"
    );
    private static final List<String> DOCS_HEADERS = List.of(
            "наличие документов", "документы"
    );

    private static final Set<String> POSITION_TOKENS = Set.of(
            "доц", "доцент", "проф", "профессор", "асс", "ассистент",
            "ст", "преп", "преподаватель", "зав", "кафедрой"
    );
    private static final Pattern GROUP_SUMMARY_PATTERN = Pattern.compile("^\\d+\\s*групп.*", Pattern.CASE_INSENSITIVE);
    private static final Set<String> ROLLUP_TOKENS = Set.of("всего", "итого");
    private static final Pattern DEBTOR_SHEET_PATTERN = Pattern.compile("задолж", Pattern.CASE_INSENSITIVE);

    private final StudentNirRecordRepository nirRecordRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;

    public StudentNirImportResponse importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не должен быть пустым");
        }
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .orElse("");
        if (!filename.endsWith(".xlsx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поддерживаются только файлы .xlsx");
        }

        List<ParsedNirRow> rows = parseXlsx(file);
        return importRows(rows);
    }

    private List<ParsedNirRow> parseXlsx(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "В Excel-файле нет листов");
            }
            DataFormatter formatter = new DataFormatter();
            List<ParsedNirRow> rows = new ArrayList<>();
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                rows.addAll(parseSheet(sheet, formatter));
            }
            if (rows.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "В Excel-файле не найдены строки с данными студентов"
                );
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

    private List<ParsedNirRow> parseSheet(Sheet sheet, DataFormatter formatter) {
        NirHeaderMapping mapping = findHeaderMapping(sheet, formatter);
        if (mapping == null) {
            return List.of();
        }
        boolean debtor = DEBTOR_SHEET_PATTERN.matcher(sheet.getSheetName()).find();
        List<ParsedNirRow> rows = new ArrayList<>();
        for (int rowIndex = mapping.headerRowIndex() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            String studentFullName = cellValue(row, mapping.studentFullNameColumn(), formatter);
            if (!StringUtils.hasText(studentFullName)) continue;
            if (isRollupRow(studentFullName)) continue;
            rows.add(new ParsedNirRow(
                    rowIndex + 1,
                    sheet.getSheetName(),
                    debtor,
                    studentFullName,
                    cellValue(row, mapping.supervisorFullNameColumn(), formatter),
                    cellValue(row, mapping.thesisTopicColumn(), formatter),
                    cellValue(row, mapping.nir7GradeColumn(), formatter),
                    cellValue(row, mapping.nir7DocsColumn(), formatter),
                    cellValue(row, mapping.predefense1Column(), formatter),
                    cellValue(row, mapping.predefense2Column(), formatter),
                    cellValue(row, mapping.nir8GradeColumn(), formatter),
                    cellValue(row, mapping.nir8DocsColumn(), formatter)
            ));
        }
        return rows;
    }

    private static NirHeaderMapping findHeaderMapping(Sheet sheet, DataFormatter formatter) {
        int lastCandidate = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + 10);
        NirHeaderMapping best = null;
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= lastCandidate; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            NirHeaderMapping candidate = buildHeaderMapping(row, rowIndex, formatter);
            if (candidate == null) continue;
            if (best == null || candidate.score() > best.score()) {
                best = candidate;
            }
        }
        return best;
    }

    private static NirHeaderMapping buildHeaderMapping(Row headerRow, int rowIndex, DataFormatter formatter) {
        Map<Integer, String> columnTexts = new LinkedHashMap<>();
        headerRow.forEach(cell -> columnTexts.put(
                cell.getColumnIndex(),
                normalizeHeaderKey(formatter.formatCellValue(cell))
        ));

        Integer studentFullNameColumn = findColumn(columnTexts, STUDENT_FULL_NAME_HEADERS);
        Integer supervisorFullNameColumn = findColumn(columnTexts, SUPERVISOR_FULL_NAME_HEADERS);
        Integer thesisTopicColumn = findColumn(columnTexts, THESIS_TOPIC_HEADERS);
        Integer nir7GradeColumn = findColumn(columnTexts, NIR_SEM7_HEADERS);
        Integer nir8GradeColumn = findColumn(columnTexts, NIR_SEM8_HEADERS);
        Integer predefense1Column = findColumn(columnTexts, PREDEFENSE1_HEADERS);
        Integer predefense2Column = findColumn(columnTexts, PREDEFENSE2_HEADERS);

        if (studentFullNameColumn == null) {
            return null;
        }

        Integer nir7DocsColumn = findFollowingDocsColumn(columnTexts, nir7GradeColumn);
        Integer nir8DocsColumn = findFollowingDocsColumn(columnTexts, nir8GradeColumn);

        return new NirHeaderMapping(
                rowIndex,
                studentFullNameColumn,
                supervisorFullNameColumn,
                thesisTopicColumn,
                nir7GradeColumn,
                nir7DocsColumn,
                predefense1Column,
                predefense2Column,
                nir8GradeColumn,
                nir8DocsColumn
        );
    }

    private static Integer findFollowingDocsColumn(Map<Integer, String> columnTexts, Integer anchor) {
        if (anchor == null) return null;
        return columnTexts.entrySet().stream()
                .filter(entry -> entry.getKey() > anchor)
                .filter(entry -> DOCS_HEADERS.stream().anyMatch(h -> h.equals(entry.getValue())))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static Integer findColumn(Map<Integer, String> columnTexts, List<String> headerVariants) {
        Set<String> variants = headerVariants.stream()
                .map(StudentNirImportService::normalizeHeaderKey)
                .collect(Collectors.toSet());
        return columnTexts.entrySet().stream()
                .filter(entry -> variants.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private StudentNirImportResponse importRows(List<ParsedNirRow> rows) {
        Set<Long> seenStudentIds = new HashSet<>();
        List<StudentNirImportErrorResponse> errors = new ArrayList<>();
        int processed = 0;
        int created = 0;
        int updated = 0;

        List<Student> allStudents = studentRepository.findAll();
        List<Employee> allEmployees = employeeRepository.findAll();

        for (ParsedNirRow row : rows) {
            processed++;
            try {
                Student student = findStudent(row, allStudents);
                if (!seenStudentIds.add(student.getId())) {
                    errors.add(new StudentNirImportErrorResponse(
                            row.rowNumber(),
                            row.sheetName(),
                            row.studentFullName(),
                            "Дублирующийся студент в файле (ID " + student.getId() + ")"
                    ));
                    continue;
                }

                Employee supervisor = findSupervisor(row, allEmployees).orElse(null);

                Optional<StudentNirRecord> existing = nirRecordRepository.findByStudent_Id(student.getId());
                StudentNirRecord record = existing.orElseGet(StudentNirRecord::new);

                record.setStudent(student);
                record.setStudentLogin(normalizeLogin(student.getUser().getLogin()));
                record.setStudentFullName(buildStudentFullName(student, row.studentFullName()));
                record.setSupervisorEmployee(supervisor);
                record.setSupervisorFullName(normalizeNullableText(row.supervisorFullName()));
                record.setThesisTopic(normalizeNullableText(row.thesisTopic()));
                record.setNir7Grade(normalizeNullableText(row.nir7Grade()));
                record.setNir7Docs(normalizeNullableText(row.nir7Docs()));
                record.setPredefense1Grade(normalizeNullableText(row.predefense1Grade()));
                record.setPredefense2Grade(normalizeNullableText(row.predefense2Grade()));
                record.setNir8Grade(normalizeNullableText(row.nir8Grade()));
                record.setNir8Docs(normalizeNullableText(row.nir8Docs()));
                record.setDebtor(row.debtor());
                record.setImportedAt(LocalDateTime.now());

                nirRecordRepository.save(record);
                if (existing.isPresent()) {
                    updated++;
                } else {
                    created++;
                }
            } catch (ResponseStatusException exception) {
                errors.add(new StudentNirImportErrorResponse(
                        row.rowNumber(),
                        row.sheetName(),
                        row.studentFullName(),
                        Optional.ofNullable(exception.getReason()).orElse(exception.getMessage())
                ));
            }
        }

        return new StudentNirImportResponse(processed, created, updated, errors.size(), List.copyOf(errors));
    }

    private static Student findStudent(ParsedNirRow row, List<Student> allStudents) {
        String normalized = normalizePersonName(row.studentFullName());
        List<Student> matches = allStudents.stream()
                .filter(student -> normalizePersonName(fullName(student)).equals(normalized))
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
                "Найдено несколько студентов с ФИО '" + normalizeText(row.studentFullName()) + "'"
        );
    }

    private static Optional<Employee> findSupervisor(ParsedNirRow row, List<Employee> allEmployees) {
        if (!StringUtils.hasText(row.supervisorFullName())) {
            return Optional.empty();
        }
        EmployeeNameSignature signature = parseEmployeeNameSignature(row.supervisorFullName());
        if (signature == null) {
            return Optional.empty();
        }
        return allEmployees.stream()
                .filter(employee -> matchesEmployee(employee, signature))
                .findFirst();
    }

    private static boolean matchesEmployee(Employee employee, EmployeeNameSignature signature) {
        String surname = normalizeToken(employee.getUser().getLastName());
        if (surname.isEmpty() || !surname.equals(signature.surname())) {
            return false;
        }
        String firstNameInitial = firstLetter(employee.getUser().getFirstName());
        String patronymicInitial = firstLetter(employee.getPatronymic());
        if (signature.firstInitial() != null && !signature.firstInitial().equals(firstNameInitial)) {
            return false;
        }
        if (signature.patronymicInitial() != null
                && !patronymicInitial.isEmpty()
                && !signature.patronymicInitial().equals(patronymicInitial)) {
            return false;
        }
        return true;
    }

    private static EmployeeNameSignature parseEmployeeNameSignature(String raw) {
        String stripped = normalizeText(raw)
                .replace(".", " ")
                .replace(",", " ");
        List<String> tokens = Stream.of(stripped.toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(StringUtils::hasText)
                .filter(token -> !POSITION_TOKENS.contains(token))
                .toList();
        if (tokens.isEmpty()) {
            return null;
        }
        String surname = tokens.stream()
                .filter(token -> token.length() > 2)
                .reduce((first, second) -> second)
                .orElse(tokens.get(tokens.size() - 1));
        List<String> initials = tokens.stream()
                .filter(token -> !token.equals(surname))
                .filter(token -> token.length() <= 2)
                .toList();
        String firstInitial = initials.isEmpty() ? null : initials.get(0).substring(0, 1);
        String patronymicInitial = initials.size() < 2 ? null : initials.get(1).substring(0, 1);
        return new EmployeeNameSignature(surname, firstInitial, patronymicInitial);
    }

    private static boolean isRollupRow(String studentFullName) {
        String normalized = normalizeText(studentFullName).toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return true;
        if (ROLLUP_TOKENS.contains(normalized)) return true;
        return GROUP_SUMMARY_PATTERN.matcher(normalized).matches();
    }

    private static String cellValue(Row row, Integer column, DataFormatter formatter) {
        if (row == null || column == null) return "";
        var cell = row.getCell(column);
        return cell == null ? "" : formatter.formatCellValue(cell);
    }

    private static String fullName(Student student) {
        return normalizeText(Stream.of(
                        student.getUser().getLastName(),
                        student.getUser().getFirstName(),
                        student.getPatronymic()
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" ")));
    }

    private static String buildStudentFullName(Student student, String fromFile) {
        String fromFileNormalized = normalizeText(fromFile);
        if (!fromFileNormalized.isEmpty()) {
            return fromFileNormalized;
        }
        return fullName(student);
    }

    private static String normalizeHeaderKey(String value) {
        return Optional.ofNullable(value)
                .map(text -> text.replace("﻿", ""))
                .map(String::trim)
                .map(text -> text.replaceAll("\\s+", " "))
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

    private static String normalizeLogin(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .map(text -> text.toLowerCase(Locale.ROOT))
                .orElse("");
    }

    private static String normalizePersonName(String value) {
        return normalizeText(value)
                .replace(".", " ")
                .replace(",", " ")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String normalizeToken(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .map(text -> text.toLowerCase(Locale.ROOT))
                .orElse("");
    }

    private static String firstLetter(String value) {
        String normalized = normalizeToken(value);
        return normalized.isEmpty() ? "" : normalized.substring(0, 1);
    }

    private record ParsedNirRow(
            int rowNumber,
            String sheetName,
            boolean debtor,
            String studentFullName,
            String supervisorFullName,
            String thesisTopic,
            String nir7Grade,
            String nir7Docs,
            String predefense1Grade,
            String predefense2Grade,
            String nir8Grade,
            String nir8Docs
    ) {
    }

    private record NirHeaderMapping(
            int headerRowIndex,
            Integer studentFullNameColumn,
            Integer supervisorFullNameColumn,
            Integer thesisTopicColumn,
            Integer nir7GradeColumn,
            Integer nir7DocsColumn,
            Integer predefense1Column,
            Integer predefense2Column,
            Integer nir8GradeColumn,
            Integer nir8DocsColumn
    ) {
        private int score() {
            return Stream.of(
                            studentFullNameColumn,
                            supervisorFullNameColumn,
                            thesisTopicColumn,
                            nir7GradeColumn,
                            nir7DocsColumn,
                            predefense1Column,
                            predefense2Column,
                            nir8GradeColumn,
                            nir8DocsColumn
                    )
                    .mapToInt(column -> column != null ? 1 : 0)
                    .sum();
        }
    }

    private record EmployeeNameSignature(String surname, String firstInitial, String patronymicInitial) {
    }
}
