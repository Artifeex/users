package ru.sandr.users.user.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.sandr.users.core.dto.ApiErrorResponse;
import ru.sandr.users.security.service.AuthenticationService;
import ru.sandr.users.user.dto.ChangeAvatarRequestDto;
import ru.sandr.users.user.dto.ChangePasswordRequest;
import ru.sandr.users.user.dto.UpdateOwnProfileRequest;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PatchMapping("/email")
    @Operation(summary = "Update current user profile", description = "Updates profile data of authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserResponse changeOwnProfile(@Valid @RequestBody UpdateOwnProfileRequest request) {
        return userService.changeOwnProfile(request);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change current user password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
    }

    @PatchMapping("/avatar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Change current user avatar", description = "Updates avatar link for authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Avatar update accepted"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void changeAvatar(
            @Valid @RequestBody ChangeAvatarRequestDto request,
            @AuthenticationPrincipal String userId
    ) {
        userService.changeAvatar(request, UUID.fromString(userId));
    }


}
