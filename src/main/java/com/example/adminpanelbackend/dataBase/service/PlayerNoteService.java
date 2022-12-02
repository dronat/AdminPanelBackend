package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.PlayerEntity;
import com.example.adminpanelbackend.dataBase.entity.PlayerMessageEntity;
import com.example.adminpanelbackend.dataBase.entity.PlayerNoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerNoteService extends JpaRepository<PlayerNoteEntity, Integer> {

    Page<PlayerNoteEntity> findAllByPlayersBySteamId(long steamId, Pageable pageable);

}
