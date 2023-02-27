package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.MapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MapService extends JpaRepository<MapEntity, Integer> {

    @Query(value = "SELECT a.rawName FROM MapEntity a")
    List<String> findAllRawNames();
}
