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
import ru.sandr.users.hierarchy.dto.CreateFieldOfStudyRequest;
import ru.sandr.users.hierarchy.dto.FieldOfStudyResponse;
import ru.sandr.users.hierarchy.dto.UpdateFieldOfStudyRequest;
import ru.sandr.users.hierarchy.service.FieldOfStudyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hierarchy/fields-of-study")
@Tag(name = "Hierarchy")
@SecurityRequirement(name = "bearerAuth")
public class FieldOfStudyController {

    private final FieldOfStudyService fieldOfStudyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create field of study")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Field of study created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public FieldOfStudyResponse create(@Valid @RequestBody CreateFieldOfStudyRequest request) {
        return fieldOfStudyService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get field of study by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Field of study found"),
            @ApiResponse(responseCode = "404", description = "Field of study not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public FieldOfStudyResponse getById(@PathVariable Long id) {
        return fieldOfStudyService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List all fields of study", description = "Returns paginated list across all faculties.")
    @ApiResponse(responseCode = "200", description = "Paginated fields of study list")
    public PageResponse<FieldOfStudyResponse> findAll(@ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable) {
        return fieldOfStudyService.findAll(pageable);
    }

    @GetMapping("/by-faculty/{facultyId}")
    @Operation(summary = "List fields of study by faculty")
    @ApiResponse(responseCode = "200", description = "Paginated fields of study list")
    public PageResponse<FieldOfStudyResponse> findAllByFacultyId(
            @PathVariable Long facultyId,
            @ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable
    ) {
        return fieldOfStudyService.findAllByFacultyId(facultyId, pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update field of study by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Field of study updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Field of study not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public FieldOfStudyResponse update(@PathVariable Long id, @Valid @RequestBody UpdateFieldOfStudyRequest request) {
        return fieldOfStudyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete field of study by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Field of study deleted"),
            @ApiResponse(responseCode = "404", description = "Field of study not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        fieldOfStudyService.delete(id);
    }
}
