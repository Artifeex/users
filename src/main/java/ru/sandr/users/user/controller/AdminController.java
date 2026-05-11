package ru.sandr.users.user.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;
import ru.sandr.users.core.dto.ApiErrorResponse;
import ru.sandr.users.core.dto.PageResponse;
import ru.sandr.users.user.dto.AdminUserDetailsResponse;
import ru.sandr.users.user.dto.AdminUserSearchResponse;
import ru.sandr.users.user.dto.CreateUserRequest;
import ru.sandr.users.user.dto.UpdateUserByAdminRequest;
import ru.sandr.users.user.dto.UserResponse;
import ru.sandr.users.user.dto.UserSearchFilter;
import ru.sandr.users.user.service.AdminUserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@Tag(name = "Admin Users")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminUserService adminUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user", description = "Creates admin, teacher or student user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g. duplicate email)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserResponse updateUser(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateUserByAdminRequest request) {
        return adminUserService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public AdminUserDetailsResponse getUserDetails(@PathVariable UUID id) {
        return adminUserService.getUserDetails(id);
    }

    @GetMapping
    @Operation(summary = "Search users", description = "Supports filters and pagination.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated users list")
    })
    public PageResponse<AdminUserSearchResponse> searchUsers(@ModelAttribute UserSearchFilter filter,
                                                             @ParameterObject @PageableDefault(size = 20, page = 0, sort = "lastName") Pageable pageable) {
        return adminUserService.searchUsers(filter, pageable);
    }


}
