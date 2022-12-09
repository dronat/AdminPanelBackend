package com.example.adminpanelbackend.dataBase.service;

import com.example.adminpanelbackend.dataBase.entity.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RolesService extends JpaRepository<RolesEntity, Integer> {
    @Query(value = "SELECT a FROM RolesEntity a WHERE a.role.id = :roleId AND a.roleGroup.id = :roleGroupId")
    RolesEntity findByRoleAndRoleGroup(int roleId, int roleGroupId);
}
