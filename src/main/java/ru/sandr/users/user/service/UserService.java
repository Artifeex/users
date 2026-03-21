package ru.sandr.users.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.exception.ConflictException;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.security.service.AuthenticationService;
import ru.sandr.users.user.dto.ChangePasswordRequest;
import ru.sandr.users.user.dto.UpdateOwnProfileRequest;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.entity.User;
import ru.sandr.users.user.mapper.UserMapper;
import ru.sandr.users.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Optional<User> getUserByUsernameOrEmail(String loginInput) {
        return userRepository.findByUsernameOrEmail(loginInput, loginInput);
    }

    @Transactional
    public UserResponse changeOwnProfile(UpdateOwnProfileRequest request) {
        User current = getCurrentUser();
        String newEmail = StringUtils.trim(request.email());
        if (Objects.equals(newEmail, current.getEmail())) {
            return userMapper.toResponse(current);
        }
        if (userRepository.existsByEmail(newEmail)) {
            throw new ConflictException("EMAIL_TAKEN", "Email already in use: " + newEmail);
        }
        LocalDateTime now = LocalDateTime.now();
        String actor = current.getUsername();
        current.setEmail(newEmail);
        current.setUpdatedAt(now);
        current.setUpdatedBy(actor);
        return userMapper.toResponse(userRepository.save(current));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object username = auth.getPrincipal();
        return userRepository.findByUsernameOrEmail(username.toString(), username.toString())
                             .orElseThrow(() -> new ObjectNotFoundException(
                                     "USER_NOT_FOUND",
                                     "User not found: " + username
                             ));
    }
}
