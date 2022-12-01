package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.AdminActionLogEntity;
import com.example.adminpanelbackend.dataBase.entity.LayerHistoryEntity;
import com.example.adminpanelbackend.dataBase.entity.PlayerMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerMessageService extends JpaRepository<PlayerMessageEntity, Integer> {
    @Query(value = "SELECT a FROM PlayerMessageEntity a WHERE a.playersBySteamId.name LIKE %:text% " +
            "OR CAST(a.playersBySteamId.steamId as string) LIKE %:text% " +
            "OR a.message LIKE %:text%")
    Page<PlayerMessageEntity> findAllByContainsInNameAndSteamId(String text, Pageable pageable);

}
