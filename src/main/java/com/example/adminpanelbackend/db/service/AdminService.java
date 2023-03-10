package com.example.adminpanelbackend.db.service;

import com.example.adminpanelbackend.db.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminService extends JpaRepository<AdminEntity, Long> {
    @Query("SELECT COUNT(*) FROM AdminEntity a WHERE a.roleGroup.id = :roleGroupId")
    int countOfAdminsWithRoleGroup(int roleGroupId);
}
