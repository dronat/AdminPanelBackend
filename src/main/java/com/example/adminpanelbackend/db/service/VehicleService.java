package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleService extends JpaRepository<VehicleEntity, Integer> {

}