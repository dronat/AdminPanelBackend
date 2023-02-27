package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.RoleGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleGroupService extends JpaRepository<RoleGroupEntity, Integer> {

}
