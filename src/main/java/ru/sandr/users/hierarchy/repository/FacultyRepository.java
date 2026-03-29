package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.hierarchy.entity.Faculty;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByName(String name);

    @Query("SELECT f.name AS name, f.id AS id FROM Faculty f")
    List<NameIdProjection> findAllNameIdProjections();

    @Query("SELECT f.name AS name, f.id AS id FROM Faculty f WHERE f.name IN :names")
    List<NameIdProjection> findNameIdProjectionsByNames(@Param("names") Collection<String> names);

    interface NameIdProjection {
        String getName();
        Long getId();
    }
}
