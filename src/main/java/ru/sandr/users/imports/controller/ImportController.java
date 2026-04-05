package ru.sandr.users.imports.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sandr.users.imports.dto.ImportResultResponse;
import ru.sandr.users.imports.service.HierarchyImportService;
import ru.sandr.users.imports.service.UserImportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/import")
public class ImportController {

    private final HierarchyImportService hierarchyImportService;
    private final UserImportService userImportService;

    @PostMapping("/hierarchy/structure")
    public ImportResultResponse importStructure(@RequestParam MultipartFile file) {

    }

    @PostMapping("/hierarchy/departments")
    public ImportResultResponse importDepartments(@RequestParam MultipartFile file) {

    }

    @PostMapping("/users/students")
    public ImportResultResponse importStudents(@RequestParam MultipartFile file) {
        return userImportService.importStudents(file);
    }

    @PostMapping("/users/teachers")
    public ImportResultResponse importTeachers(@RequestParam MultipartFile file) {
        return userImportService.importTeachers(file);
    }
}
