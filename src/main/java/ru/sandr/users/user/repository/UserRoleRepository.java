package ru.sandr.users.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.user.entity.UserRole;
import ru.sandr.users.user.entity.UserRoleId;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.name = :roleName")
    boolean existsByUserIdAndRoleName(@Param("userId") UUID userId, @Param("roleName") String roleName);

    @Query("""
            SELECT ur.user.id
            FROM UserRole ur
            WHERE ur.role.name = 'ROLE_STUDENT' and ur.user.username IN :usernames
            """)
    Set<UUID> findUserIdsWithStudentRoleByUsernameIn(@Param("usernames") Collection<String> usernames);
}

