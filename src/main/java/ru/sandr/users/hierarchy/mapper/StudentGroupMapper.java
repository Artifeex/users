package ru.sandr.users.hierarchy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sandr.users.hierarchy.dto.StudentGroupResponse;
import ru.sandr.users.hierarchy.entity.StudentGroup;

@Mapper(componentModel = "spring")
public interface StudentGroupMapper {

    @Mapping(source = "faculty.id", target = "facultyId")
    @Mapping(source = "fieldOfStudy.id", target = "fieldOfStudyId")
    StudentGroupResponse toResponse(StudentGroup studentGroup);
}
