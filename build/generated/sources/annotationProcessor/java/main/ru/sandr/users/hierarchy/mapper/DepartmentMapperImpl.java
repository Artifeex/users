package ru.sandr.users.hierarchy.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.sandr.users.hierarchy.dto.DepartmentResponse;
import ru.sandr.users.hierarchy.entity.Department;
import ru.sandr.users.hierarchy.entity.Faculty;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T19:14:16+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.4.jar, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class DepartmentMapperImpl implements DepartmentMapper {

    @Override
    public DepartmentResponse toResponse(Department department) {
        if ( department == null ) {
            return null;
        }

        Long facultyId = null;
        Long id = null;
        String name = null;

        facultyId = departmentFacultyId( department );
        id = department.getId();
        name = department.getName();

        DepartmentResponse departmentResponse = new DepartmentResponse( id, name, facultyId );

        return departmentResponse;
    }

    private Long departmentFacultyId(Department department) {
        Faculty faculty = department.getFaculty();
        if ( faculty == null ) {
            return null;
        }
        return faculty.getId();
    }
}
