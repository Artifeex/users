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
import ru.sandr.users.hierarchy.dto.CreateFacultyRequest;
import ru.sandr.users.hierarchy.dto.FacultyResponse;
import ru.sandr.users.hierarchy.dto.UpdateFacultyRequest;
import ru.sandr.users.hierarchy.service.FacultyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hierarchy/faculties")
@Tag(name = "Hierarchy")
@SecurityRequirement(name = "bearerAuth")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create faculty")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Faculty created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public FacultyResponse create(@Valid @RequestBody CreateFacultyRequest request) {
        return facultyService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get faculty by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faculty found"),
            @ApiResponse(responseCode = "404", description = "Faculty not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public FacultyResponse getById(@PathVariable Long id) {
        return facultyService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List faculties", description = "Returns paginated faculties.")
    @ApiResponse(responseCode = "200", description = "Paginated faculties list")
    public PageResponse<FacultyResponse> findAll(@ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable) {
        return facultyService.findAll(pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update faculty by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faculty updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Faculty not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public FacultyResponse update(@PathVariable Long id, @Valid @RequestBody UpdateFacultyRequest request) {
        return facultyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete faculty by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Faculty deleted"),
            @ApiResponse(responseCode = "404", description = "Faculty not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        facultyService.delete(id);
    }
}
