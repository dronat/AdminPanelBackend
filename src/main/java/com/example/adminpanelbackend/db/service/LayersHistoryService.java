package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.LayerHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LayersHistoryService extends JpaRepository<LayerHistoryEntity, Integer> {
}
