package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.hierarchy.entity.StudentGroup;

import java.util.Collection;
import java.util.List;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    boolean existsByFaculty_Id(Long facultyId);

    boolean existsByFieldOfStudy_Id(Long fieldOfStudyId);

    List<StudentGroup> findAllByNameIn(Collection<String> names);

    @Query("SELECT sg.name AS name, sg.id AS id FROM StudentGroup sg WHERE sg.name IN :names")
    List<NameIdProjection> findByNameIn(@Param("names") Collection<String> names);

    interface NameIdProjection {
        String getName();
        Long getId();
    }

}
