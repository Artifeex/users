package ru.sandr.users.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sandr.users.user.dto.AdminUserSearchResponse;
import ru.sandr.users.user.entity.StudentProfile;
import ru.sandr.users.user.entity.TeacherProfile;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.entity.User;
import ru.sandr.users.user.entity.UserRole;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapRoleNames")
    UserResponse toResponse(User user);

    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapRoleNames")
    @Mapping(target = "department", source = "user", qualifiedByName = "mapDepartmentName")
    AdminUserSearchResponse toAdminSearchResponse(User user);

    @Named("mapRoleNames")
    default Set<String> mapRoleNames(Set<UserRole> userRoles) {
        if (userRoles == null) return Set.of();
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }

    @Named("mapDepartmentName")
    default String mapDepartmentName(User user) {
        if (user == null) {
            return null;
        }
        TeacherProfile teacherProfile = user.getTeacherProfile();
        if (teacherProfile != null && teacherProfile.getDepartment() != null) {
            return teacherProfile.getDepartment().getName();
        }
        StudentProfile studentProfile = user.getStudentProfile();
        if (studentProfile != null && studentProfile.getDepartment() != null) {
            return studentProfile.getDepartment().getName();
        }
        return null;
    }
}
