package ru.sandr.users.imports.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.hierarchy.entity.Department;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;
import ru.sandr.users.hierarchy.entity.StudentGroup;
import ru.sandr.users.hierarchy.service.DepartmentService;
import ru.sandr.users.hierarchy.service.FieldOfStudyService;
import ru.sandr.users.hierarchy.service.FacultyService;
import ru.sandr.users.hierarchy.service.StudentGroupService;
import ru.sandr.users.imports.dto.ImportResultResponse;
import ru.sandr.users.imports.dto.ImportRowError;
import ru.sandr.users.imports.exception.ImportValidationException;
import ru.sandr.users.imports.parser.ExcelStreamingParser;
import ru.sandr.users.imports.parser.ParsedRow;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyImportService {

    private static String cell(String[] cells, int index) {
        return (cells.length > index && cells[index] != null) ? cells[index] : "";
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "import";
    }
}
