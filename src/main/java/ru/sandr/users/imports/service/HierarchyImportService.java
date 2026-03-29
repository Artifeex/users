package ru.sandr.users.imports.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.hierarchy.entity.Department;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;
import ru.sandr.users.hierarchy.entity.StudentGroup;
import ru.sandr.users.hierarchy.service.DepartmentService;
import ru.sandr.users.hierarchy.service.FieldOfStudyService;
import ru.sandr.users.hierarchy.service.FacultyService;
import ru.sandr.users.hierarchy.service.StudentGroupService;
import ru.sandr.users.imports.dto.ImportResultResponse;
import ru.sandr.users.imports.dto.ImportRowError;
import ru.sandr.users.imports.exception.ImportValidationException;
import ru.sandr.users.imports.parser.ExcelStreamingParser;
import ru.sandr.users.imports.parser.ParsedRow;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyImportService {

    private static final int MAX_ERRORS = 50;

    /** Matches {@code spring.jpa.properties.hibernate.jdbc.batch_size} default intent. */
    private static final int STRUCTURE_IMPORT_BATCH_SIZE = 100;

    private final FacultyService facultyService;
    private final FieldOfStudyService fieldOfStudyService;
    private final DepartmentService departmentService;
    private final StudentGroupService studentGroupService;
    private final ExcelStreamingParser parser;

    /**
     * Type 1 — Faculty + FieldOfStudy + StudentGroup.
     * Expected columns: [0] Institute/Faculty name, [1] FieldOfStudy name, [2] Group name.
     * <p>
     * State maps live in {@link StructureImportState}; each batch prefetches missing faculty and
     * field-of-study rows from the DB by name under the resolved faculty id. Entity proxies from
     * getReference() are used for FK assignment.
     */
    @Transactional
    public ImportResultResponse importStructure(MultipartFile file) {
        validateImportStructureFile(file);

        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        int[] imported = {0};
        StructureImportState state = new StructureImportState();

        try {
            parser.parseBatch(
                    file.getInputStream(),
                    STRUCTURE_IMPORT_BATCH_SIZE,
                    batch -> processStructureBatch(batch, state, now, actor, imported)
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        return new ImportResultResponse(imported[0]);
    }

    private void processStructureBatch(
            List<ParsedRow> batch,
            StructureImportState state,
            LocalDateTime now,
            String actor,
            int[] imported
    ) {
        prefetchStructureBatchLookups(batch, state);
        for (ParsedRow row : batch) {
            String[] cells = row.cells();
            String facultyName = cell(cells, 0);
            String fosName = cell(cells, 1);
            String groupName = cell(cells, 2);

            Long facultyId = state.facultyNameToId.computeIfAbsent(
                    facultyName, name -> {
                        Faculty saved = facultyService.saveEntity(Faculty.builder()
                                                                             .name(name)
                                                                             .shortName(name.length() > 50 ? name.substring(
                                                                                     0,
                                                                                     50
                                                                             ) : name)
                                                                             .createdAt(now).createdBy(actor)
                                                                             .updatedAt(now).updatedBy(actor)
                                                                             .build());
                        return saved.getId();
                    }
            );
            Faculty facultyRef = facultyService.getReference(facultyId);

            Map<String, Long> fosByName = state.fieldOfStudyIdByFacultyIdAndName.computeIfAbsent(
                    facultyId,
                    k -> new HashMap<>()
            );
            Long fosId = fosByName.computeIfAbsent(
                    fosName,
                    fn -> {
                        FieldOfStudy saved = fieldOfStudyService.saveEntity(FieldOfStudy.builder()
                                                                                        .name(fn)
                                                                                        .faculty(facultyRef)
                                                                                        .createdAt(now)
                                                                                        .createdBy(actor)
                                                                                        .updatedAt(now)
                                                                                        .updatedBy(actor)
                                                                                        .build());
                        return saved.getId();
                    }
            );
            FieldOfStudy fosRef = fieldOfStudyService.getReference(fosId);

            studentGroupService.saveEntity(StudentGroup.builder()
                    .name(groupName)
                    .faculty(facultyRef)
                    .fieldOfStudy(fosRef)
                    .createdAt(now).createdBy(actor)
                    .updatedAt(now).updatedBy(actor)
                    .build());
            imported[0]++;
        }
    }

    /**
     * Loads DB ids for faculty names and, per known faculty id, field-of-study names needed in this batch.
     */
    private void prefetchStructureBatchLookups(List<ParsedRow> batch, StructureImportState state) {
        Set<String> facultyNames = new HashSet<>();
        for (ParsedRow row : batch) {
            facultyNames.add(cell(row.cells(), 0));
        }
        List<String> missingFaculty = facultyNames.stream()
                .filter(n -> !state.facultyNameToId.containsKey(n))
                .toList();
        if (!missingFaculty.isEmpty()) {
            state.facultyNameToId.putAll(facultyService.findNameIdMapByNames(missingFaculty));
        }

        Map<Long, Set<String>> fosNamesByFacultyId = new HashMap<>();
        for (ParsedRow row : batch) {
            String[] cells = row.cells();
            String facultyName = cell(cells, 0);
            String fosName = cell(cells, 1);
            Long facultyId = state.facultyNameToId.get(facultyName);
            if (facultyId != null) {
                fosNamesByFacultyId.computeIfAbsent(facultyId, k -> new HashSet<>()).add(fosName);
            }
        }
        for (var e : fosNamesByFacultyId.entrySet()) {
            Long facultyId = e.getKey();
            Set<String> fosNames = e.getValue();
            Map<String, Long> inner = state.fieldOfStudyIdByFacultyIdAndName.computeIfAbsent(
                    facultyId,
                    k -> new HashMap<>()
            );
            List<String> missingFos = fosNames.stream().filter(n -> !inner.containsKey(n)).toList();
            if (!missingFos.isEmpty()) {
                inner.putAll(fieldOfStudyService.findNameIdMapByFacultyIdAndFosNames(facultyId, missingFos));
            }
        }
    }

    /**
     * Holds name→id maps for the whole import (initialized here, not in {@link #importStructure}).
     * Faculty: name → id. Field of study: faculty id → (fos name → id).
     */
    private static final class StructureImportState {
        final Map<String, Long> facultyNameToId = new HashMap<>();
        final Map<Long, Map<String, Long>> fieldOfStudyIdByFacultyIdAndName = new HashMap<>();
    }

    private void validateImportStructureFile(MultipartFile file) {
        List<ImportRowError> errors = new ArrayList<>();

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        String faculty = cell(cells, 0);
                        String fieldOfStudy = cell(cells, 1);
                        String studentGroup = cell(cells, 2);
                        int displayRow = rowIndex + 1;

                        if (errors.size() < MAX_ERRORS) {
                            if (faculty.isBlank())
                                errors.add(new ImportRowError(displayRow, "Институт", "Required field is blank"));
                            if (fieldOfStudy.isBlank())
                                errors.add(new ImportRowError(displayRow, "Направление", "Required field is blank"));
                            if (studentGroup.isBlank())
                                errors.add(new ImportRowError(displayRow, "Группа", "Required field is blank"));
                        }

                    }
            );
        } catch (IOException e) {
            log.error("Не удалось прочитать файл по причине: ", e);
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!errors.isEmpty()) throw new ImportValidationException(errors);
    }

    /**
     * Type 2 — Faculty + Department.
     * Expected columns: [0] Institute/Faculty name, [1] Department name.
     */
    @Transactional
    public ImportResultResponse importDepartments(MultipartFile file) {
        // ── Pass 1 ───────────────────────────────────────────────────────────────
        List<ImportRowError> errors = new ArrayList<>();

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        String faculty = cell(cells, 0);
                        String dept = cell(cells, 1);
                        int displayRow = rowIndex + 1;

                        if (errors.size() < MAX_ERRORS) {
                            if (faculty.isBlank())
                                errors.add(new ImportRowError(displayRow, "Institute", "Required field is blank"));
                            if (dept.isBlank())
                                errors.add(new ImportRowError(displayRow, "Department", "Required field is blank"));
                        }
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        if (!errors.isEmpty()) throw new ImportValidationException(errors);

        // ── Pass 2 ───────────────────────────────────────────────────────────────
        Map<String, Long> facultyNameToId = new HashMap<>(facultyService.findAllAsNameIdMap());
        Map<String, Long> deptKeyToId = new HashMap<>(departmentService.findAllAsCompositeKeyIdMap());

        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        int[] imported = {0};

        try {
            parser.parse(
                    file.getInputStream(), (rowIndex, cells) -> {
                        String facultyName = cell(cells, 0);
                        String deptName = cell(cells, 1);
                        String deptKey = facultyName + "|" + deptName;

                        if (deptKeyToId.containsKey(deptKey)) {
                            return;
                        }

                        Long facultyId = facultyNameToId.computeIfAbsent(
                                facultyName, name -> {
                                    Faculty saved = facultyService.saveEntity(Faculty.builder()
                                                                                     .name(name)
                                                                                     .shortName(name.length() > 50 ? name.substring(
                                                                                             0,
                                                                                             50
                                                                                     ) : name)
                                                                                     .createdAt(now).createdBy(actor)
                                                                                     .updatedAt(now).updatedBy(actor)
                                                                                     .build());
                                    return saved.getId();
                                }
                        );

                        Department saved = departmentService.saveEntity(Department.builder()
                                                                                  .name(deptName)
                                                                                  .faculty(facultyService.getReference(
                                                                                          facultyId))
                                                                                  .createdAt(now).createdBy(actor)
                                                                                  .updatedAt(now).updatedBy(actor)
                                                                                  .build());
                        deptKeyToId.put(deptKey, saved.getId());
                        imported[0]++;
                    }
            );
        } catch (IOException e) {
            throw new BadRequestException("FILE_READ_ERROR", "Cannot read uploaded file");
        }

        return new ImportResultResponse(imported[0]);
    }

    private static String cell(String[] cells, int index) {
        return (cells.length > index && cells[index] != null) ? cells[index] : "";
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "import";
    }
}
