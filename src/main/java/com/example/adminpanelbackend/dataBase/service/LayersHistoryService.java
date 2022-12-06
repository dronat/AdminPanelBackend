package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.LayerHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LayersHistoryService extends JpaRepository<LayerHistoryEntity, Integer> {
}
