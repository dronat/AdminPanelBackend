package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.PlayerBanEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerBanService extends JpaRepository<PlayerBanEntity, Integer> {

    @NotNull
    @Query(value = "SELECT a FROM PlayerBanEntity a WHERE a.expirationTime > CURRENT_TIMESTAMP AND a.isUnbannedManually = false")
    Page<PlayerBanEntity> findAllActiveBans(@NotNull Pageable pageable);

    @NotNull
    Page<PlayerBanEntity> findAll(@NotNull Pageable pageable);

}
