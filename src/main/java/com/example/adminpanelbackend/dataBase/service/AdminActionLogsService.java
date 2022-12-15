package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.AdminActionLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;

public interface AdminActionLogsService extends JpaRepository<AdminActionLogEntity, Integer> {
    @Query(value = "SELECT a FROM AdminActionLogEntity a WHERE a.admin.steamId = :adminSteamId")
    Page<AdminActionLogEntity> findAllByAdmin(long adminSteamId, Pageable pageable);

    @Query(value = "SELECT a FROM AdminActionLogEntity a WHERE CAST(a.admin.steamId as string) LIKE %:adminSteamId% " +
            "AND CAST(a.player.steamId as string) LIKE %:playerSteamId% " +
            "AND CAST(a.action as string) IN (:actions) " +
            "AND a.createTime >= :dateFrom " +
            "AND a.createTime <= :dateTo")
    Page<AdminActionLogEntity> findAllByParams(String adminSteamId, String playerSteamId, List<String> actions, Timestamp dateFrom, Timestamp dateTo, Pageable pageable);

    @Query(value = "SELECT a FROM AdminActionLogEntity a WHERE a.player.steamId = :playerSteamId")
    Page<AdminActionLogEntity> findAllActionsWithPlayerByAdmin(long playerSteamId, Pageable pageable);

}
