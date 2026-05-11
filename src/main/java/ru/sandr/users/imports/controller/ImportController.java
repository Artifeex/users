package ru.sandr.users.imports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sandr.users.core.dto.ApiErrorResponse;
import ru.sandr.users.imports.dto.ImportResultResponse;
import ru.sandr.users.imports.service.HierarchyImportService;
import ru.sandr.users.imports.service.UserImportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/import")
@Tag(name = "Imports")
@SecurityRequirement(name = "bearerAuth")
public class ImportController {

    private final HierarchyImportService hierarchyImportService;
    private final UserImportService userImportService;

    @PostMapping("/hierarchy/structure")
    @Operation(summary = "Import full hierarchy structure from Excel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import processed"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation errors",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ImportResultResponse importStructure(@RequestParam MultipartFile file) {
        return hierarchyImportService.importStructure(file);
    }

    @PostMapping("/hierarchy/departments")
    @Operation(summary = "Import departments from Excel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import processed"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation errors",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ImportResultResponse importDepartments(@RequestParam MultipartFile file) {
        return hierarchyImportService.importDepartments(file);
    }

    @PostMapping("/users/students")
    @Operation(summary = "Import students from Excel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import processed"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation errors",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ImportResultResponse importStudents(@RequestParam MultipartFile file) {
        return userImportService.importStudents(file);
    }

    @PostMapping("/users/teachers")
    @Operation(summary = "Import teachers from Excel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import processed"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation errors",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ImportResultResponse importTeachers(@RequestParam MultipartFile file) {
        return userImportService.importTeachers(file);
    }
}
