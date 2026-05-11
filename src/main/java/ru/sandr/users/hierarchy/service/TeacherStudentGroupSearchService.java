package ru.sandr.users.hierarchy.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.core.exception.UnauthorizedException;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.mapper.StudentGroupMapper;
import ru.sandr.users.hierarchy.repository.StudentGroupRepository;
import ru.sandr.users.user.repository.TeacherProfileRepository;
import ru.sandr.users.teacheraccess.service.TeacherGroupAccessService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherStudentGroupSearchService {

    private static final Long NON_EXISTENT_ID = -1L;

    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherGroupAccessService teacherGroupAccessService;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentGroupMapper studentGroupMapper;

    @Transactional(readOnly = true)
    public Page<StudentGroupResponse> searchAccessibleStudentGroups(String query, Pageable pageable) {
        UUID teacherId = currentTeacherId();
        TeacherGroupAccessService.TeacherScopeIds scopeIds = teacherGroupAccessService.getScopeIds(teacherId);
        if (scopeIds.isEmpty()) {
            return Page.empty(pageable);
        }

        String normalizedQuery = StringUtils.isBlank(query) ? null : query.trim();

        return studentGroupRepository.searchAccessibleForTeacher(
                nonEmptyOrFallback(scopeIds.groupIds()),
                nonEmptyOrFallback(scopeIds.fieldOfStudyIds()),
                nonEmptyOrFallback(scopeIds.facultyIds()),
                !scopeIds.groupIds().isEmpty(),
                !scopeIds.fieldOfStudyIds().isEmpty(),
                !scopeIds.facultyIds().isEmpty(),
                normalizedQuery,
                pageable
        ).map(studentGroupMapper::toResponse);
    }

    private UUID currentTeacherId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractCurrentUserId(auth);
        return teacherProfileRepository.findByUser_Id(userId)
                                       .map(teacherProfile -> teacherProfile.getId())
                                       .orElseThrow(() -> new ObjectNotFoundException(
                                               "TEACHER_NOT_FOUND",
                                               "Teacher profile not found for authenticated user"
                                       ));
    }

    private UUID extractCurrentUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new UnauthorizedException("AUTHENTICATION_REQUIRED", "Authenticated teacher not found");
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("INVALID_AUTH_PRINCIPAL", "Invalid authenticated principal format");
        }
    }

    private Collection<Long> nonEmptyOrFallback(Collection<Long> ids) {
        return ids.isEmpty() ? List.of(NON_EXISTENT_ID) : ids;
    }
}
