package ru.sandr.users.hierarchy.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.core.exception.MissedRequiredArgument;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.hierarchy.dto.CreateDepartmentRequest;
import ru.sandr.users.hierarchy.dto.DepartmentResponse;
import ru.sandr.users.hierarchy.dto.UpdateDepartmentRequest;
import ru.sandr.users.hierarchy.entity.Department;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.mapper.DepartmentMapper;
import ru.sandr.users.hierarchy.repository.DepartmentRepository;
import ru.sandr.users.hierarchy.repository.FacultyRepository;
import ru.sandr.users.user.service.TeacherProfileService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final TeacherProfileService teacherProfileService;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        var faculty = facultyRepository.findById(request.facultyId())
                                       .orElseThrow(() -> new ObjectNotFoundException(
                                               "FACULTY_NOT_FOUND",
                                               "Faculty not found: " + request.facultyId()
                                       ));
        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        Department department = Department.builder()
                                          .name(request.name())
                                          .faculty(faculty)
                                          .createdAt(now)
                                          .createdBy(actor)
                                          .updatedAt(now)
                                          .updatedBy(actor)
                                          .build();
        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return departmentMapper.toResponse(findDepartmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(departmentMapper::toResponse);
    }

    @Transactional
    public DepartmentResponse update(Long id, UpdateDepartmentRequest request) {
        Department department = findDepartmentOrThrow(id);
        if (request.name() != null) {
            department.setName(request.name());
        }
        if (request.facultyId() != null) {
            var faculty = facultyRepository.findById(request.facultyId())
                                           .orElseThrow(() -> new ObjectNotFoundException(
                                                   "FACULTY_NOT_FOUND",
                                                   "Faculty not found: " + request.facultyId()
                                           ));
            department.setFaculty(faculty);
        }
        LocalDateTime now = LocalDateTime.now();
        department.setUpdatedAt(now);
        department.setUpdatedBy(currentUsername());
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public void delete(Long id) {
        findDepartmentOrThrow(id);
        if (teacherProfileService.hasTeachersInDepartment(id)) {
            throw new BadRequestException(
                    "DEPARTMENT_HAS_TEACHERS",
                    "Cannot delete department with assigned teachers"
            );
        }
        departmentRepository.deleteById(id);
    }

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                                   .orElseThrow(() -> new ObjectNotFoundException(
                                           "DEPARTMENT_NOT_FOUND",
                                           "Department not found: " + id
                                   ));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    @Transactional(readOnly = true)
    public Map<String, Long> findDepartmentIdsByNamesIs(Set<String> departmentNames) {
        if (CollectionUtils.isEmpty(departmentNames)) {
            return Map.of();
        }
        return departmentRepository.findByDepartmentNameIn(departmentNames).stream().collect(Collectors.toMap(
                DepartmentRepository.DepartmentProjection::getName,
                DepartmentRepository.DepartmentProjection::getId
        ));
    }

    @Transactional(readOnly = true)
    public Map<String, Department> findByNamesIn(Collection<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Map.of();
        }
        return departmentRepository.findAllByNameIn(names).stream()
                                   .collect(Collectors.toMap(Department::getName, d -> d, (a, b) -> a));
    }

    @Transactional
    public Map<String, Department> bulkUpsertByNames(Collection<DepartmentUpsertRow> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return Map.of();
        }

        Map<String, DepartmentUpsertRow> uniqueByName = new LinkedHashMap<>();
        for (DepartmentUpsertRow row : rows) {
            if (StringUtils.isBlank(row.name()) || row.faculty() == null) {
                continue;
            }
            uniqueByName.put(row.name(), row);
        }
        if (uniqueByName.isEmpty()) {
            return Map.of();
        }

        Map<String, Department> departmentsByName = new LinkedHashMap<>(findByNamesIn(uniqueByName.keySet()));
        for (DepartmentUpsertRow row : uniqueByName.values()) {
            Department existing = departmentsByName.get(row.name());
            if (existing != null) {
                existing.setFaculty(row.faculty());
            } else {
                Department created = Department.builder()
                                               .name(row.name())
                                               .faculty(row.faculty())
                                               .build();
                departmentsByName.put(row.name(), created);
            }
        }

        departmentRepository.saveAll(departmentsByName.values());
        return departmentsByName;
    }

    public record DepartmentUpsertRow(String name, Faculty faculty) {
    }
}
