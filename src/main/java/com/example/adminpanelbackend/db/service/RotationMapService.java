package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.RotationGroupEntity;
import com.example.adminpanelbackend.db.entity.RotationMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RotationMapService extends JpaRepository<RotationMapEntity, Integer> {
    Integer deleteAllByRotationGroup(RotationGroupEntity rotationGroupEntity);
}
