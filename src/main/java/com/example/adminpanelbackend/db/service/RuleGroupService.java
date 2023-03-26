package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.RuleGroupEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RuleGroupService extends JpaRepository<RuleGroupEntity, Integer> {
    @Query(value = "SELECT a FROM RuleGroupEntity a")
    @NotNull
    List<RuleGroupEntity> findAll();

}
