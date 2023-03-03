package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.LayerHistoryEntity;
import com.example.adminpanelbackend.db.entity.ServerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LayersHistoryService extends JpaRepository<LayerHistoryEntity, Integer> {
    Page<LayerHistoryEntity> findAllByServerId(ServerEntity serverEntity, Pageable pageable);
}
