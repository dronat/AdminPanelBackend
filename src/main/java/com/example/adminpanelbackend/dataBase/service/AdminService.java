package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminService extends JpaRepository<AdminEntity, Integer> {
}
