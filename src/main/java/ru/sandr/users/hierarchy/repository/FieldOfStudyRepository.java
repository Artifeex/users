package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;

import java.util.Collection;
import java.util.List;

public interface FieldOfStudyRepository extends JpaRepository<FieldOfStudy, Long> {

    boolean existsByFaculty_Id(Long facultyId);

    /**
     * Returns (facultyName, fosName, id) tuples — no entity objects, no session tracking.
     * Used to build the composite-key dedup map before import Pass 2.
     */
    @Query("SELECT f.faculty.name AS facultyName, f.name AS fosName, f.id AS id FROM FieldOfStudy f")
    List<CompositeProjection> findAllCompositeProjections();

    @Query("SELECT f.name AS name, f.id AS id FROM FieldOfStudy f WHERE f.faculty.id = :facultyId AND f.name IN :names")
    List<FosNameIdProjection> findNameIdProjectionsByFacultyIdAndNames(
            @Param("facultyId") Long facultyId,
            @Param("names") Collection<String> names
    );

    interface CompositeProjection {
        String getFacultyName();
        String getFosName();
        Long getId();
    }

    interface FosNameIdProjection {
        String getName();
        Long getId();
    }
}
