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

    @Query("SELECT sg.name AS name, sg.id AS id FROM StudentGroup sg")
    List<NameIdProjection> findAllNameIdProjections();

    @Query("SELECT sg.name AS name, sg.id AS id FROM StudentGroup sg WHERE sg.name IN :names")
    List<NameIdProjection> findByNameIn(@Param("names") Collection<String> names);

    @Query("SELECT sg.faculty.name AS facultyName, sg.fieldOfStudy.name AS fosName, sg.name AS groupName, sg.id AS id FROM StudentGroup sg")
    List<CompositeProjection> findAllCompositeProjections();

    interface NameIdProjection {
        String getName();
        Long getId();
    }

    interface CompositeProjection {
        String getFacultyName();
        String getFosName();
        String getGroupName();
        Long getId();
    }
}
