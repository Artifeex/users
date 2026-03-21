package ru.sandr.users.hierarchy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sandr.users.hierarchy.dto.FieldOfStudyResponse;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;

@Mapper(componentModel = "spring")
public interface FieldOfStudyMapper {

    @Mapping(source = "faculty.id", target = "facultyId")
    FieldOfStudyResponse toResponse(FieldOfStudy fieldOfStudy);
}
