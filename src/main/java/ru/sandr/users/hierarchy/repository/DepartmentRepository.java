package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sandr.users.hierarchy.entity.Department;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByFaculty_Id(Long facultyId);

    @Query("SELECT d.name AS name, d.id AS id FROM Department d")
    List<NameIdProjection> findAllNameIdProjections();

    @Query("SELECT d.faculty.name AS facultyName, d.name AS deptName, d.id AS id FROM Department d")
    List<CompositeProjection> findAllCompositeProjections();

    interface NameIdProjection {
        String getName();
        Long getId();
    }

    interface CompositeProjection {
        String getFacultyName();
        String getDeptName();
        Long getId();
    }
}
