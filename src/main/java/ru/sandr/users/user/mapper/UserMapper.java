package ru.sandr.users.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.entity.User;
import ru.sandr.users.user.entity.UserRole;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapRoleNames")
    UserResponse toResponse(User user);

    @Named("mapRoleNames")
    default Set<String> mapRoleNames(Set<UserRole> userRoles) {
        if (userRoles == null) return Set.of();
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }
}
