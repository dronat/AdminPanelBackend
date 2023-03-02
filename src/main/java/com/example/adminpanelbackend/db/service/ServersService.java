package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServersService extends JpaRepository<ServerEntity, Integer> {
}
