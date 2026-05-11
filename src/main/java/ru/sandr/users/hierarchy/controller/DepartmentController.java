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
import ru.sandr.users.hierarchy.dto.CreateDepartmentRequest;
import ru.sandr.users.hierarchy.dto.DepartmentResponse;
import ru.sandr.users.hierarchy.dto.UpdateDepartmentRequest;
import ru.sandr.users.hierarchy.service.DepartmentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hierarchy/departments")
@Tag(name = "Hierarchy")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create department")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public DepartmentResponse create(@Valid @RequestBody CreateDepartmentRequest request) {
        return departmentService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department found"),
            @ApiResponse(responseCode = "404", description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public DepartmentResponse getById(@PathVariable Long id) {
        return departmentService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List departments", description = "Returns paginated departments.")
    @ApiResponse(responseCode = "200", description = "Paginated departments list")
    public PageResponse<DepartmentResponse> findAll(@ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable) {
        return departmentService.findAll(pageable);
    }

    @GetMapping("/by-faculty/{facultyId}")
    @Operation(summary = "List departments by faculty id")
    @ApiResponse(responseCode = "200", description = "Paginated departments list")
    public PageResponse<DepartmentResponse> findAllByFacultyId(
            @PathVariable Long facultyId,
            @ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable
    ) {
        return departmentService.findAllByFacultyId(facultyId, pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update department by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody UpdateDepartmentRequest request) {
        return departmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete department by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Department deleted"),
            @ApiResponse(responseCode = "404", description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }
}
