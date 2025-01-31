package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.PlayerMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerMessageService extends JpaRepository<PlayerMessageEntity, Integer> {
    @Query(value = "SELECT a FROM PlayerMessageEntity a WHERE a.player.name LIKE %:text% " +
            "OR CAST(a.player.steamId as string) LIKE %:text% " +
            "OR a.message LIKE %:text%")
    Page<PlayerMessageEntity> findAllByContainsInNameAndSteamId(String text, Pageable pageable);

    @Query(value = "SELECT a FROM PlayerMessageEntity a WHERE a.player.steamId = :playerSteamId")
    Page<PlayerMessageEntity> findAllByPlayer(long playerSteamId, Pageable pageable);

}
