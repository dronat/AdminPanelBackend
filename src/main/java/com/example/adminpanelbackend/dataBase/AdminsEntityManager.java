package com.example.adminpanelbackend.dataBase;

import com.example.adminpanelbackend.dataBase.core.JpaConnection;
import com.example.adminpanelbackend.dataBase.core.JpaManager;
import com.example.adminpanelbackend.repository.Admins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminsEntityManager extends JpaManager implements JpaConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminsEntityManager.class);
    public AdminsEntityManager() {
        super(EMF_THREAD_LOCAL.getEntityManager());
    }

    public Admins getAdminBySteamID(String strSteamId) {
        try {
            long steamId = Long.parseLong(strSteamId);
            return em.createQuery("SELECT a FROM Admins a WHERE a.steamId=:steamId", Admins.class)
                    .setParameter("steamId", steamId)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.error("SQL error while get admin by id " + strSteamId, e);
            return null;
        }
    }
}
