package ru.sandr.users.hierarchy.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
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
import ru.sandr.users.hierarchy.dto.CreateStudentGroupRequest;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.dto.UpdateStudentGroupRequest;
import ru.sandr.users.hierarchy.service.StudentGroupService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hierarchy/student-groups")
@Tag(name = "Hierarchy")
@SecurityRequirement(name = "bearerAuth")
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create student group")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Student group created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public StudentGroupResponse create(@Valid @RequestBody CreateStudentGroupRequest request) {
        return studentGroupService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student group by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student group found"),
            @ApiResponse(responseCode = "404", description = "Student group not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public StudentGroupResponse getById(@PathVariable Long id) {
        return studentGroupService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List student groups", description = "Returns paginated student groups.")
    @ApiResponse(responseCode = "200", description = "Paginated student groups list")
    public PageResponse<StudentGroupResponse> findAll(@ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable) {
        return studentGroupService.findAll(pageable);
    }

    @GetMapping("/by-field-of-study/{fieldOfStudyId}")
    @Operation(summary = "List student groups by field of study id")
    @ApiResponse(responseCode = "200", description = "Paginated student groups list")
    public PageResponse<StudentGroupResponse> findAllByFieldOfStudyId(
            @PathVariable Long fieldOfStudyId,
            @ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable
    ) {
        return studentGroupService.findAllByFieldOfStudyId(fieldOfStudyId, pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update student group by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student group updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Student group not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public StudentGroupResponse update(@PathVariable Long id, @Valid @RequestBody UpdateStudentGroupRequest request) {
        return studentGroupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete student group by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Student group deleted"),
            @ApiResponse(responseCode = "404", description = "Student group not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        studentGroupService.delete(id);
    }
}
