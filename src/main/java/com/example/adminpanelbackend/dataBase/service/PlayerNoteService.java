package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.PlayerNoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerNoteService extends JpaRepository<PlayerNoteEntity, Integer> {

    Page<PlayerNoteEntity> findAllByPlayer(long steamId, Pageable pageable);

}
