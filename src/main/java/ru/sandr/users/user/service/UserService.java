package ru.sandr.users.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import ru.sandr.users.core.exception.ConflictException;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.security.service.AuthenticationService;
import ru.sandr.users.user.dto.ChangeAvatarRequestDto;
import ru.sandr.users.user.dto.ChangePasswordRequest;
import ru.sandr.users.user.dto.UpdateOwnProfileRequest;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.entity.User;
import ru.sandr.users.user.events.FileLoadedEvent;
import ru.sandr.users.user.mapper.UserMapper;
import ru.sandr.users.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RestClient fileServiceClient;
    private final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    public void changeAvatar(@Valid ChangeAvatarRequestDto changeAvatarRequestDto, UUID userId) {
        var fileId = changeAvatarRequestDto.fileId();
        var responseFromFileService = fileServiceClient.get()
                                                       .uri("/api/v1/files/{fileId}", fileId)
                                                       .accept(MediaType.APPLICATION_JSON)
                                                       .retrieve()
                                                       .onStatus(
                                                               HttpStatusCode::is4xxClientError,
                                                               (request, response) -> {
                                                                   log.error(
                                                                           "Not found avatar with fileId {}",
                                                                           changeAvatarRequestDto.fileId()
                                                                   );
                                                                   throw new ObjectNotFoundException(
                                                                           "FILE_NOT_FOUND",
                                                                           "File for avatar not found: " + fileId
                                                                   );
                                                               }
                                                       )
                                                       .toBodilessEntity();
        if (responseFromFileService.getStatusCode().is2xxSuccessful()) {
            var currentUser = userRepository.findById(userId)
                                            .orElseThrow(() -> new ObjectNotFoundException(
                                                    "USER_NOT_FOUND",
                                                    "User with id " + userId + " not found"
                                            ));
            currentUser.setAvatarFileId(UUID.fromString(fileId));
            eventPublisher.publishEvent(new FileLoadedEvent(UUID.fromString(fileId)));
        }
    }
}
