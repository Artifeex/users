package ru.sandr.users.user.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.exception.ConflictException;
import ru.sandr.users.core.exception.MissedRequiredArgument;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.hierarchy.entity.StudentGroup;
import ru.sandr.users.hierarchy.repository.DepartmentRepository;
import ru.sandr.users.hierarchy.repository.StudentGroupRepository;
import ru.sandr.users.security.utils.PasswordAndTokenGenerator;
import ru.sandr.users.user.dto.*;
import ru.sandr.users.user.entity.*;
import ru.sandr.users.user.events.UserCreatedEvent;
import ru.sandr.users.user.mapper.UserMapper;
import ru.sandr.users.user.repository.*;

import ru.sandr.users.imports.dto.StudentImportRow;
import ru.sandr.users.imports.dto.TeacherImportRow;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("USERNAME_TAKEN", "Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("EMAIL_TAKEN", "Email already in use: " + request.email());
        }

        var role = roleRepository.findByName(request.role().name())
                                 .orElseThrow(() -> new ObjectNotFoundException(
                                         "ROLE_NOT_FOUND",
                                         "Role not found: " + request.role().name()
                                 ));

        String tempPassword = PasswordAndTokenGenerator.generate(PASSWORD_LENGTH);
        String actor = currentUsername();
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                        .username(request.username())
                        .email(request.email())
                        .password(passwordEncoder.encode(tempPassword)) // Подумат о том, чтобы tmp пароли сохранить без шифрования, чтобы можно было отдать потом excel файлик с паролями ученикам через старост(если через почту не получится)
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .middleName(request.middleName())
                        .active(true)
                        .createdAt(now)
                        .createdBy(actor)
                        .updatedAt(now)
                        .updatedBy(actor)
                        .build();

        User savedUser = userRepository.save(user);

        UserRole userRole = UserRole.builder()
                                    .id(new UserRoleId(savedUser.getId(), role.getId()))
                                    .user(savedUser)
                                    .role(role)
                                    .build();
        userRoleRepository.save(userRole);
        // Это нужно, поскольку мы будем отдавать на фронт mapping из savedUser
        savedUser.getUserRoles().add(userRole);

        if (request.role() == RoleName.ROLE_STUDENT) {
            createProfileForStudent(request, savedUser);
        } else if (request.role() == RoleName.ROLE_TEACHER) {
            createProfileForTeacher(request, savedUser);
        }
        eventPublisher.publishEvent(
                UserCreatedEvent.builder()
                                .userId(savedUser.getId())
                                .email(savedUser.getEmail())
                                .username(savedUser.getUsername())
                                .firstName(savedUser.getFirstName())
                                .lastName(savedUser.getLastName())
                                .temporaryPassword(savedUser.getPassword())
                                .build()

        );
        return userMapper.toResponse(savedUser);
    }

    private void createProfileForTeacher(CreateUserRequest request, User savedUser) {
        if (request.departmentId() == null) {
            throw new MissedRequiredArgument("DEPARTMENT_REQUIRED", "departmentId is required for TEACHER role");
        }
        var department = departmentRepository.findById(request.departmentId())
                                             .orElseThrow(() -> new ObjectNotFoundException(
                                                     "DEPARTMENT_NOT_FOUND",
                                                     "Department not found: " + request.departmentId()
                                             ));
        var teacherProfile = TeacherProfile.builder().user(savedUser).department(department).build();
        teacherProfileRepository.save(teacherProfile);
    }

    private void createProfileForStudent(CreateUserRequest request, User savedUser) {
        if (request.groupId() == null) {
            throw new MissedRequiredArgument("GROUP_REQUIRED", "groupId is required for STUDENT role");
        }
        var group = studentGroupRepository.findById(request.groupId())
                                          .orElseThrow(() -> new ObjectNotFoundException(
                                                  "GROUP_NOT_FOUND",
                                                  "Student group not found: " + request.groupId()
                                          ));
        studentProfileRepository.save(StudentProfile.builder().user(savedUser).group(group).build());
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserByAdminRequest request) {
        User updatedUser = findUserOrThrow(id);
        String actor = currentUsername();
        LocalDateTime now = LocalDateTime.now();
        if (StringUtils.isNotBlank(request.email()) && !Objects.equals(request.email(), updatedUser.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException("EMAIL_TAKEN", "Email already in use: " + request.email());
            }
            updatedUser.setEmail(request.email());
        }
        if (StringUtils.isNotBlank(request.username()) && !Objects.equals(
                request.username(),
                updatedUser.getUsername()
        )) {
            if (userRepository.existsByUsername(request.username())) {
                throw new ConflictException("USERNAME_TAKEN", "Username already in use: " + request.email());
            }
            updatedUser.setUsername(request.username());
        }
        if (StringUtils.isNotBlank(request.firstName())) {
            updatedUser.setFirstName(request.firstName());
        }
        if (StringUtils.isNotBlank(request.lastName())) {
            updatedUser.setLastName(request.lastName());
        }
        if (StringUtils.isNotBlank(request.middleName())) {
            updatedUser.setMiddleName(request.middleName());
        }

        updatedUser.setUpdatedAt(now);
        updatedUser.setUpdatedBy(actor);

        // Roles (and role-bound profiles) first so groupId/departmentId updates see the correct profiles
        // (e.g. TEACHER → STUDENT with groupId in one request).
        if (CollectionUtils.isNotEmpty(request.roles())) {
            updateUserRolesIfChanged(updatedUser, request, now);
        }
        if (request.groupId() != null) {
            updateStudentGroup(id, request);
        }
        if (request.departmentId() != null) {
            updateDepartment(id, request);
        }

        return userMapper.toResponse(userRepository.save(updatedUser));
    }

    private void updateDepartment(UUID id, UpdateUserByAdminRequest request) {
        var department = departmentRepository.findById(request.departmentId())
                                             .orElseThrow(() -> new ObjectNotFoundException(
                                                     "DEPARTMENT_NOT_FOUND",
                                                     "Department not found: " + request.departmentId()
                                             ));
        var teacherProfile = teacherProfileRepository.findById(id)
                                                     .orElseThrow(() -> new ObjectNotFoundException(
                                                             "PROFILE_NOT_FOUND",
                                                             "Teacher profile not found for user: " + id
                                                     ));
        teacherProfile.setDepartment(department);
        teacherProfileRepository.save(teacherProfile);
    }

    private void updateStudentGroup(UUID id, UpdateUserByAdminRequest request) {
        var group = studentGroupRepository.findById(request.groupId())
                                          .orElseThrow(() -> new ObjectNotFoundException(
                                                  "GROUP_NOT_FOUND",
                                                  "Student group not found: " + request.groupId()
                                          ));

        var studentProfile = studentProfileRepository.findById(id)
                                                     .orElseThrow(() -> new ObjectNotFoundException(
                                                             "PROFILE_NOT_FOUND",
                                                             "Student profile not found for user: " + id
                                                     ));
        studentProfile.setGroup(group);
        studentProfileRepository.save(studentProfile);
    }

    private void updateUserRolesIfChanged(User user, UpdateUserByAdminRequest request, LocalDateTime now) {
        List<Role> resolved = resolveAndDedupeRoles(request.roles());
        Set<Integer> resolvedIds = resolved.stream().map(Role::getId).collect(Collectors.toSet());
        Set<Integer> currentIds = user.getUserRoles().stream()
                                      .map(ur -> ur.getRole().getId())
                                      .collect(Collectors.toSet());
        if (resolvedIds.equals(currentIds)) {
            return;
        }
        validateRoleAssignments(user, request, resolved);
        user.getUserRoles().removeIf(ur -> !resolvedIds.contains(ur.getRole().getId()));
        for (Role role : resolved) {
            boolean alreadyLinked = user.getUserRoles().stream()
                                        .anyMatch(ur -> ur.getRole().getId().equals(role.getId()));
            if (!alreadyLinked) {
                user.getUserRoles().add(UserRole.builder()
                                                .id(new UserRoleId(user.getId(), role.getId()))
                                                .user(user)
                                                .role(role)
                                                .build());
            }
        }
        syncProfilesWithRoles(user, request, resolved);
    }

    /**
     * Profiles are tied to ROLE_STUDENT / ROLE_TEACHER, not to user_roles rows alone.
     * Drop profiles when the corresponding role is gone; create missing profiles when a role is added.
     */
    private void syncProfilesWithRoles(User user, UpdateUserByAdminRequest request, List<Role> resolved) {
        Set<String> roleNames = resolved.stream().map(Role::getName).collect(Collectors.toSet());
        boolean assigningStudent = roleNames.contains(RoleName.ROLE_STUDENT.name());
        boolean assigningTeacher = roleNames.contains(RoleName.ROLE_TEACHER.name());

        if (!assigningStudent) {
            user.setStudentProfile(null); // Благодаря orphanRemoval удалит профиль
        } else if (user.getStudentProfile() == null) {
            if (request.groupId() == null) {
                throw new MissedRequiredArgument(
                        "GROUP_REQUIRED",
                        "groupId is required when assigning ROLE_STUDENT without an existing student profile"
                );
            }
            var group = studentGroupRepository.findById(request.groupId())
                                              .orElseThrow(() -> new ObjectNotFoundException(
                                                      "GROUP_NOT_FOUND",
                                                      "Student group not found: " + request.groupId()
                                              ));
            user.setStudentProfile(StudentProfile.builder().user(user).group(group).build());
        }

        if (!assigningTeacher) {
            user.setTeacherProfile(null);
        } else if (user.getTeacherProfile() == null) {
            if (request.departmentId() == null) {
                throw new MissedRequiredArgument(
                        "DEPARTMENT_REQUIRED",
                        "departmentId is required when assigning ROLE_TEACHER without an existing teacher profile"
                );
            }
            var department = departmentRepository.findById(request.departmentId())
                                                 .orElseThrow(() -> new ObjectNotFoundException(
                                                         "DEPARTMENT_NOT_FOUND",
                                                         "Department not found: " + request.departmentId()
                                                 ));
            user.setTeacherProfile(TeacherProfile.builder().user(user).department(department).build());
        }
    }

    private List<Role> resolveAndDedupeRoles(Set<RoleName> requestedRoles) {
        Map<Integer, Role> byId = new LinkedHashMap<>();
        for (RoleName requestedRole : requestedRoles) {
            Role resolvedRole = resolveRole(requestedRole);
            byId.putIfAbsent(resolvedRole.getId(), resolvedRole);
        }
        return new ArrayList<>(byId.values());
    }

    private Role resolveRole(RoleName requestRole) {
        if (StringUtils.isNotBlank(requestRole.name())) {
            return roleRepository.findByName(requestRole.name())
                                 .orElseThrow(() -> new ObjectNotFoundException(
                                         "ROLE_NOT_FOUND",
                                         "Role not found: " + requestRole.name()
                                 ));
        }
        throw new MissedRequiredArgument("ROLE_REQUIRED", "Role name is required");
    }

    private void validateRoleAssignments(User user, UpdateUserByAdminRequest request, List<Role> resolved) {
        boolean assigningStudentRole = resolved.stream()
                                               .anyMatch(r -> RoleName.ROLE_STUDENT.name().equals(r.getName()));
        if (assigningStudentRole) {
            boolean hasGroup = request.groupId() != null
                    || (user.getStudentProfile() != null && user.getStudentProfile().getGroup() != null);
            if (!hasGroup) {
                throw new MissedRequiredArgument(
                        "GROUP_REQUIRED",
                        "groupId or existing student group is required when assigning ROLE_STUDENT"
                );
            }
        }
        boolean assigningTeacherRole = resolved.stream()
                                               .anyMatch(r -> RoleName.ROLE_TEACHER.name().equals(r.getName()));
        if (assigningTeacherRole) {
            boolean hasDepartment = request.departmentId() != null
                    || (user.getTeacherProfile() != null && user.getTeacherProfile().getDepartment() != null);
            if (!hasDepartment) {
                throw new MissedRequiredArgument(
                        "DEPARTMENT_REQUIRED",
                        "departmentId or existing teacher department is required when assigning ROLE_TEACHER"
                );
            }
        }
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findUserOrThrow(id);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUsername());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserSearchFilter filter, Pageable pageable) {
        Specification<User> spec = buildSpecification(filter);
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    private Specification<User> buildSpecification(UserSearchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.firstName() != null && !filter.firstName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + filter.firstName().toLowerCase() + "%"));
            }
            if (filter.lastName() != null && !filter.lastName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + filter.lastName().toLowerCase() + "%"));
            }
            if (filter.email() != null && !filter.email().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + filter.email().toLowerCase() + "%"));
            }
            if (filter.active() != null) {
                predicates.add(cb.equal(root.get("active"), filter.active()));
            }
            if (filter.role() != null && !filter.role().isBlank()) {
                Join<Object, Object> userRoles = root.join("userRoles", JoinType.INNER);
                Join<Object, Object> role = userRoles.join("role", JoinType.INNER);
                predicates.add(cb.equal(role.get("name"), filter.role()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Email → username for rows in the current import batch (bounded IN query).
     */
    @Transactional(readOnly = true)
    public Map<String, String> findUsernameByEmailIn(Collection<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllByEmailIn(emails).stream()
                             .collect(Collectors.toMap(
                                     UserRepository.EmailUsernameProjection::getEmail,
                                     UserRepository.EmailUsernameProjection::getUsername,
                                     (a, b) -> a
                             ));
    }

    /**
     * Creates new student users or updates existing ones by username (PostgreSQL upsert semantics; implemented via JPA).
     */
    @Transactional
    public int bulkUpsertStudents(List<StudentImportRow> rows) {
        if (rows.isEmpty()) return 0;
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT.name())
                                         .orElseThrow(() -> new ObjectNotFoundException(
                                                 "ROLE_NOT_FOUND",
                                                 "ROLE_STUDENT not found"
                                         ));
        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        List<String> usernames = rows.stream().map(StudentImportRow::username).toList();
        Map<String, User> userByUsername = userRepository.findAllByUsernameInWithStudentProfile(usernames)
                                                         .stream()
                                                         .collect(Collectors.toMap(
                                                                 User::getUsername,
                                                                 Function.identity(),
                                                                 (a, b) -> a
                                                         ));
        Set<UUID> userIdsWithStudentRole = userRoleRepository.findUserIdsWithStudentRoleByUsernameIn(usernames);
        for (StudentImportRow row : rows) {
            User existing = userByUsername.get(row.username());
            if (existing != null) {
                updateStudentFromImport(existing, userIdsWithStudentRole, row, studentRole, now, actor);
            } else {
                User created = createStudentFromImport(row, studentRole, now, actor);
                userByUsername.put(row.username(), created);
            }
        }
        return rows.size();
    }

    private User createStudentFromImport(StudentImportRow row, Role studentRole, LocalDateTime now, String actor) {
        String tempPassword = PasswordAndTokenGenerator.generate(PASSWORD_LENGTH);
        User user = User.builder()
                        .username(row.username())
                        .email(row.email())
                        .password(passwordEncoder.encode(tempPassword))
                        .firstName(row.firstName())
                        .lastName(row.lastName())
                        .middleName(row.middleName())
                        .active(row.active())
                        .createdAt(now)
                        .createdBy(actor)
                        .updatedAt(now)
                        .updatedBy(actor)
                        .build();
        User savedUser = userRepository.save(user);

        userRoleRepository.save(UserRole.builder()
                                        .id(new UserRoleId(savedUser.getId(), studentRole.getId()))
                                        .user(savedUser)
                                        .role(studentRole)
                                        .build());

        StudentGroup group = studentGroupRepository.getReferenceById(row.groupId());
        var studentProfileBuilder = StudentProfile.builder()
                                                  .user(savedUser)
                                                  .group(group);

        if (row.departmentId() != null) {
            var department = departmentRepository.getReferenceById(row.departmentId());
            studentProfileBuilder.department(department);
        }

        studentProfileRepository.save(studentProfileBuilder.build());

        eventPublisher.publishEvent(UserCreatedEvent.builder()
                                                    .userId(savedUser.getId())
                                                    .email(savedUser.getEmail())
                                                    .username(savedUser.getUsername())
                                                    .firstName(savedUser.getFirstName())
                                                    .lastName(savedUser.getLastName())
                                                    .temporaryPassword(tempPassword)
                                                    .build());
        return savedUser;
    }

    private void updateStudentFromImport(
            User user,
            Set<UUID> userIdsWithStudentRole,
            StudentImportRow row,
            Role studentRole,
            LocalDateTime now,
            String actor
    ) {
        user.setEmail(row.email());
        user.setFirstName(row.firstName());
        user.setLastName(row.lastName());
        user.setMiddleName(row.middleName());
        user.setActive(row.active());
        user.setUpdatedAt(now);
        user.setUpdatedBy(actor);
        userRepository.save(user);

        StudentGroup group = studentGroupRepository.getReferenceById(row.groupId());
        if (user.getStudentProfile() == null) {
            var studentProfileBuilder = StudentProfile.builder().user(user).group(group);
            if (row.departmentId() != null) {
                var department = departmentRepository.getReferenceById(row.departmentId());
                studentProfileBuilder.department(department);
            }
            studentProfileRepository.save(studentProfileBuilder.build());
        } else {
            StudentProfile sp = user.getStudentProfile();
            sp.setGroup(group);
            sp.setDepartment(row.departmentId() != null ? departmentRepository.getReferenceById(row.departmentId()) : null);
            studentProfileRepository.save(sp);
        }

        if (!userIdsWithStudentRole.contains(user.getId()) && !userRoleRepository.existsByUserIdAndRoleName(
                user.getId(),
                RoleName.ROLE_STUDENT.name()
        )) {
            userRoleRepository.save(UserRole.builder()
                                            .id(new UserRoleId(user.getId(), studentRole.getId()))
                                            .user(user)
                                            .role(studentRole)
                                            .build());
        }
    }

    /**
     * Creates new teacher users or updates existing ones by username (PostgreSQL upsert semantics; implemented via JPA).
     */
    @Transactional
    public int bulkUpsertTeachers(List<TeacherImportRow> rows) {
        if (rows.isEmpty()) return 0;
        Role teacherRole = roleRepository.findByName(RoleName.ROLE_TEACHER.name())
                                         .orElseThrow(() -> new ObjectNotFoundException(
                                                 "ROLE_NOT_FOUND",
                                                 "ROLE_TEACHER not found"
                                         ));
        LocalDateTime now = LocalDateTime.now();
        String actor = currentUsername();
        List<String> usernames = rows.stream().map(TeacherImportRow::username).toList();
        Map<String, User> userByUsername = userRepository.findAllByUsernameInWithTeacherProfile(usernames)
                                                         .stream()
                                                         .collect(Collectors.toMap(
                                                                 User::getUsername,
                                                                 Function.identity(),
                                                                 (a, b) -> a
                                                         ));
        Set<UUID> userIdsWithTeacherRole = userRoleRepository.findUserIdsWithTeacherRoleByUsernameIn(usernames);
        for (TeacherImportRow row : rows) {
            User existing = userByUsername.get(row.username());
            if (existing != null) {
                updateTeacherFromImport(existing, userIdsWithTeacherRole, row, teacherRole, now, actor);
            } else {
                User created = createTeacherFromImport(row, teacherRole, now, actor);
                userByUsername.put(row.username(), created);
            }
        }
        return rows.size();
    }

    private User createTeacherFromImport(TeacherImportRow row, Role teacherRole, LocalDateTime now, String actor) {
        String tempPassword = PasswordAndTokenGenerator.generate(PASSWORD_LENGTH);
        User user = User.builder()
                        .username(row.username())
                        .email(row.email())
                        .password(passwordEncoder.encode(tempPassword))
                        .firstName(row.firstName())
                        .lastName(row.lastName())
                        .middleName(row.middleName())
                        .active(row.active())
                        .createdAt(now)
                        .createdBy(actor)
                        .updatedAt(now)
                        .updatedBy(actor)
                        .build();
        User savedUser = userRepository.save(user);

        userRoleRepository.save(UserRole.builder()
                                        .id(new UserRoleId(savedUser.getId(), teacherRole.getId()))
                                        .user(savedUser)
                                        .role(teacherRole)
                                        .build());

        var department = departmentRepository.getReferenceById(row.departmentId());
        teacherProfileRepository.save(TeacherProfile.builder().user(savedUser).department(department).build());

        eventPublisher.publishEvent(UserCreatedEvent.builder()
                                                    .userId(savedUser.getId())
                                                    .email(savedUser.getEmail())
                                                    .username(savedUser.getUsername())
                                                    .firstName(savedUser.getFirstName())
                                                    .lastName(savedUser.getLastName())
                                                    .temporaryPassword(tempPassword)
                                                    .build());
        return savedUser;
    }

    private void updateTeacherFromImport(
            User user,
            Set<UUID> userIdsWithTeacherRole,
            TeacherImportRow row,
            Role teacherRole,
            LocalDateTime now,
            String actor
    ) {
        user.setEmail(row.email());
        user.setFirstName(row.firstName());
        user.setLastName(row.lastName());
        user.setMiddleName(row.middleName());
        user.setActive(row.active());
        user.setUpdatedAt(now);
        user.setUpdatedBy(actor);
        userRepository.save(user);

        if (user.getTeacherProfile() == null) {
            var department = departmentRepository.getReferenceById(row.departmentId());
            teacherProfileRepository.save(TeacherProfile.builder().user(user).department(department).build());
        } else {
            TeacherProfile tp = user.getTeacherProfile();
            tp.setDepartment(departmentRepository.getReferenceById(row.departmentId()));
            teacherProfileRepository.save(tp);
        }

        if (!userIdsWithTeacherRole.contains(user.getId()) && !userRoleRepository.existsByUserIdAndRoleName(
                user.getId(),
                RoleName.ROLE_TEACHER.name()
        )) {
            userRoleRepository.save(UserRole.builder()
                                            .id(new UserRoleId(user.getId(), teacherRole.getId()))
                                            .user(user)
                                            .role(teacherRole)
                                            .build());
        }
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
