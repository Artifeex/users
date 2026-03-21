package ru.sandr.users.hierarchy.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.sandr.users.hierarchy.dto.FieldOfStudyResponse;
import ru.sandr.users.hierarchy.entity.Faculty;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T19:14:16+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.4.jar, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class FieldOfStudyMapperImpl implements FieldOfStudyMapper {

    @Override
    public FieldOfStudyResponse toResponse(FieldOfStudy fieldOfStudy) {
        if ( fieldOfStudy == null ) {
            return null;
        }

        Long facultyId = null;
        Long id = null;
        String code = null;
        String name = null;

        facultyId = fieldOfStudyFacultyId( fieldOfStudy );
        id = fieldOfStudy.getId();
        code = fieldOfStudy.getCode();
        name = fieldOfStudy.getName();

        FieldOfStudyResponse fieldOfStudyResponse = new FieldOfStudyResponse( id, code, name, facultyId );

        return fieldOfStudyResponse;
    }

    private Long fieldOfStudyFacultyId(FieldOfStudy fieldOfStudy) {
        Faculty faculty = fieldOfStudy.getFaculty();
        if ( faculty == null ) {
            return null;
        }
        return faculty.getId();
    }
}
