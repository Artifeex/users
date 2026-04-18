package ru.sandr.users.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sandr.users.hierarchy.entity.Department;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;
import ru.sandr.users.hierarchy.entity.StudentGroup;
import ru.sandr.users.user.dto.AdminUserDetailsResponse;
import ru.sandr.users.user.dto.AdminUserSearchResponse;
import ru.sandr.users.user.entity.StudentProfile;
import ru.sandr.users.user.entity.TeacherProfile;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.entity.User;
import ru.sandr.users.user.entity.UserRole;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapRoleNames")
    UserResponse toResponse(User user);

    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapRoleNames")
    @Mapping(target = "department", source = "user", qualifiedByName = "mapDepartmentName")
    AdminUserSearchResponse toAdminSearchResponse(User user);

    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapRoleNamesAsList")
    @Mapping(target = "faculty", source = "user", qualifiedByName = "mapFacultyName")
    @Mapping(target = "fieldOfStudy", source = "user", qualifiedByName = "mapFieldOfStudyName")
    @Mapping(target = "studentGroup", source = "user", qualifiedByName = "mapStudentGroupName")
    @Mapping(target = "department", source = "user", qualifiedByName = "mapDepartmentName")
    AdminUserDetailsResponse toAdminDetailsResponse(User user);

    @Named("mapRoleNames")
    default Set<String> mapRoleNames(Set<UserRole> userRoles) {
        if (userRoles == null) return Set.of();
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }

    @Named("mapRoleNamesAsList")
    default List<String> mapRoleNamesAsList(Set<UserRole> userRoles) {
        if (userRoles == null) {
            return List.of();
        }
        return userRoles.stream()
                        .map(ur -> ur.getRole().getName())
                        .sorted(Comparator.naturalOrder())
                        .toList();
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

    @Named("mapFacultyName")
    default String mapFacultyName(User user) {
        if (user == null) {
            return null;
        }
        TeacherProfile teacherProfile = user.getTeacherProfile();
        if (teacherProfile != null) {
            Department department = teacherProfile.getDepartment();
            if (department != null) {
                Faculty faculty = department.getFaculty();
                if (faculty != null) {
                    return faculty.getName();
                }
            }
        }
        StudentProfile studentProfile = user.getStudentProfile();
        if (studentProfile != null) {
            Department department = studentProfile.getDepartment();
            if (department != null && department.getFaculty() != null) {
                return department.getFaculty().getName();
            }
            StudentGroup studentGroup = studentProfile.getGroup();
            if (studentGroup != null && studentGroup.getFaculty() != null) {
                return studentGroup.getFaculty().getName();
            }
        }
        return null;
    }

    @Named("mapFieldOfStudyName")
    default String mapFieldOfStudyName(User user) {
        if (user == null || user.getStudentProfile() == null || user.getStudentProfile().getGroup() == null) {
            return null;
        }
        FieldOfStudy fieldOfStudy = user.getStudentProfile().getGroup().getFieldOfStudy();
        return fieldOfStudy != null ? fieldOfStudy.getName() : null;
    }

    @Named("mapStudentGroupName")
    default String mapStudentGroupName(User user) {
        if (user == null || user.getStudentProfile() == null || user.getStudentProfile().getGroup() == null) {
            return null;
        }
        return user.getStudentProfile().getGroup().getName();
    }
}
