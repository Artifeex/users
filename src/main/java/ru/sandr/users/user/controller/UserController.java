package ru.sandr.users.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.sandr.users.security.service.AuthenticationService;
import ru.sandr.users.user.dto.ChangePasswordRequest;
import ru.sandr.users.user.dto.UpdateOwnProfileRequest;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PatchMapping()
    public UserResponse changeOwnProfile(@Valid @RequestBody UpdateOwnProfileRequest request) {
        return userService.changeOwnProfile(request);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
    }
}
