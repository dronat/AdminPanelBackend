package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesService extends JpaRepository<RolesEntity, Integer> {

}
