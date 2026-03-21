package ru.sandr.users.hierarchy.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;
import ru.sandr.users.hierarchy.entity.StudentGroup;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T19:14:16+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.4.jar, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class StudentGroupMapperImpl implements StudentGroupMapper {

    @Override
    public StudentGroupResponse toResponse(StudentGroup studentGroup) {
        if ( studentGroup == null ) {
            return null;
        }

        Long facultyId = null;
        Long fieldOfStudyId = null;
        Long id = null;
        String name = null;

        facultyId = studentGroupFacultyId( studentGroup );
        fieldOfStudyId = studentGroupFieldOfStudyId( studentGroup );
        id = studentGroup.getId();
        name = studentGroup.getName();

        StudentGroupResponse studentGroupResponse = new StudentGroupResponse( id, name, facultyId, fieldOfStudyId );

        return studentGroupResponse;
    }

    private Long studentGroupFacultyId(StudentGroup studentGroup) {
        Faculty faculty = studentGroup.getFaculty();
        if ( faculty == null ) {
            return null;
        }
        return faculty.getId();
    }

    private Long studentGroupFieldOfStudyId(StudentGroup studentGroup) {
        FieldOfStudy fieldOfStudy = studentGroup.getFieldOfStudy();
        if ( fieldOfStudy == null ) {
            return null;
        }
        return fieldOfStudy.getId();
    }
}
