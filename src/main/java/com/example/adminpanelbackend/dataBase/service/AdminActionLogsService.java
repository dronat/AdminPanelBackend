package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.AdminActionLogEntity;
import com.example.adminpanelbackend.dataBase.entity.PlayerMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;

public interface AdminActionLogsService extends JpaRepository<AdminActionLogEntity, Integer> {
    Page<AdminActionLogEntity> findAllBy(long adminSteamId, Pageable pageable);

    @Query(value = "SELECT a FROM AdminActionLogEntity a WHERE CAST(a.adminsByAdminId.steamId as string) LIKE %:adminSteamId% " +
            "AND CAST(a.playerByAdminId.steamId as string) LIKE %:playerSteamId% " +
            "AND CAST(a.action as string) LIKE %:action% " +
            "AND a.createTime >= :dateFrom " +
            "AND a.createTime <= :dateTo")
    Page<AdminActionLogEntity> findAllByContainsInNameAndSteamId(String adminSteamId, String playerSteamId, String action, Timestamp dateFrom, Timestamp dateTo, Pageable pageable);

}
