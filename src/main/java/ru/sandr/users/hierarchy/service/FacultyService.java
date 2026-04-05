package ru.sandr.users.hierarchy.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.hierarchy.dto.CreateFacultyRequest;
import ru.sandr.users.hierarchy.dto.FacultyResponse;
import ru.sandr.users.hierarchy.dto.UpdateFacultyRequest;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.mapper.FacultyMapper;
import ru.sandr.users.hierarchy.repository.DepartmentRepository;
import ru.sandr.users.hierarchy.repository.FacultyRepository;
import ru.sandr.users.hierarchy.repository.FieldOfStudyRepository;
import ru.sandr.users.hierarchy.repository.StudentGroupRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final FieldOfStudyRepository fieldOfStudyRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final FacultyMapper facultyMapper;

    @Transactional
    public FacultyResponse create(CreateFacultyRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        Faculty faculty = Faculty.builder()
                .name(request.name())
                .shortName(request.shortName())
                .createdAt(now)
                .createdBy(actor)
                .updatedAt(now)
                .updatedBy(actor)
                .build();
        return facultyMapper.toResponse(facultyRepository.save(faculty));
    }

    @Transactional(readOnly = true)
    public FacultyResponse getById(Long id) {
        return facultyMapper.toResponse(findFacultyOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<FacultyResponse> findAll(Pageable pageable) {
        return facultyRepository.findAll(pageable).map(facultyMapper::toResponse);
    }

    @Transactional
    public FacultyResponse update(Long id, UpdateFacultyRequest request) {
        Faculty faculty = findFacultyOrThrow(id);
        if (request.name() != null) {
            faculty.setName(request.name());
        }
        if (request.shortName() != null) {
            faculty.setShortName(request.shortName());
        }
        LocalDateTime now = LocalDateTime.now();
        faculty.setUpdatedAt(now);
        faculty.setUpdatedBy(currentUsername());
        return facultyMapper.toResponse(faculty);
    }

    @Transactional
    public void delete(Long id) {
        findFacultyOrThrow(id);
        if (departmentRepository.existsByFaculty_Id(id)
                || fieldOfStudyRepository.existsByFaculty_Id(id)
                || studentGroupRepository.existsByFaculty_Id(id)) {
            throw new BadRequestException(
                    "FACULTY_HAS_DEPENDENCIES",
                    "Cannot delete faculty with linked departments, fields of study, or student groups"
            );
        }
        facultyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Faculty> findByNamesIn(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return Map.of();
        }
        return facultyRepository.findAllByNameIn(names).stream()
                                .collect(Collectors.toMap(Faculty::getName, f -> f, (a, b) -> a));
    }

    @Transactional
    public Map<String, Faculty> bulkUpsertByNames(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return Map.of();
        }

        Map<String, Faculty> facultyByName = new LinkedHashMap<>(findByNamesIn(names));
        boolean hasNew = false;
        for (String name : names) {
            if (StringUtils.isBlank(name) || facultyByName.containsKey(name)) {
                continue;
            }
            Faculty created = Faculty.builder()
                                     .name(name)
                                     .shortName(buildFacultyShortName(name))
                                     .build();
            facultyByName.put(name, created);
            hasNew = true;
        }

        if (hasNew) {
            facultyRepository.saveAll(facultyByName.values());
        }

        return facultyByName;
    }

    private Faculty findFacultyOrThrow(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("FACULTY_NOT_FOUND", "Faculty not found: " + id));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private String buildFacultyShortName(String fullName) {
        String normalized = fullName.trim();
        if (normalized.length() <= 50) {
            return normalized;
        }
        return normalized.substring(0, 50);
    }
}
