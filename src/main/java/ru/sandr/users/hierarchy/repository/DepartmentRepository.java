package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.hierarchy.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByFaculty_Id(Long facultyId);
}

