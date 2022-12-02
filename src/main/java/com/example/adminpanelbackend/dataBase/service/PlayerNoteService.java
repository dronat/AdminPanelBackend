package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.PlayerNoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerNoteService extends JpaRepository<PlayerNoteEntity, Integer> {
    @Query(value = "SELECT a FROM PlayerNoteEntity a WHERE a.player.steamId = :playerSteamId")
    Page<PlayerNoteEntity> findAllByPlayer(long playerSteamId, Pageable pageable);

}
