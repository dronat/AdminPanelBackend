package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.PlayerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerService extends JpaRepository<PlayerEntity, Integer> {
    @Query(value = "SELECT a FROM PlayerEntity a WHERE a.name LIKE %:text% " +
            "OR CAST(a.steamId as string) LIKE %:text%")
    Page<PlayerEntity> findAllByContainsInNameAndSteamId(String text, Pageable pageable);
    //Page<PlayerEntity> findAll(Pageable pageable);

    Page<PlayerEntity> findBySteamId(long steamId, Pageable pageable);

    @Query(value = "SELECT a FROM PlayerEntity a WHERE CAST(a.steamId as string) LIKE %:steamId%")
    Page<PlayerEntity> findAllBySteamIdContains(String steamId, Pageable pageable);

    //Page<PlayerEntity> findAllBySteamIdContaining(long steamId, Pageable pageable);

    //Page<PlayerEntity> findAllBySteamIdIsLike(long steamId, Pageable pageable);

    Page<PlayerEntity> findAllByNameContainsIgnoreCase(String name, Pageable pageable);

    PlayerEntity findPlayerEntityBySteamId(long steamId);

    //@Query("SELECT a FROM PlayerEntity a  WHERE a.name LIKE '%:value%' OR a.steamId ")
    //Page<PlayerEntity> findFirst10BySteamIdContainingIgnoreCaseOrNameContainsIgnoreCase(Long steamId, String name, Pageable pageable);

    //Page<PlayerEntity> findAllBySteamIdContainingIgnoreCaseOrNameContainsIgnoreCase(int limit, @Param("value") String value);
}
