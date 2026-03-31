package ru.sandr.users.imports.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.hierarchy.service.DepartmentService;
import ru.sandr.users.hierarchy.service.StudentGroupService;
import ru.sandr.users.imports.dto.ImportResultResponse;
import ru.sandr.users.imports.dto.ImportRowError;
import ru.sandr.users.imports.dto.StudentImportRow;
import ru.sandr.users.imports.dto.TeacherImportRow;
import ru.sandr.users.imports.exception.ImportValidationException;
import ru.sandr.users.imports.parser.ExcelStreamingParser;
import ru.sandr.users.user.service.AdminUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserImportService {

    private static final int BATCH_SIZE = 5000;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final ExcelStreamingParser parser;
    private final StudentGroupService studentGroupService;
    private final DepartmentService departmentService;
    private final AdminUserService adminUserService;

    @Value("${app.import.max-errors:50}")
    private int maxImportErrors;

    /**
     * Type 3 — Students.
     * Columns: [0] Group name, [1] Student ID, [2] Last name, [3] First name,
     * [4] Middle name (opt), [5] Email, [6] Activity flag.
     * <p>
     * Group existence checks use batched IN queries — never loads all groups at once.
     */
    @Transactional
    public ImportResultResponse importStudents(MultipartFile file) {
        validateImportStudentsFile(file);

        int[] imported = {0};
        List<StudentImportPendingRow> batch = new ArrayList<>(BATCH_SIZE);

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        String groupName = cell(cells, 0);
                        String username = cell(cells, 1);
                        String lastName = cell(cells, 2);
                        String firstName = cell(cells, 3);
                        String middleName = cell(cells, 4);
                        String email = cell(cells, 5);
                        String activeFlag = cell(cells, 6);
                        String departmentName = cell(cells, 7);

                        batch.add(StudentImportPendingRow.builder()
                                                         .groupName(groupName)
                                                         .username(username)
                                                         .lastName(lastName)
                                                         .firstName(firstName)
                                                         .middleName(middleName)
                                                         .email(email)
                                                         .active(parseActiveFlag(activeFlag))
                                                         .departmentName(departmentName)
                                                         .build()
                        );

                        if (batch.size() >= BATCH_SIZE) {
                            imported[0] += flushStudentImportBatch(batch);
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!batch.isEmpty()) {
            imported[0] += flushStudentImportBatch(batch);
        }

        return new ImportResultResponse(imported[0]);
    }

    private void validateImportStudentsFile(MultipartFile file) {
        List<ImportRowError> errors = new ArrayList<>();
        List<PendingStudentRow> buffer = new ArrayList<>(BATCH_SIZE);

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        if (rowIndex == 0) {
                            return;
                        }
                        if (errors.size() >= maxImportErrors) {
                            return;
                        }
                        buffer.add(new PendingStudentRow(rowIndex, cells));
                        if (buffer.size() >= BATCH_SIZE) {
                            flushStudentValidationBuffer(buffer, errors);
                            buffer.clear();
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!buffer.isEmpty()) {
            flushStudentValidationBuffer(buffer, errors);
        }

        if (!errors.isEmpty()) {
            throw new ImportValidationException(errors);
        }
    }

    private void flushStudentValidationBuffer(List<PendingStudentRow> buffer, List<ImportRowError> errors) {
        if (buffer.isEmpty() || errors.size() >= maxImportErrors) {
            return;
        }

        Set<String> groupNames = new HashSet<>();
        Set<String> emails = new HashSet<>();
        Set<String> departmentNames = new HashSet<>();
        for (PendingStudentRow pr : buffer) {
            String[] cells = pr.cells();
            String groupCell = cell(cells, 0);
            if (!groupCell.isBlank()) {
                groupNames.add(groupCell);
            }
            String emailCell = cell(cells, 5);
            if (!emailCell.isBlank()) {
                emails.add(emailCell);
            }
            String departmentCell = cell(cells, 7);
            if (!departmentCell.isBlank()) {
                departmentNames.add(departmentCell);
            }
        }

        Map<String, Long> groupIdByGroupName = studentGroupService.findGroupIdsByNamesIn(groupNames);
        Map<String, String> usernameByEmail = adminUserService.findUsernameByEmailIn(emails);
        Map<String, Long> departmentIdByDepartmentName = departmentService.findDepartmentIdsByNamesIs(departmentNames);

        for (PendingStudentRow row : buffer) {
            if (errors.size() >= maxImportErrors) {
                break;
            }
            String[] cells = row.cells();
            int displayRow = row.rowIndex() + 1;

            String groupName = cell(cells, 0);
            String username = cell(cells, 1);
            String lastName = cell(cells, 2);
            String firstName = cell(cells, 3);
            String email = cell(cells, 5);
            String activeFlag = cell(cells, 6);
            String departmentName = cell(cells, 7);

            if (groupName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "Group", "Required field is blank"));
            } else if (!groupIdByGroupName.containsKey(groupName)) {
                errors.add(new ImportRowError(displayRow, "Group", "Group not found: " + groupName));
            }
            if (!departmentName.isBlank() && !departmentIdByDepartmentName.containsKey(departmentName)) {
                errors.add(new ImportRowError(displayRow, "Department", "Department not found: " + departmentName));
            }
            if (username.isBlank()) {
                errors.add(new ImportRowError(displayRow, "Student ID", "Required field is blank"));
            }
            if (lastName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "Last name", "Required field is blank"));
            }
            if (firstName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "First name", "Required field is blank"));
            }
            if (email.isBlank()) {
                errors.add(new ImportRowError(displayRow, "Email", "Required field is blank"));
            }
