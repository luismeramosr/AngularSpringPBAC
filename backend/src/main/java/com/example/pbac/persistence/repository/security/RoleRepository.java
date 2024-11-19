package com.example.pbac.persistence.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pbac.persistence.model.security.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

}
