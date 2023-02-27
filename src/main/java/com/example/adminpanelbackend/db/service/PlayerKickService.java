package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.PlayerKickEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerKickService extends JpaRepository<PlayerKickEntity, Integer> {

    @Query(value = "SELECT a FROM PlayerKickEntity a WHERE a.player.steamId = :playerSteamId")
    Page<PlayerKickEntity> findAllByPlayer(long playerSteamId, @NotNull Pageable pageable);

}
