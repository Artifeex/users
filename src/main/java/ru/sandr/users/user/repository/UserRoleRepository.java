package ru.sandr.users.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.user.entity.UserRole;
import ru.sandr.users.user.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}

