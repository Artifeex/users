package ru.sandr.users.teacheraccess.controller;

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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.sandr.users.core.dto.ApiErrorResponse;
import ru.sandr.users.core.dto.PageResponse;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessScopeRequest;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessScopeResponse;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;
import ru.sandr.users.teacheraccess.service.TeacherGroupAccessService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/teachers")
@Tag(name = "Teacher Access")
@SecurityRequirement(name = "bearerAuth")
public class TeacherGroupAccessController {

    private final TeacherGroupAccessService teacherGroupAccessService;

    @PostMapping("/{teacherId}/group-access")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Grant teacher group access scope")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Scope created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Teacher or target node not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public TeacherGroupAccessScopeResponse addTeacherGroupAccess(@PathVariable UUID teacherId,
                                                                 @Valid @RequestBody TeacherGroupAccessScopeRequest request) {
        return teacherGroupAccessService.addTeacherScope(teacherId, request);
    }

    @GetMapping("/{teacherId}/group-access/by-type/{scopeType}")
    @Operation(summary = "List teacher scopes by scope type")
    @ApiResponse(responseCode = "200", description = "Paginated scopes list")
    public PageResponse<TeacherGroupAccessScopeResponse> findTeacherGroupAccessByType(
            @Parameter(description = "Teacher user id")
            @PathVariable UUID teacherId,
            @Parameter(description = "Scope type", example = "FACULTY")
            @PathVariable TeacherGroupAccessScopeType scopeType,
            @ParameterObject @PageableDefault(size = 20, page = 0, sort = "scopeId") Pageable pageable
    ) {
        return teacherGroupAccessService.findTeacherScopesByType(teacherId, scopeType, pageable);
    }
}
