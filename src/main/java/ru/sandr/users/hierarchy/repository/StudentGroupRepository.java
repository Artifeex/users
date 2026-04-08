package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sandr.users.hierarchy.entity.StudentGroup;

import java.util.Collection;
import java.util.List;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    boolean existsByFaculty_Id(Long facultyId);

    boolean existsByFieldOfStudy_Id(Long fieldOfStudyId);

    List<StudentGroup> findAllByNameIn(Collection<String> names);

    Page<StudentGroup> findAllByFieldOfStudy_Id(Long fieldOfStudyId, Pageable pageable);

    @Query("SELECT sg.name AS name, sg.id AS id FROM StudentGroup sg WHERE sg.name IN :names")
    List<NameIdProjection> findByNameIn(@Param("names") Collection<String> names);

    @Query("""
            SELECT sg
            FROM StudentGroup sg
            WHERE (
                (:applyGroup = true AND sg.id IN :groupIds)
                OR (:applyField = true AND sg.fieldOfStudy.id IN :fieldIds)
                OR (:applyFaculty = true AND sg.faculty.id IN :facultyIds)
            )
            AND (:query IS NULL OR LOWER(sg.name) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<StudentGroup> searchAccessibleForTeacher(
            @Param("groupIds") Collection<Long> groupIds,
            @Param("fieldIds") Collection<Long> fieldIds,
            @Param("facultyIds") Collection<Long> facultyIds,
            @Param("applyGroup") boolean applyGroup,
            @Param("applyField") boolean applyField,
            @Param("applyFaculty") boolean applyFaculty,
            @Param("query") String query,
            Pageable pageable
    );

    interface NameIdProjection {
        String getName();
        Long getId();
    }

}
