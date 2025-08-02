package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Role;
import com.project.DuAnTotNghiep.entity.enumClass.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}