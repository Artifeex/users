package ru.sandr.users.teacheraccess.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.dto.PageResponse;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.core.validation.PageableValidator;
import ru.sandr.users.hierarchy.service.FacultyService;
import ru.sandr.users.hierarchy.service.FieldOfStudyService;
import ru.sandr.users.hierarchy.service.StudentGroupService;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessScopeDetailsResponse;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessScopeRequest;
import ru.sandr.users.user.dto.RoleName;
import ru.sandr.users.user.entity.TeacherProfile;
import ru.sandr.users.user.repository.TeacherProfileRepository;
import ru.sandr.users.user.repository.UserRoleRepository;
import ru.sandr.users.teacheraccess.dto.ReplaceTeacherGroupAccessRequest;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessResponse;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessScopeResponse;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScope;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeId;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;
import ru.sandr.users.teacheraccess.repository.TeacherGroupAccessScopeRepository;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Observed(name = "users.teacher-group-access")
@Service
@RequiredArgsConstructor
public class TeacherGroupAccessService {

    private final TeacherGroupAccessScopeRepository teacherGroupAccessScopeRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final StudentGroupService studentGroupService;
    private final FieldOfStudyService fieldOfStudyService;
    private final FacultyService facultyService;

    @Transactional
    public TeacherGroupAccessScopeResponse addTeacherScope(UUID teacherId, TeacherGroupAccessScopeRequest request) {
        TeacherProfile teacher = ensureTeacherIsValid(teacherId);
        validateScopeExists(request.scopeType(), request.scopeId());

        TeacherGroupAccessScopeId scopeId = new TeacherGroupAccessScopeId(teacherId, request.scopeType(), request.scopeId());
        if (teacherGroupAccessScopeRepository.existsById(scopeId)) {
            throw new BadRequestException(
                    "TEACHER_GROUP_ACCESS_SCOPE_EXISTS",
                    "Teacher scope already exists for teacherId=%s, scopeType=%s, scopeId=%s"
                            .formatted(teacherId, request.scopeType(), request.scopeId())
            );
        }

        TeacherGroupAccessScope saved = teacherGroupAccessScopeRepository.save(
                TeacherGroupAccessScope.builder()
                                       .id(scopeId)
                                       .teacher(teacher)
                                       .build()
        );
        return toScopeResponse(saved);
    }

