package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.AdminActionLogEntity;
import com.example.adminpanelbackend.dataBase.entity.PlayerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminActionLogsService extends JpaRepository<AdminActionLogEntity, Integer> {
}
