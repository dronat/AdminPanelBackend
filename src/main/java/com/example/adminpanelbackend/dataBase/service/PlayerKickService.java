package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.PlayerKickEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerKickService extends JpaRepository<PlayerKickEntity, Integer> {

    @NotNull
    Page<PlayerKickEntity> findAllByPlayer(long playerSteamId, @NotNull Pageable pageable);

}
