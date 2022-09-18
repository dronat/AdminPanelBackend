package com.example.adminpanelbackend.dataBase;

import com.example.adminpanelbackend.dataBase.core.JpaConnection;
import com.example.adminpanelbackend.dataBase.core.JpaManager;
import com.example.adminpanelbackend.dataBase.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

public class EntityManager extends JpaManager implements JpaConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManager.class);

    public EntityManager() {
        super(EMF_THREAD_LOCAL.getEntityManager());
    }


    public void addAdmin(long steamId) {
        LOGGER.info("\u001B[46m \u001B[30m Added new admin with steamId: {} \u001B[0m", steamId);
        persist(
                new AdminEntity()
                        .setSteamId(steamId)
                        .setName("notLoggedIn")
                        .setRole(0)
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
        );
    }

    public void deleteAdmin(long steamId) {
        LOGGER.info("\u001B[46m \u001B[30m Delete admin with steamId: {} \u001B[0m", steamId);
        remove(getAdminBySteamID(steamId));
    }

    public AdminEntity getAdminBySteamID(long adminSteamId) {
        try {
            return em.createQuery("SELECT a FROM AdminEntity a WHERE a.steamId=:steamId", AdminEntity.class)
                    .setParameter("steamId", adminSteamId)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.error("SQL error while get admin by steamId " + adminSteamId, e);
            return null;
        }
    }

    public void addAdminActionInLog(long adminSteamId, String action) {
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addAdminActionInLog(admin, action);
    }

    public void addAdminActionInLog(AdminEntity admin, String action) {
        LOGGER.info("\u001B[46m \u001B[30m New admin action: admin '{}' made '{}' \u001B[0m", admin.getName(), action);
        persist(new AdminActionLogEntity().setAdminsByAdminId(admin)
                .setAction(action)
                .setCreateTime(new Timestamp(System.currentTimeMillis())));
        refresh(admin);
    }

    public boolean isPlayerExist(long steamId) {
        return !em.createQuery("SELECT a FROM PlayerEntity a WHERE a.steamId=:steamId", PlayerEntity.class)
                .setParameter("steamId", steamId)
                .getResultList()
                .isEmpty();
    }

    public void addPlayer(long steamId, String name) {
        LOGGER.info("\u001B[46m \u001B[30m New unique player entered on the server: {} ({}) \u001B[0m", name, steamId);
        persist(
                new PlayerEntity()
                        .setSteamId(steamId)
                        .setName(name)
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
        );
    }

    public PlayerEntity getPlayerBySteamId(long steamId) {
        try {
            return em.createQuery("SELECT a FROM PlayerEntity a WHERE a.steamId=:steamId", PlayerEntity.class)
                    .setParameter("steamId", steamId)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.error("SQL error while get player by steamId " + steamId, e);
            return null;
        }
    }

    public void addPlayerBan(long playerSteamId, long adminSteamId, String expireTime, String reason) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        PlayerBanEntity ban = new PlayerBanEntity()
                .setPlayersBySteamId(player)
                .setAdminsBySteamId(admin)
                .setReason(reason)
                .setIsUnbannedManually(false)
                .setExpirationTime(new Timestamp(Long.parseLong(expireTime)))
                .setCreationTime(new Timestamp(System.currentTimeMillis()));

        persist(ban);
        refresh(player);
        refresh(admin);
        addAdminActionInLog(adminSteamId, "Забанил игрока '" + playerSteamId + "' на '" + expireTime + "' по причине '" + reason + "'");
    }

    public void unbanPlayerBan(int banId, long adminSteamId) {
        PlayerBanEntity ban = em.createQuery("SELECT a FROM PlayerBanEntity a WHERE a.id=:id", PlayerBanEntity.class)
                .setParameter("id", banId)
                .getSingleResult();
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        update(
                ban.setIsUnbannedManually(true)
                        .setUnbannedAdminBySteamId(admin)
                        .setUnbannedTime(new Timestamp(System.currentTimeMillis()))
        );
        update(admin);
        update(ban.getPlayersBySteamId());
        addAdminActionInLog(admin.getSteamId(), "Разбанил игрока '" + ban.getPlayersBySteamId().getSteamId() + "'");
    }

    public List<PlayerBanEntity> getActiveBans() {
        return em.createQuery("SELECT a FROM PlayerBanEntity a WHERE a.isUnbannedManually = false AND a.expirationTime > :currentTime", PlayerBanEntity.class)
                .setParameter("currentTime", new Timestamp(System.currentTimeMillis()))
                .getResultList();
    }

    public void addPlayerNote(long playerSteamId, long adminSteamId, String note) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addPlayerNote(player, admin, note);
    }

    public void addPlayerNote(PlayerEntity player, AdminEntity admin, String note) {
        LOGGER.info("\u001B[46m \u001B[30m New player note: player '{}' note: '{}' \u001B[0m", player.getName(), note);
        persist(
                new PlayerNoteEntity()
                        .setPlayersBySteamId(player)
                        .setAdminsBySteamId(admin)
                        .setNote(note)
                        .setCreationTime(new Timestamp(System.currentTimeMillis()))
        );
        refresh(player);
    }

    public void deletePlayerNote(int noteId) {
        PlayerNoteEntity playerNote = em.find(PlayerNoteEntity.class, noteId);
        PlayerEntity player = playerNote.getPlayersBySteamId();
        LOGGER.info("\u001B[46m \u001B[30m Deleted player note: player '{}' note: '{}' \u001B[0m", player.getName(), playerNote.getNote());
        remove(playerNote);
        refresh(playerNote);
    }

    public PlayerNoteEntity getPlayerNote(int noteId) {
        return em.find(PlayerNoteEntity.class, noteId);
    }

    public void addPlayerMessage(long steamId, String chatType, String message) {
        PlayerEntity player = getPlayerBySteamId(steamId);
        addPlayerMessage(player, chatType, message);
    }

    public void addPlayerMessage(PlayerEntity player, String chatType, String message) {
        LOGGER.info("\u001B[46m \u001B[30m New player message - player: '{}' chatType: '{}' message: '{}' \u001B[0m", player.getName(), chatType, message);
        persist(
                new PlayerMessageEntity()
                        .setPlayersBySteamId(player)
                        .setChatType(chatType)
                        .setMessage(message)
                        .setCreationTime(new Timestamp(System.currentTimeMillis()))
        );
        refresh(player);
    }

    public void addPlayerKick(long playerSteamId, long adminSteamId, String reason) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addPlayerKick(player, admin, reason);
    }

    public void addPlayerKick(PlayerEntity player, long adminSteamId, String reason) {
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addPlayerKick(player, admin, reason);
    }

    public void addPlayerKick(long playerSteamId, AdminEntity admin, String reason) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        addPlayerKick(player, admin, reason);
    }

    public void addPlayerKick(PlayerEntity player, AdminEntity admin, String reason) {
        LOGGER.info("\u001B[46m \u001B[30m Player '{}' kicked by '{}' by reason '{}' \u001B[0m", player.getName(), admin.getName(), reason);
        persist(
                new PlayerKickEntity()
                        .setPlayersBySteamId(player)
                        .setAdminsBySteamId(admin)
                        .setReason(reason)
                        .setCreationTime(new Timestamp(System.currentTimeMillis()))
        );
        refresh(player);
    }
}