    @Transactional
    public void deleteTeacherScope(UUID teacherId, TeacherGroupAccessScopeType scopeType, Long scopeId) {
        ensureTeacherIsValid(teacherId);
        TeacherGroupAccessScopeId id = new TeacherGroupAccessScopeId(teacherId, scopeType, scopeId);
        if (!teacherGroupAccessScopeRepository.existsById(id)) {
            throw new ObjectNotFoundException(
                    "TEACHER_GROUP_ACCESS_SCOPE_NOT_FOUND",
                    "Teacher scope not found for teacherId=%s, scopeType=%s, scopeId=%s"
                            .formatted(teacherId, scopeType, scopeId)
            );
        }
        teacherGroupAccessScopeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PageResponse<TeacherGroupAccessScopeResponse> findTeacherScopesByType(UUID teacherId,
                                                                                  TeacherGroupAccessScopeType scopeType,
                                                                                  Pageable pageable) {
        ensureTeacherIsValid(teacherId);
        Pageable safePageable = PageableValidator.validateAndMap(pageable, Map.of("scopeId", "id.scopeId"));
        return new PageResponse<>(
                teacherGroupAccessScopeRepository.findAllByTeacher_IdAndId_ScopeType(teacherId, scopeType, safePageable)
                                                 .map(this::toScopeResponse)
        );
    }

    @Transactional(readOnly = true)
    public TeacherGroupAccessResponse getTeacherScopes(UUID teacherId) {
        ensureTeacherIsValid(teacherId);
        return buildResponse(teacherId, teacherGroupAccessScopeRepository.findAllByTeacher_Id(teacherId));
    }

    @Transactional(readOnly = true)
    public List<TeacherGroupAccessScopeDetailsResponse> findTeacherScopesWithDetails(UUID teacherId) {
        ensureTeacherIsValid(teacherId);
        return teacherGroupAccessScopeRepository.findAllByTeacher_Id(teacherId).stream()
                                                .map(this::toScopeDetailsResponse)
                                                .sorted(Comparator.comparing(TeacherGroupAccessScopeDetailsResponse::scopeType)
                                                                  .thenComparing(TeacherGroupAccessScopeDetailsResponse::scopeId))
                                                .toList();
    }

    @Transactional
    public TeacherGroupAccessResponse replaceTeacherScopes(UUID teacherId, ReplaceTeacherGroupAccessRequest request) {
        TeacherProfile teacher = ensureTeacherIsValid(teacherId);
        Set<TeacherGroupAccessScopeId> uniqueScopes = new LinkedHashSet<>();
        for (var scopeRequest : request.scopes()) {
            validateScopeExists(scopeRequest.scopeType(), scopeRequest.scopeId());
            uniqueScopes.add(new TeacherGroupAccessScopeId(teacherId, scopeRequest.scopeType(), scopeRequest.scopeId()));
        }

        teacherGroupAccessScopeRepository.deleteByTeacher_Id(teacherId);
        List<TeacherGroupAccessScope> saved = teacherGroupAccessScopeRepository.saveAll(
                uniqueScopes.stream()
                            .map(id -> TeacherGroupAccessScope.builder()
                                                              .id(id)
                                                              .teacher(teacher)
                                                              .build())
                            .toList()
        );
        return buildResponse(teacherId, saved);
    }

    @Transactional(readOnly = true)
    public TeacherScopeIds getScopeIds(UUID teacherId) {
        ensureTeacherIsValid(teacherId);
        List<TeacherGroupAccessScope> scopes = teacherGroupAccessScopeRepository.findAllByTeacher_Id(teacherId);
        Set<Long> groupIds = new LinkedHashSet<>();
        Set<Long> fieldIds = new LinkedHashSet<>();
        Set<Long> facultyIds = new LinkedHashSet<>();
        for (TeacherGroupAccessScope scope : scopes) {
            if (scope.getId().getScopeType() == TeacherGroupAccessScopeType.STUDENT_GROUP) {
                groupIds.add(scope.getId().getScopeId());
            } else if (scope.getId().getScopeType() == TeacherGroupAccessScopeType.FIELD_OF_STUDY) {
                fieldIds.add(scope.getId().getScopeId());
            } else if (scope.getId().getScopeType() == TeacherGroupAccessScopeType.FACULTY) {
                facultyIds.add(scope.getId().getScopeId());
            }
        }
        return new TeacherScopeIds(groupIds, fieldIds, facultyIds);
    }

    private TeacherProfile ensureTeacherIsValid(UUID teacherId) {
        TeacherProfile teacherProfile = teacherProfileRepository.findById(teacherId)
                                                                .orElseThrow(() -> new ObjectNotFoundException(
                                                                        "TEACHER_NOT_FOUND",
                                                                        "Teacher profile not found: " + teacherId
                                                                ));
        boolean isTeacher = userRoleRepository.existsByUserIdAndRoleName(teacherId, RoleName.ROLE_TEACHER.name());
        if (!isTeacher) {
            throw new BadRequestException("USER_NOT_TEACHER", "User is not assigned ROLE_TEACHER: " + teacherId);
        }
        return teacherProfile;
    }

    private void validateScopeExists(TeacherGroupAccessScopeType scopeType, Long scopeId) {
        if (scopeId == null || scopeId <= 0) {
            throw new BadRequestException("INVALID_SCOPE_ID", "scopeId must be a positive number");
        }
        if (scopeType == TeacherGroupAccessScopeType.STUDENT_GROUP) {
            studentGroupService.getById(scopeId);
        } else if (scopeType == TeacherGroupAccessScopeType.FIELD_OF_STUDY) {
            fieldOfStudyService.getById(scopeId);
        } else if (scopeType == TeacherGroupAccessScopeType.FACULTY) {
            facultyService.getById(scopeId);
        }
    }

    private TeacherGroupAccessResponse buildResponse(UUID teacherId, List<TeacherGroupAccessScope> scopes) {
        List<TeacherGroupAccessScopeResponse> items = scopes.stream()
                                                            .map(this::toScopeResponse)
                                                            .sorted(Comparator.comparing(TeacherGroupAccessScopeResponse::scopeType)
                                                                              .thenComparing(TeacherGroupAccessScopeResponse::scopeId))
                                                            .toList();
        return new TeacherGroupAccessResponse(teacherId, items);
    }

    private TeacherGroupAccessScopeResponse toScopeResponse(TeacherGroupAccessScope scope) {
        return new TeacherGroupAccessScopeResponse(
                scope.getId().getScopeType(),
                scope.getId().getScopeId()
        );
    }

    private TeacherGroupAccessScopeDetailsResponse toScopeDetailsResponse(TeacherGroupAccessScope scope) {
        TeacherGroupAccessScopeType scopeType = scope.getId().getScopeType();
        Long scopeId = scope.getId().getScopeId();
        return new TeacherGroupAccessScopeDetailsResponse(
                scopeId,
                scopeType,
                resolveScopeName(scopeType, scopeId)
        );
    }

    private String resolveScopeName(TeacherGroupAccessScopeType scopeType, Long scopeId) {
        if (scopeType == TeacherGroupAccessScopeType.STUDENT_GROUP) {
            return studentGroupService.getById(scopeId).name();
        }
        if (scopeType == TeacherGroupAccessScopeType.FIELD_OF_STUDY) {
            return fieldOfStudyService.getById(scopeId).name();
        }
        return facultyService.getById(scopeId).name();
    }

    public record TeacherScopeIds(Set<Long> groupIds, Set<Long> fieldOfStudyIds, Set<Long> facultyIds) {
        public boolean isEmpty() {
            return groupIds.isEmpty() && fieldOfStudyIds.isEmpty() && facultyIds.isEmpty();
        }
    }
}
