package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.ServersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServersService extends JpaRepository<ServersEntity, Integer> {
}
