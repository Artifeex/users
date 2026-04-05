package ru.sandr.users.hierarchy.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.core.exception.MissedRequiredArgument;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.hierarchy.dto.CreateFieldOfStudyRequest;
import ru.sandr.users.hierarchy.dto.FieldOfStudyResponse;
import ru.sandr.users.hierarchy.dto.UpdateFieldOfStudyRequest;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;
import ru.sandr.users.hierarchy.mapper.FieldOfStudyMapper;
import ru.sandr.users.hierarchy.repository.FacultyRepository;
import ru.sandr.users.hierarchy.repository.FieldOfStudyRepository;
import ru.sandr.users.hierarchy.repository.StudentGroupRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FieldOfStudyService {

    private static final int NAME_MAX_LEN = 255;

    private final FieldOfStudyRepository fieldOfStudyRepository;
    private final FacultyRepository facultyRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final FieldOfStudyMapper fieldOfStudyMapper;

    @Transactional
    public FieldOfStudyResponse create(CreateFieldOfStudyRequest request) {
        var faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "FACULTY_NOT_FOUND",
                        "Faculty not found: " + request.facultyId()
                ));
        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        FieldOfStudy field = FieldOfStudy.builder()
                .name(request.name())
                .faculty(faculty)
                .createdAt(now)
                .createdBy(actor)
                .updatedAt(now)
                .updatedBy(actor)
                .build();
        return fieldOfStudyMapper.toResponse(fieldOfStudyRepository.save(field));
    }

    @Transactional(readOnly = true)
    public FieldOfStudyResponse getById(Long id) {
        return fieldOfStudyMapper.toResponse(findFieldOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<FieldOfStudyResponse> findAll(Pageable pageable) {
        return fieldOfStudyRepository.findAll(pageable).map(fieldOfStudyMapper::toResponse);
    }

    @Transactional
    public FieldOfStudyResponse update(Long id, UpdateFieldOfStudyRequest request) {
        FieldOfStudy field = findFieldOrThrow(id);
        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new MissedRequiredArgument("NAME_BLANK", "Name must not be blank when provided");
            }
            if (request.name().length() > NAME_MAX_LEN) {
                throw new BadRequestException("NAME_TOO_LONG", "Name must be at most " + NAME_MAX_LEN + " characters");
            }
            field.setName(request.name());
        }
        if (request.facultyId() != null) {
            var faculty = facultyRepository.findById(request.facultyId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "FACULTY_NOT_FOUND",
                            "Faculty not found: " + request.facultyId()
                    ));
            field.setFaculty(faculty);
        }
        LocalDateTime now = LocalDateTime.now();
        field.setUpdatedAt(now);
        field.setUpdatedBy(currentUsername());
        return fieldOfStudyMapper.toResponse(field);
    }

    @Transactional
    public void delete(Long id) {
        findFieldOrThrow(id);
        if (studentGroupRepository.existsByFieldOfStudy_Id(id)) {
            throw new BadRequestException(
                    "FIELD_OF_STUDY_HAS_GROUPS",
                    "Cannot delete field of study with linked student groups"
            );
        }
        fieldOfStudyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, FieldOfStudy> findByNamesIn(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return Map.of();
        }
        return fieldOfStudyRepository.findAllByNameIn(names).stream()
                                     .collect(Collectors.toMap(FieldOfStudy::getName, f -> f, (a, b) -> a));
    }

    @Transactional
    public Map<String, FieldOfStudy> bulkUpsertByNames(Collection<FieldOfStudyUpsertRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }

        Map<String, Faculty> facultyByFieldOfStudyName = new HashMap<>();
        for (FieldOfStudyUpsertRow row : rows) {
            if (StringUtils.isBlank(row.name()) || row.faculty() == null) {
                continue;
            }
            facultyByFieldOfStudyName.put(row.name(), row.faculty());
        }
        if (facultyByFieldOfStudyName.isEmpty()) {
            return Map.of();
        }

        Map<String, FieldOfStudy> fieldByName = findByNamesIn(facultyByFieldOfStudyName.keySet());
        for (Map.Entry<String, Faculty> entry : facultyByFieldOfStudyName.entrySet()) {
            String fieldName = entry.getKey();
            Faculty faculty = entry.getValue();

            FieldOfStudy existing = fieldByName.get(fieldName);
            if (existing != null) {
                existing.setFaculty(faculty);
            } else {
                FieldOfStudy created = FieldOfStudy.builder()
                                                   .name(fieldName)
                                                   .faculty(faculty)
                                                   .build();
                fieldByName.put(fieldName, created);
            }
        }

        fieldOfStudyRepository.saveAll(fieldByName.values());
        return fieldByName;
    }

    private FieldOfStudy findFieldOrThrow(Long id) {
        return fieldOfStudyRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "FIELD_OF_STUDY_NOT_FOUND",
                        "Field of study not found: " + id
                ));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    public record FieldOfStudyUpsertRow(String name, Faculty faculty) {
    }
}