//                        else if (!EMAIL_PATTERN.matcher(email).matches()) {
//                errors.add(new ImportRowError(displayRow, "Email", "Invalid email format"));
//            }

            if (!email.isBlank()) {
                String ownerInDb = usernameByEmail.get(email);
                if (ownerInDb != null && !username.isBlank() && !ownerInDb.equals(username)) {
                    errors.add(new ImportRowError(
                            displayRow,
                            "Email",
                            "Email already in use by another user: " + email
                    ));
                }
            }

            if (!isValidActiveFlag(activeFlag)) {
                errors.add(new ImportRowError(
                        displayRow, "Activity flag",
                        "Invalid value; expected: 1/0/true/false/yes/no"
                ));
            }
        }
    }

    private int flushStudentImportBatch(List<StudentImportPendingRow> batch) {
        Set<String> groupNames = batch.stream()
                                      .map(StudentImportPendingRow::groupName)
                                      .filter(StringUtils::isNotBlank)
                                      .collect(Collectors.toSet());
        Set<String> departNames = batch.stream()
                                       .map(StudentImportPendingRow::departmentName)
                                       .filter(StringUtils::isNotBlank)
                                       .collect(Collectors.toSet());
        Map<String, Long> groupIdByGroupName = studentGroupService.findGroupIdsByNamesIn(groupNames);
        Map<String, Long> departmentIdByDepartmentName = departmentService.findDepartmentIdsByNamesIs(departNames);

        List<StudentImportRow> rows = new ArrayList<>(batch.size());
        for (StudentImportPendingRow p : batch) {
            Long groupId = groupIdByGroupName.get(p.groupName());
            Long departmentId = departmentIdByDepartmentName.get(p.departmentName());
            rows.add(new StudentImportRow(
                    p.username(),
                    p.email(),
                    p.firstName(),
                    p.lastName(),
                    p.middleName(),
                    p.active(),
                    groupId,
                    departmentId
            ));
        }
        int n = adminUserService.bulkUpsertStudents(rows);
        batch.clear();
        return n;
    }

    private record PendingStudentRow(int rowIndex, String[] cells) {
    }

    @Builder
    private record StudentImportPendingRow(
            String username,
            String email,
            String firstName,
            String lastName,
            String middleName,
            boolean active,
            String groupName,
            String departmentName
    ) {
    }

    /**
     * Type 4 — Teachers.
     * Columns: [0] Teacher ID, [1] Last name, [2] First name,
     * [3] Middle name (opt), [4] Email, [5] Department name, [6] Activity flag.
     */
    @Transactional
    public ImportResultResponse importTeachers(MultipartFile file) {
        Map<String, Long> deptNameToId = departmentService.findAllAsNameMap();

        // ── Pass 1 ────────────────────────────────────────────────────────────────
        List<ImportRowError> errors = new ArrayList<>();

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        String username = cell(cells, 0);
                        String lastName = cell(cells, 1);
                        String firstName = cell(cells, 2);
                        String email = cell(cells, 4);
                        String deptName = cell(cells, 5);
                        String activeFlag = cell(cells, 6);
                        int displayRow = rowIndex + 1;

                        if (errors.size() < maxImportErrors) {
                            if (username.isBlank())
                                errors.add(new ImportRowError(displayRow, "Teacher ID", "Required field is blank"));
                            if (lastName.isBlank())
                                errors.add(new ImportRowError(displayRow, "Last name", "Required field is blank"));
                            if (firstName.isBlank())
                                errors.add(new ImportRowError(displayRow, "First name", "Required field is blank"));
                            if (!email.isBlank() && !EMAIL_PATTERN.matcher(email).matches())
                                errors.add(new ImportRowError(displayRow, "Email", "Invalid email format"));
                            if (deptName.isBlank())
                                errors.add(new ImportRowError(displayRow, "Department", "Required field is blank"));
                            else if (!deptNameToId.containsKey(deptName))
                                errors.add(new ImportRowError(
                                        displayRow,
                                        "Department",
                                        "Department not found: " + deptName
                                ));
                            if (!isValidActiveFlag(activeFlag))
                                errors.add(new ImportRowError(
                                        displayRow, "Activity flag",
                                        "Invalid value; expected: 1/0/true/false/yes/no"
                                ));
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!errors.isEmpty()) throw new ImportValidationException(errors);

        // ── Pass 2 ────────────────────────────────────────────────────────────────
        int[] imported = {0};
        List<TeacherImportRow> batch = new ArrayList<>(BATCH_SIZE);

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        String username = cell(cells, 0);
                        String email = cell(cells, 4);
                        String deptName = cell(cells, 5);

                        batch.add(new TeacherImportRow(
                                username,
                                email.isBlank() ? null : email,
                                cell(cells, 2),              // firstName
                                cell(cells, 1),              // lastName
                                blankToNull(cell(cells, 3)), // middleName
                                parseActiveFlag(cell(cells, 6)),
                                deptNameToId.get(deptName)
                        ));

                        if (batch.size() >= BATCH_SIZE) {
                            imported[0] += flushTeacherBatch(batch);
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!batch.isEmpty()) {
            imported[0] += flushTeacherBatch(batch);
        }

        return new ImportResultResponse(imported[0]);
    }

    // ── Batch flush helpers ───────────────────────────────────────────────────

    private int flushTeacherBatch(List<TeacherImportRow> batch) {
        int n = adminUserService.bulkUpsertTeachers(new ArrayList<>(batch));
        batch.clear();
        return n;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static String cell(String[] cells, int index) {
        return (cells.length > index && cells[index] != null) ? cells[index] : "";
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static boolean isValidActiveFlag(String value) {
        return "1".equals(value) || "0".equals(value)
                || "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)
                || "yes".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)
                || "да".equalsIgnoreCase(value) || "нет".equalsIgnoreCase(value);
    }

    private static boolean parseActiveFlag(String value) {
        return "1".equals(value)
                || "true".equalsIgnoreCase(value)
                || "yes".equalsIgnoreCase(value)
                || "да".equalsIgnoreCase(value);
    }
}
