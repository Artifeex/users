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
import ru.sandr.users.hierarchy.dto.CreateFacultyRequest;
import ru.sandr.users.hierarchy.dto.FacultyResponse;
import ru.sandr.users.hierarchy.dto.UpdateFacultyRequest;
import ru.sandr.users.hierarchy.service.FacultyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hierarchy/faculties")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacultyResponse create(@Valid @RequestBody CreateFacultyRequest request) {
        return facultyService.create(request);
    }

    @GetMapping("/{id}")
    public FacultyResponse getById(@PathVariable Long id) {
        return facultyService.getById(id);
    }

    @GetMapping
    public Page<FacultyResponse> findAll(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return facultyService.findAll(pageable);
    }

    @PatchMapping("/{id}")
    public FacultyResponse update(@PathVariable Long id, @Valid @RequestBody UpdateFacultyRequest request) {
        return facultyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        facultyService.delete(id);
    }
}
