package ru.sandr.users.hierarchy.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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
import ru.sandr.users.hierarchy.mapper.DepartmentMapper;
import ru.sandr.users.hierarchy.repository.DepartmentRepository;
import ru.sandr.users.hierarchy.repository.FacultyRepository;
import ru.sandr.users.user.service.TeacherProfileService;

import java.time.LocalDateTime;
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

    // ── Import helpers ────────────────────────────────────────────────────────

    /**
     * Returns name → id via scalar projection — no entity objects, no session tracking.
     * Used by UserImportService to validate and resolve teacher department names.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> findAllAsNameMap() {
        return departmentRepository.findAllNameIdProjections().stream()
                                   .collect(Collectors.toMap(
                                           DepartmentRepository.NameIdProjection::getName,
                                           DepartmentRepository.NameIdProjection::getId,
                                           (a, b) -> a
                                   ));
    }

    /**
     * Returns "facultyName|deptName" → deptId via scalar projection.
     * Used by HierarchyImportService for composite-key dedup before Pass 2.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> findAllAsCompositeKeyIdMap() {
        return departmentRepository.findAllCompositeProjections().stream()
                                   .collect(Collectors.toMap(
                                           p -> p.getFacultyName() + "|" + p.getDeptName(),
                                           DepartmentRepository.CompositeProjection::getId,
                                           (a, b) -> a
                                   ));
    }

    /**
     * Returns a Hibernate proxy for FK assignment — no SELECT is issued.
     */
    public Department getReference(Long id) {
        return departmentRepository.getReferenceById(id);
    }

    @Transactional
    public Department saveEntity(Department department) {
        return departmentRepository.save(department);
    }

    // ─────────────────────────────────────────────────────────────────────────

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
}
