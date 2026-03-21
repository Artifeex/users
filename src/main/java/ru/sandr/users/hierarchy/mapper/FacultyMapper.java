package ru.sandr.users.hierarchy.mapper;

import org.mapstruct.Mapper;
import ru.sandr.users.hierarchy.dto.FacultyResponse;
import ru.sandr.users.hierarchy.entity.Faculty;

@Mapper(componentModel = "spring")
public interface FacultyMapper {

    FacultyResponse toResponse(Faculty faculty);
}
