package ru.sandr.users.hierarchy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.core.exception.MissedRequiredArgument;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.hierarchy.dto.CreateStudentGroupRequest;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.dto.UpdateStudentGroupRequest;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;
import ru.sandr.users.hierarchy.entity.StudentGroup;
import ru.sandr.users.hierarchy.mapper.StudentGroupMapper;
import ru.sandr.users.hierarchy.repository.FacultyRepository;
import ru.sandr.users.hierarchy.repository.FieldOfStudyRepository;
import ru.sandr.users.hierarchy.repository.StudentGroupRepository;
import ru.sandr.users.user.service.StudentProfileService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentGroupService {

    private final StudentGroupRepository studentGroupRepository;
    private final FacultyRepository facultyRepository;
    private final FieldOfStudyRepository fieldOfStudyRepository;
    private final StudentProfileService studentProfileService;
    private final StudentGroupMapper studentGroupMapper;

    @Transactional
    public StudentGroupResponse create(CreateStudentGroupRequest request) {
        Faculty faculty = findFacultyOrThrow(request.facultyId());
        FieldOfStudy field = findFieldOrThrow(request.fieldOfStudyId());
        ensureFieldBelongsToFaculty(field, faculty);
        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        StudentGroup group = StudentGroup.builder()
                .name(request.name())
                .faculty(faculty)
                .fieldOfStudy(field)
                .createdAt(now)
                .createdBy(actor)
                .updatedAt(now)
                .updatedBy(actor)
                .build();
        return studentGroupMapper.toResponse(studentGroupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public StudentGroupResponse getById(Long id) {
        return studentGroupMapper.toResponse(findGroupOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<StudentGroupResponse> findAll(Pageable pageable) {
        return studentGroupRepository.findAll(pageable).map(studentGroupMapper::toResponse);
    }

    @Transactional
    public StudentGroupResponse update(Long id, UpdateStudentGroupRequest request) {
        StudentGroup group = findGroupOrThrow(id);
        Faculty faculty;
        FieldOfStudy field;

        if (request.name() != null) {
            group.setName(request.name());
        }
        if (request.facultyId() != null) {
            faculty = findFacultyOrThrow(request.facultyId());
            group.setFaculty(faculty);
        }
        if (request.fieldOfStudyId() != null) {
            field = findFieldOrThrow(request.fieldOfStudyId());
            group.setFieldOfStudy(field);
        }
        ensureFieldBelongsToFaculty(group.getFieldOfStudy(), group.getFaculty());

        LocalDateTime now = LocalDateTime.now();
        group.setUpdatedAt(now);
        group.setUpdatedBy(currentUsername());
        return studentGroupMapper.toResponse(group);
    }

    @Transactional
    public void delete(Long id) {
        findGroupOrThrow(id);
        if (studentProfileService.hasStudentsInGroup(id)) {
            throw new BadRequestException(
                    "STUDENT_GROUP_HAS_STUDENTS",
                    "Cannot delete student group with assigned students"
            );
        }
        studentGroupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> findGroupIdsByNamesIn(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return Map.of();
        }
        return studentGroupRepository.findByNameIn(names).stream()
                .collect(Collectors.toMap(
                        StudentGroupRepository.NameIdProjection::getName,
                        StudentGroupRepository.NameIdProjection::getId,
                        (a, b) -> a
                ));
    }

    private StudentGroup findGroupOrThrow(Long id) {
        return studentGroupRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "STUDENT_GROUP_NOT_FOUND",
                        "Student group not found: " + id
                ));
    }

    private Faculty findFacultyOrThrow(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "FACULTY_NOT_FOUND",
                        "Faculty not found: " + id
                ));
    }

    private FieldOfStudy findFieldOrThrow(Long id) {
        return fieldOfStudyRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "FIELD_OF_STUDY_NOT_FOUND",
                        "Field of study not found: " + id
                ));
    }

    private void ensureFieldBelongsToFaculty(FieldOfStudy field, Faculty faculty) {
        if (!field.getFaculty().getId().equals(faculty.getId())) {
            throw new BadRequestException(
                    "FIELD_FACULTY_MISMATCH",
                    "Field of study does not belong to the selected faculty"
            );
        }
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
