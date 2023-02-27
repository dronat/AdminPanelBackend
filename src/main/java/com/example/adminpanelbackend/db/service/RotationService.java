package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.RotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RotationService extends JpaRepository<RotationEntity, Integer> {
    @Query
    void deleteAllByServerId(int roleId);
}
