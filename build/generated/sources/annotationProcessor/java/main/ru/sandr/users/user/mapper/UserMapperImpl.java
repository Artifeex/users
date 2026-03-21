package ru.sandr.users.user.mapper;

import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.entity.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T19:14:16+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.4.jar, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        Set<String> roles = null;
        UUID id = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        String middleName = null;
        boolean active = false;

        roles = mapRoleNames( user.getUserRoles() );
        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        middleName = user.getMiddleName();
        active = user.isActive();

        UserResponse userResponse = new UserResponse( id, username, email, firstName, lastName, middleName, active, roles );

        return userResponse;
    }
}
