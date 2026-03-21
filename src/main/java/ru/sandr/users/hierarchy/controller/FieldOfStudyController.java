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
import ru.sandr.users.hierarchy.dto.CreateFieldOfStudyRequest;
import ru.sandr.users.hierarchy.dto.FieldOfStudyResponse;
import ru.sandr.users.hierarchy.dto.UpdateFieldOfStudyRequest;
import ru.sandr.users.hierarchy.service.FieldOfStudyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hierarchy/fields-of-study")
public class FieldOfStudyController {

    private final FieldOfStudyService fieldOfStudyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FieldOfStudyResponse create(@Valid @RequestBody CreateFieldOfStudyRequest request) {
        return fieldOfStudyService.create(request);
    }

    @GetMapping("/{id}")
    public FieldOfStudyResponse getById(@PathVariable Long id) {
        return fieldOfStudyService.getById(id);
    }

    @GetMapping
    public Page<FieldOfStudyResponse> findAll(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return fieldOfStudyService.findAll(pageable);
    }

    @PatchMapping("/{id}")
    public FieldOfStudyResponse update(@PathVariable Long id, @Valid @RequestBody UpdateFieldOfStudyRequest request) {
        return fieldOfStudyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        fieldOfStudyService.delete(id);
    }
}
