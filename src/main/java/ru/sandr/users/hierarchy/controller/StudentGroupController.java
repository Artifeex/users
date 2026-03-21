package ru.sandr.users.hierarchy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import ru.sandr.users.hierarchy.dto.CreateStudentGroupRequest;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.dto.UpdateStudentGroupRequest;
import ru.sandr.users.hierarchy.service.StudentGroupService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hierarchy/student-groups")
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentGroupResponse create(@Valid @RequestBody CreateStudentGroupRequest request) {
        return studentGroupService.create(request);
    }

    @GetMapping("/{id}")
    public StudentGroupResponse getById(@PathVariable Long id) {
        return studentGroupService.getById(id);
    }

    @GetMapping
    public Page<StudentGroupResponse> findAll(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return studentGroupService.findAll(pageable);
    }

    @PatchMapping("/{id}")
    public StudentGroupResponse update(@PathVariable Long id, @Valid @RequestBody UpdateStudentGroupRequest request) {
        return studentGroupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        studentGroupService.delete(id);
    }
}
