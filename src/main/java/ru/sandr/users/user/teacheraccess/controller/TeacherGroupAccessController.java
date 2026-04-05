package ru.sandr.users.user.teacheraccess.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sandr.users.user.teacheraccess.dto.ReplaceTeacherGroupAccessRequest;
import ru.sandr.users.user.teacheraccess.dto.TeacherGroupAccessResponse;
import ru.sandr.users.user.teacheraccess.service.TeacherGroupAccessService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/teachers")
public class TeacherGroupAccessController {

    private final TeacherGroupAccessService teacherGroupAccessService;

    @GetMapping("/{teacherId}/group-access")
    public TeacherGroupAccessResponse getTeacherGroupAccess(@PathVariable UUID teacherId) {
        return teacherGroupAccessService.getTeacherScopes(teacherId);
    }

    @PutMapping("/{teacherId}/group-access")
    public TeacherGroupAccessResponse replaceTeacherGroupAccess(@PathVariable UUID teacherId,
                                                                @Valid @RequestBody ReplaceTeacherGroupAccessRequest request) {
        return teacherGroupAccessService.replaceTeacherScopes(teacherId, request);
    }
}
