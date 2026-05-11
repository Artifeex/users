package ru.sandr.users.hierarchy.controller;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sandr.users.core.dto.PageResponse;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.service.TeacherStudentGroupSearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teachers/me/student-groups")
@Tag(name = "Teacher Groups")
@SecurityRequirement(name = "bearerAuth")
public class TeacherStudentGroupController {

    private final TeacherStudentGroupSearchService teacherStudentGroupSearchService;

    @GetMapping
    @Operation(
            summary = "Search accessible student groups",
            description = "Returns only groups available for authenticated teacher. Optional query performs text search."
    )
    @ApiResponse(responseCode = "200", description = "Paginated accessible groups")
    public PageResponse<StudentGroupResponse> findAll(
            @Parameter(description = "Optional search term by group name/code")
            @RequestParam(required = false) String query,
            @ParameterObject @PageableDefault(size = 20, page = 0, sort = "name") Pageable pageable
    ) {
        return new PageResponse<>(teacherStudentGroupSearchService.searchAccessibleStudentGroups(query, pageable));
    }
}
