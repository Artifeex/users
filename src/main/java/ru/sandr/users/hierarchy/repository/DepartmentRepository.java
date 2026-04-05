package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.hierarchy.entity.Department;

import java.util.Collection;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByFaculty_Id(Long facultyId);

    @Query("SELECT d.name as name, d.id as id FROM Department d where d.name IN :departmentNames")
    List<DepartmentProjection> findByDepartmentNameIn(@Param("departmentNames") Collection<String> departmentNames);

    interface NameIdProjection {
        String getName();

        Long getId();
    }

    interface DepartmentProjection {
        String getName();
        Long getId();
    }
}
