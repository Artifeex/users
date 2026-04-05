package ru.sandr.users.imports.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;
import ru.sandr.users.hierarchy.service.DepartmentService;
import ru.sandr.users.hierarchy.service.FieldOfStudyService;
import ru.sandr.users.hierarchy.service.FacultyService;
import ru.sandr.users.hierarchy.service.StudentGroupService;
import ru.sandr.users.imports.dto.ImportResultResponse;
import ru.sandr.users.imports.dto.ImportRowError;
import ru.sandr.users.imports.exception.ImportValidationException;
import ru.sandr.users.imports.parser.ExcelStreamingParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HierarchyImportService {

    private static final int BATCH_SIZE = 5000;

    private final ExcelStreamingParser parser;
    private final FacultyService facultyService;
    private final DepartmentService departmentService;
    private final FieldOfStudyService fieldOfStudyService;
    private final StudentGroupService studentGroupService;

    @Value("${app.import.max-errors:50}")
    private int maxImportErrors;

    /**
     * Columns:
     * [0] Faculty name (required)
     * [1] Field of study name (required)
     * [2] Student group name (required)
     */
    @Transactional
    public ImportResultResponse importStructure(MultipartFile file) {
        validateImportStructureFile(file);

        int[] imported = {0};
        List<StructureImportPendingRow> batch = new ArrayList<>(BATCH_SIZE);

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        batch.add(StructureImportPendingRow.builder()
                                                           .facultyName(cell(cells, 0))
                                                           .fieldOfStudyName(cell(cells, 1))
                                                           .groupName(cell(cells, 2))
                                                           .build());

                        if (batch.size() >= BATCH_SIZE) {
                            imported[0] += flushStructureImportBatch(batch);
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!batch.isEmpty()) {
            imported[0] += flushStructureImportBatch(batch);
        }

        return new ImportResultResponse(imported[0]);
    }

    /**
     * Columns:
     * [0] Faculty name (required)
     * [1] Department name (required)
     */
    @Transactional
    public ImportResultResponse importDepartments(MultipartFile file) {
        validateImportDepartmentsFile(file);

        int[] imported = {0};
        List<DepartmentsImportPendingRow> batch = new ArrayList<>(BATCH_SIZE);

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        batch.add(DepartmentsImportPendingRow.builder()
                                                             .facultyName(cell(cells, 0))
                                                             .departmentName(cell(cells, 1))
                                                             .build());

                        if (batch.size() >= BATCH_SIZE) {
                            imported[0] += flushDepartmentsImportBatch(batch);
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!batch.isEmpty()) {
            imported[0] += flushDepartmentsImportBatch(batch);
        }

        return new ImportResultResponse(imported[0]);
    }

    private void validateImportStructureFile(MultipartFile file) {
        List<ImportRowError> errors = new ArrayList<>();
        List<PendingStructureRow> buffer = new ArrayList<>(BATCH_SIZE);

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        if (errors.size() >= maxImportErrors) {
                            return;
                        }
                        buffer.add(new PendingStructureRow(rowIndex, cells));
                        if (buffer.size() >= BATCH_SIZE) {
                            flushStructureValidationBuffer(buffer, errors);
                            buffer.clear();
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!buffer.isEmpty()) {
            flushStructureValidationBuffer(buffer, errors);
        }

        if (!errors.isEmpty()) {
            throw new ImportValidationException(errors);
        }
    }

    private void validateImportDepartmentsFile(MultipartFile file) {
        List<ImportRowError> errors = new ArrayList<>();
        List<PendingDepartmentsRow> buffer = new ArrayList<>(BATCH_SIZE);

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        if (errors.size() >= maxImportErrors) {
                            return;
                        }
                        buffer.add(new PendingDepartmentsRow(rowIndex, cells));
                        if (buffer.size() >= BATCH_SIZE) {
                            flushDepartmentsValidationBuffer(buffer, errors);
                            buffer.clear();
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!buffer.isEmpty()) {
            flushDepartmentsValidationBuffer(buffer, errors);
        }

        if (!errors.isEmpty()) {
            throw new ImportValidationException(errors);
        }
    }

    private void flushStructureValidationBuffer(List<PendingStructureRow> buffer, List<ImportRowError> errors) {
        if (buffer.isEmpty() || errors.size() >= maxImportErrors) {
            return;
        }

        for (PendingStructureRow row : buffer) {
            if (errors.size() >= maxImportErrors) {
                return;
            }

            String[] cells = row.cells();
            int displayRow = row.rowIndex() + 1;

            String facultyName = cell(cells, 0);
            String fieldOfStudyName = cell(cells, 1);
            String groupName = cell(cells, 2);

            if (facultyName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "FacultyName", "Required field is blank"));
            } else if (facultyName.length() > 255) {
                errors.add(new ImportRowError(displayRow, "FacultyName", "Max length is 255"));
            }

            if (fieldOfStudyName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "FieldOfStudyName", "Required field is blank"));
            } else if (fieldOfStudyName.length() > 255) {
                errors.add(new ImportRowError(displayRow, "FieldOfStudyName", "Max length is 255"));
            }

            if (groupName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "GroupName", "Required field is blank"));
            } else if (groupName.length() > 50) {
                errors.add(new ImportRowError(displayRow, "GroupName", "Max length is 50"));
            }
        }
    }

    private void flushDepartmentsValidationBuffer(List<PendingDepartmentsRow> buffer, List<ImportRowError> errors) {
        if (buffer.isEmpty() || errors.size() >= maxImportErrors) {
            return;
        }

        for (PendingDepartmentsRow row : buffer) {
            if (errors.size() >= maxImportErrors) {
                return;
            }

            String[] cells = row.cells();
            int displayRow = row.rowIndex() + 1;

            String facultyName = cell(cells, 0);
            String departmentName = cell(cells, 1);

            if (facultyName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "FacultyName", "Required field is blank"));
            } else if (facultyName.length() > 255) {
                errors.add(new ImportRowError(displayRow, "FacultyName", "Max length is 255"));
            }

            if (departmentName.isBlank()) {
                errors.add(new ImportRowError(displayRow, "DepartmentName", "Required field is blank"));
            } else if (departmentName.length() > 255) {
                errors.add(new ImportRowError(displayRow, "DepartmentName", "Max length is 255"));
            }
        }
    }

    private int flushStructureImportBatch(List<StructureImportPendingRow> batch) {
        if (batch.isEmpty()) {
            return 0;
        }

        Map<String, Faculty> facultyByName = facultyService.bulkUpsertByNames(
                batch.stream()
                     .map(StructureImportPendingRow::facultyName)
                     .filter(StringUtils::isNotBlank)
                     .collect(Collectors.toSet())
        );

        List<FieldOfStudyService.FieldOfStudyUpsertRow> fieldsToUpsert = batch.stream()
                                                                               .map(row -> new FieldOfStudyService.FieldOfStudyUpsertRow(
                                                                                       row.fieldOfStudyName(),
                                                                                       facultyByName.get(row.facultyName())
                                                                               ))
                                                                               .toList();
        Map<String, FieldOfStudy> fieldByName = fieldOfStudyService.bulkUpsertByNames(fieldsToUpsert);

        List<StudentGroupService.StudentGroupUpsertRow> groupsToUpsert = batch.stream()
                                                                               .map(row -> new StudentGroupService.StudentGroupUpsertRow(
                                                                                       row.groupName(),
                                                                                       facultyByName.get(row.facultyName()),
                                                                                       fieldByName.get(row.fieldOfStudyName())
                                                                               ))
                                                                               .toList();
        studentGroupService.bulkUpsertByNames(groupsToUpsert);

        int imported = batch.size();
        batch.clear();
        return imported;
    }

    private int flushDepartmentsImportBatch(List<DepartmentsImportPendingRow> batch) {
        if (batch.isEmpty()) {
            return 0;
        }

        Map<String, Faculty> facultyByName = facultyService.bulkUpsertByNames(
                batch.stream()
                     .map(DepartmentsImportPendingRow::facultyName)
                     .filter(StringUtils::isNotBlank)
                     .collect(Collectors.toSet())
        );

        List<DepartmentService.DepartmentUpsertRow> departmentsToUpsert = batch.stream()
                                                                                .map(row -> new DepartmentService.DepartmentUpsertRow(
                                                                                        row.departmentName(),
                                                                                        facultyByName.get(row.facultyName())
                                                                                ))
                                                                                .toList();
        departmentService.bulkUpsertByNames(departmentsToUpsert);

        int imported = batch.size();
        batch.clear();
        return imported;
    }

    private record PendingStructureRow(int rowIndex, String[] cells) {
    }

    private record PendingDepartmentsRow(int rowIndex, String[] cells) {
    }

    @Builder
    private record StructureImportPendingRow(
            String facultyName,
            String fieldOfStudyName,
            String groupName
    ) {
    }

    @Builder
    private record DepartmentsImportPendingRow(
            String facultyName,
            String departmentName
    ) {
    }

    private static String cell(String[] cells, int index) {
        return (cells.length > index && cells[index] != null) ? cells[index] : "";
    }
}
