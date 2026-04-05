package ru.sandr.users.hierarchy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.service.TeacherStudentGroupSearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teachers/me/student-groups")
public class TeacherStudentGroupController {

    private final TeacherStudentGroupSearchService teacherStudentGroupSearchService;

    @GetMapping
    public Page<StudentGroupResponse> searchAccessible(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return teacherStudentGroupSearchService.searchAccessibleStudentGroups(query, pageable);
    }
}
