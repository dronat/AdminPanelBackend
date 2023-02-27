package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminService extends JpaRepository<AdminEntity, Long> {
}
