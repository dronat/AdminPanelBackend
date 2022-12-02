package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.PlayerNoteEntity;
import com.example.adminpanelbackend.dataBase.entity.RuleGroupEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedList;
import java.util.List;

public interface RuleGroupService extends JpaRepository<RuleGroupEntity, Integer> {
    @Query(value = "SELECT a FROM RuleGroupEntity a")
    @NotNull
    List<RuleGroupEntity> findAll();

}
