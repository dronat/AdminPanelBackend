package com.example.adminpanelbackend.dataBase;

import com.example.adminpanelbackend.repository.AdminEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Persistence;

public class AdminsEntityManager extends JpaManager{
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminsEntityManager.class);
    public AdminsEntityManager() {
        super(Persistence.createEntityManagerFactory("AdminPanelPersistence").createEntityManager());
    }

    public AdminEntity getAdminBySteamID(String strSteamId) {
        try {
            long steamId = Long.parseLong(strSteamId);
            return em.createQuery("SELECT a FROM AdminEntity a WHERE a.steamId=:steamId", AdminEntity.class)
                    .setParameter("steamId", steamId)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.error("SQL error while get admin by id " + strSteamId, e);
            return null;
        }
    }
}
