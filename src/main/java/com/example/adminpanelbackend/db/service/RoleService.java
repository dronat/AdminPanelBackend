package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleService extends JpaRepository<RoleEntity, Integer> {

}
