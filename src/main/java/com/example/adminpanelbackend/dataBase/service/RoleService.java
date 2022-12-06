package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleService extends JpaRepository<RoleEntity, Integer> {

}
