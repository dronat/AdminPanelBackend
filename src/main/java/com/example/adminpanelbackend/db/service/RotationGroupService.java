package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.RotationGroupEntity;
import com.example.adminpanelbackend.db.entity.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RotationGroupService extends JpaRepository<RotationGroupEntity, Integer> {
    @Query
    void deleteById(RotationGroupEntity serversEntity);

    RotationGroupEntity findByServerIDAndIsActiveIsTrue(ServerEntity serverEntity);

    List<RotationGroupEntity> findAllByServerID(ServerEntity serverEntity);
}
