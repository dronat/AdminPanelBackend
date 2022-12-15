package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.PlayerBanEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerBanService extends JpaRepository<PlayerBanEntity, Integer> {

    @NotNull
    @Query(value = "SELECT a FROM PlayerBanEntity a WHERE a.isUnbannedManually = FALSE AND (a.expirationTime IS NULL OR a.expirationTime > CURRENT_TIMESTAMP)")
    Page<PlayerBanEntity> findAllActiveBans(@NotNull Pageable pageable);

    @NotNull
    Page<PlayerBanEntity> findAll(@NotNull Pageable pageable);

    @Query(value = "SELECT a FROM PlayerBanEntity a WHERE a.player.steamId = :playerSteamId")
    Page<PlayerBanEntity> findAllByPlayer(long playerSteamId, @NotNull Pageable pageable);

    @Query(value = "SELECT a FROM PlayerBanEntity a WHERE " +
            "CAST(a.player.steamId as string) LIKE %:playerSteamId% " +
            "AND CAST(a.admin.steamId as string) LIKE %:adminSteamId% ")
    Page<PlayerBanEntity> findAllBansByParams(String playerSteamId, String adminSteamId, @NotNull Pageable pageable);

    @Query(value = "SELECT a FROM PlayerBanEntity a WHERE a.isUnbannedManually = FALSE " +
            "AND a.expirationTime IS NOT NULL " +
            "AND a.expirationTime > CURRENT_TIMESTAMP " +
            "AND CAST(a.player.steamId as string) LIKE %:playerSteamId% " +
            "AND CAST(a.admin.steamId as string) LIKE %:adminSteamId% ")
    Page<PlayerBanEntity> findActiveBansByParams(String playerSteamId, String adminSteamId, @NotNull Pageable pageable);

    @Query(value = "SELECT a FROM PlayerBanEntity a WHERE a.isUnbannedManually = FALSE " +
            "AND a.expirationTime IS NULL " +
            "AND CAST(a.player.steamId as string) LIKE %:playerSteamId% " +
            "AND CAST(a.admin.steamId as string) LIKE %:adminSteamId% ")
    Page<PlayerBanEntity> findPermanentBansByParams(String playerSteamId, String adminSteamId, @NotNull Pageable pageable);

    @Query(value = "SELECT a FROM PlayerBanEntity a WHERE (a.isUnbannedManually = TRUE OR (a.expirationTime IS NOT NULL AND a.expirationTime < CURRENT_TIMESTAMP)) " +
            "AND CAST(a.player.steamId as string) LIKE %:playerSteamId% " +
            "AND CAST(a.admin.steamId as string) LIKE %:adminSteamId% ")
    Page<PlayerBanEntity> findNotActiveBansByParams(String playerSteamId, String adminSteamId, @NotNull Pageable pageable);

}
