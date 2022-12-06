package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.RoleGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleGroupService extends JpaRepository<RoleGroupEntity, Integer> {

}
