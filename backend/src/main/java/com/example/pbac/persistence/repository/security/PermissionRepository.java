package com.example.pbac.persistence.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pbac.persistence.model.security.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

}
