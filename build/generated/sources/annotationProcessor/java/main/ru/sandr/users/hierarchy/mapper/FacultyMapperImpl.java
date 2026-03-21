package ru.sandr.users.hierarchy.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.sandr.users.hierarchy.dto.FacultyResponse;
import ru.sandr.users.hierarchy.entity.Faculty;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T19:14:16+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.4.jar, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class FacultyMapperImpl implements FacultyMapper {

    @Override
    public FacultyResponse toResponse(Faculty faculty) {
        if ( faculty == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String shortName = null;

        id = faculty.getId();
        name = faculty.getName();
        shortName = faculty.getShortName();

        FacultyResponse facultyResponse = new FacultyResponse( id, name, shortName );

        return facultyResponse;
    }
}
