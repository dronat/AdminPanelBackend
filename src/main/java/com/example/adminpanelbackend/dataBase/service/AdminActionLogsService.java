package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.AdminActionLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogsService extends JpaRepository<AdminActionLogEntity, Integer> {
    Page<AdminActionLogEntity> findAllBy(long adminSteamId, Pageable pageable);
}
