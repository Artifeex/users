package ru.sandr.users.hierarchy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sandr.users.hierarchy.dto.DepartmentResponse;
import ru.sandr.users.hierarchy.entity.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "faculty.id", target = "facultyId")
    DepartmentResponse toResponse(Department department);
}
