package ru.sandr.users.teacheraccess.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.sandr.users.core.dto.PageResponse;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessScopeRequest;
import ru.sandr.users.teacheraccess.dto.TeacherGroupAccessScopeResponse;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;
import ru.sandr.users.teacheraccess.service.TeacherGroupAccessService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/teachers")
public class TeacherGroupAccessController {

    private final TeacherGroupAccessService teacherGroupAccessService;

    @PostMapping("/{teacherId}/group-access")
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherGroupAccessScopeResponse addTeacherGroupAccess(@PathVariable UUID teacherId,
                                                                 @Valid @RequestBody TeacherGroupAccessScopeRequest request) {
        return teacherGroupAccessService.addTeacherScope(teacherId, request);
    }

    @GetMapping("/{teacherId}/group-access/by-type/{scopeType}")
    public PageResponse<TeacherGroupAccessScopeResponse> findTeacherGroupAccessByType(
            @PathVariable UUID teacherId,
            @PathVariable TeacherGroupAccessScopeType scopeType,
            @PageableDefault(size = 20, page = 0, sort = "scopeId") Pageable pageable
    ) {
        return teacherGroupAccessService.findTeacherScopesByType(teacherId, scopeType, pageable);
    }
}
