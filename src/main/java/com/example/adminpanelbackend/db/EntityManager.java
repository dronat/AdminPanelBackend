package com.example.adminpanelbackend.db;

import com.example.adminpanelbackend.ActionEnum;
import com.example.adminpanelbackend.RoleEnum;
import com.example.adminpanelbackend.SteamService;
import com.example.adminpanelbackend.db.core.JpaConnection;
import com.example.adminpanelbackend.db.core.JpaManager;
import com.example.adminpanelbackend.db.entity.*;
import com.example.adminpanelbackend.model.SteamUserModel;
import com.woop.Squad4J.server.SquadServer;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.adminpanelbackend.ActionEnum.*;

public class EntityManager extends JpaManager implements JpaConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManager.class);
    private static final int SERVER_ID = ConfigLoader.get("server.id", Integer.class);
    private static volatile boolean initialized = false;
    private static EntityManager entityManager;

    public EntityManager() {
        super(EMF_THREAD_LOCAL.getEntityManager());
    }

    public static synchronized EntityManager getInstance() {
        if (entityManager == null) {
            entityManager = new EntityManager();
        }
        return entityManager;
    }
    public synchronized void init() {
        List<RoleEnum> roleEnums = List.of(RoleEnum.values());
        List<String> roleEntities = em.createQuery("SELECT a FROM RoleEntity a", RoleEntity.class)
                .getResultList()
                .stream()
                .map(RoleEntity::getName)
                .toList();
        roleEnums.forEach(roleEnum -> {
            if (!roleEntities.contains(roleEnum.name)) {
                persist(
                        new RoleEntity()
                                .setName(roleEnum.name)
                                .setDescription(roleEnum.description)
                );
            }
        });
        try {
            em.createQuery("SELECT a FROM  AdminEntity a WHERE steamId = 1").getSingleResult();
        } catch (Exception e) {
            persist(
                    new AdminEntity()
                    .setSteamId(1L)
                    .setName("Admin Panel")
                    .setCreateTime(new Timestamp(System.currentTimeMillis()))
                    .setModifiedTime(new Timestamp(System.currentTimeMillis()))
            );
        }

    }

    public synchronized void addAdmin(long adminSteamId) {
        LOGGER.info("\u001B[46m \u001B[30m Added new admin with adminSteamId: {} \u001B[0m", adminSteamId);
        persist(
                new AdminEntity()
                        .setSteamId(adminSteamId)
                        .setName("notLoggedIn")
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
                        .setModifiedTime(new Timestamp(System.currentTimeMillis()))
        );
        SquadServer.addAdmin(adminSteamId);
    }

    public synchronized void deactivateAdmin(long adminSteamId) {
        LOGGER.info("\u001B[46m \u001B[30m Delete admin with adminSteamId: {} \u001B[0m", adminSteamId);
        update(getAdminBySteamID(adminSteamId).setRoleGroup(null).setModifiedTime(new Timestamp(System.currentTimeMillis())));
        SquadServer.removeAdmin(adminSteamId);
    }

    public synchronized AdminEntity getAdminBySteamID(long adminSteamId) {
        try {
            return em.createQuery("SELECT a FROM AdminEntity a WHERE a.steamId=:steamId", AdminEntity.class)
                    .setParameter("steamId", adminSteamId)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.warn("SQL error while get admin by steamId " + adminSteamId);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return getAdminBySteamID(adminSteamId);
        }
    }

    public synchronized AdminEntity tryGetAdminBySteamID(long adminSteamId) {
        return em.createQuery("SELECT a FROM AdminEntity a WHERE a.steamId=:steamId", AdminEntity.class)
                .setParameter("steamId", adminSteamId)
                .getSingleResult();
    }

    public synchronized List<Long> getActiveAdminsSteamId() {
        try {
            return em.createQuery("SELECT a FROM AdminEntity a WHERE a.roleGroup IS NOT NULL", AdminEntity.class)
                    .getResultList()
                    .stream()
                    .map(AdminEntity::getSteamId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.warn("SQL error while get steam ids of active admins");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return getActiveAdminsSteamId();
        }
    }

    public synchronized void addAdminActionInLog(long adminSteamId, Long playerSteamId, ActionEnum action, String reason) {
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        PlayerEntity player = playerSteamId == null ? null : getPlayerBySteamId(playerSteamId);
        addAdminActionInLog(admin, player, action, reason);
    }

    public synchronized void addAdminActionInLog(AdminEntity admin, PlayerEntity player, ActionEnum action, String reason) {
        LOGGER.info("\u001B[46m \u001B[30m New admin action: admin '{}' made '{}' \u001B[0m", admin.getName(), action);
        persist(
                new AdminActionLogEntity()
                        .setAdmin(admin)
                        .setPlayer(player)
                        .setAction(action.actionName)
                        .setReason(reason)
                        .setServerId(getServerById(SERVER_ID))
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
        );
        refresh(admin);
    }

    public synchronized boolean isPlayerExist(long steamId) {
        try {
            return !em.createQuery("SELECT a FROM PlayerEntity a WHERE a.steamId=:steamId", PlayerEntity.class)
                    .setParameter("steamId", steamId)
                    .getResultList()
                    .isEmpty();
        } catch (Exception e) {
            LOGGER.warn("Exception while trying execute sql query isPlayerExist");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return isPlayerExist(steamId);
        }
    }

    public synchronized boolean isAdminExist(long steamId) {
        try {
            return !em.createQuery("SELECT a FROM AdminEntity a WHERE a.steamId=:steamId", AdminEntity.class)
                    .setParameter("steamId", steamId)
                    .getResultList()
                    .isEmpty();
        } catch (Exception e) {
            LOGGER.warn("Exception while trying execute sql query isAdminExist");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return isPlayerExist(steamId);
        }
    }

    public synchronized void addPlayer(long steamId, String name) {
        LOGGER.info("\u001B[46m \u001B[30m New unique player entered on the server: {} ({}) \u001B[0m", name, steamId);
        persist(
                new PlayerEntity()
                        .setSteamId(steamId)
                        .setName(name)
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
        );
    }

    public synchronized PlayerEntity getPlayerBySteamId(long steamId) {
        try {
            if (!isPlayerExist(steamId)) {
                SteamUserModel.Response.Player player = SteamService.getSteamUserInfo(steamId);
                addPlayer(steamId, player.getPersonaname());
            }
            return em.createQuery("SELECT a FROM PlayerEntity a WHERE a.steamId=:steamId", PlayerEntity.class)
                    .setParameter("steamId", steamId)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.warn("SQL error while get player by steamId " + steamId);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return getPlayerBySteamId(steamId);
        }
    }

    public synchronized List<Long> getPlayersOnControl() {
        try {
            return em.createQuery("SELECT a FROM PlayerEntity a WHERE a.onControl=true", PlayerEntity.class)
                    .getResultList().stream().map(PlayerEntity::getSteamId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("SQL error while get players on control ");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return getPlayersOnControl();
        }
    }

    public synchronized void addPlayerBan(long playerSteamId, long adminSteamId, Timestamp expireTime, String reason) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        PlayerBanEntity ban = new PlayerBanEntity()
                .setPlayer(player)
                .setAdmin(admin)
                .setReason(reason)
                .setIsUnbannedManually(false)
                .setExpirationTime(expireTime)
                .setCreationTime(new Timestamp(System.currentTimeMillis()));

        persist(ban);
        refresh(player);
        refresh(admin);
        addAdminActionInLog(adminSteamId, playerSteamId, BAN_PLAYER, reason);
    }

    public synchronized void unbanPlayerBan(int banId, long adminSteamId) {
        PlayerBanEntity ban = em.createQuery("SELECT a FROM PlayerBanEntity a WHERE a.id=:id", PlayerBanEntity.class)
                .setParameter("id", banId)
                .getSingleResult();
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        update(
                ban.setIsUnbannedManually(true)
                        .setUnbannedAdmin(admin)
                        .setUnbannedTime(new Timestamp(System.currentTimeMillis()))
        );
        update(admin);
        update(ban.getPlayer());
        addAdminActionInLog(admin.getSteamId(), ban.getPlayer().getSteamId(), UNBAN, null);
    }

    public synchronized List<PlayerBanEntity> getActiveBans() {
        return em.createQuery("SELECT a FROM PlayerBanEntity a WHERE a.isUnbannedManually = FALSE AND (a.expirationTime IS NULL OR a.expirationTime > :currentTime)", PlayerBanEntity.class)
                .setParameter("currentTime", new Timestamp(System.currentTimeMillis()))
                .getResultList();
    }

    public synchronized List<PlayerBanEntity> getActiveNonPermanentBans() {
        return em.createQuery("SELECT a FROM PlayerBanEntity a WHERE a.isUnbannedManually = FALSE AND a.expirationTime IS NOT NULL AND a.expirationTime > :currentTime", PlayerBanEntity.class)
                .setParameter("currentTime", new Timestamp(System.currentTimeMillis()))
                .getResultList();
    }

    public synchronized void addPlayerNote(long playerSteamId, long adminSteamId, String note) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addPlayerNote(player, admin, note);
    }

    public synchronized void addPlayerNote(PlayerEntity player, AdminEntity admin, String note) {
        LOGGER.info("\u001B[46m \u001B[30m New player note: player '{}' note: '{}' \u001B[0m", player.getName(), note);
        persist(
                new PlayerNoteEntity()
                        .setPlayer(player)
                        .setAdmin(admin)
                        .setNote(note)
                        .setCreationTime(new Timestamp(System.currentTimeMillis()))
        );
        refresh(player);
        addAdminActionInLog(admin, player, ADD_PLAYER_NOTE, note);
    }

    public synchronized PlayerNoteEntity getPlayerNote(int noteId) {
        return em.find(PlayerNoteEntity.class, noteId);
    }

    public synchronized void addPlayerOnControl(long adminSteamId, long playerSteamId) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addPlayerOnControl(admin, player);
    }

    public synchronized void addPlayerOnControl(AdminEntity admin, PlayerEntity player) {
        LOGGER.info("\u001B[46m \u001B[30m Player {} added on control by admin {} \u001B[0m", player.getName(), admin.getName());
        update(player.setOnControl(true));
        addPlayerNote(player, admin, "Добавил игрока на контроль");
        addAdminActionInLog(admin, player, ADD_PLAYER_ON_CONTROL, null);
        SquadServer.addPlayerOnControl(player.getSteamId());
    }

    public synchronized void removePlayerFromControl(long adminSteamId, long playerSteamId) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        removePlayerFromControl(admin, player);
    }

    public synchronized void removePlayerFromControl(AdminEntity admin, PlayerEntity player) {
        LOGGER.info("\u001B[46m \u001B[30m Player {} removed from control by admin {} \u001B[0m", player.getName(), admin.getName());
        update(player.setOnControl(false));
        addPlayerNote(player, admin, "Убрал игрока с контроля");
        addAdminActionInLog(admin, player, REMOVE_PLAYER_FROM_CONTROL, null);
        SquadServer.removePlayerFromControl(player.getSteamId());
    }

    public synchronized void deletePlayerNote(int noteId) {
        PlayerNoteEntity playerNote = em.find(PlayerNoteEntity.class, noteId);
        PlayerEntity player = playerNote.getPlayer();
        LOGGER.info("\u001B[46m \u001B[30m Deleted player note: player '{}' note: '{}' \u001B[0m", player.getName(), playerNote.getNote());
        remove(playerNote);
        refresh(player);
    }

    public synchronized void addPlayerMessage(long steamId, String chatType, String message) {
        PlayerEntity player = getPlayerBySteamId(steamId);
        addPlayerMessage(player, chatType, message);
    }

    public synchronized void addPlayerMessage(PlayerEntity player, String chatType, String message) {
        LOGGER.info("\u001B[46m \u001B[30m New player message - player: '{}' chatType: '{}' message: '{}' \u001B[0m", player.getName(), chatType, message);
        persist(
                new PlayerMessageEntity()
                        .setPlayer(player)
                        .setChatType(chatType)
                        .setMessage(message)
                        .setServerId(getServerById(SERVER_ID))
                        .setCreationTime(new Timestamp(System.currentTimeMillis()))
        );
        refresh(player);
    }

    public synchronized void addPlayerKick(long playerSteamId, long adminSteamId, String reason) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addPlayerKick(player, admin, reason);
    }

    public synchronized void addPlayerKick(PlayerEntity player, long adminSteamId, String reason) {
        AdminEntity admin = getAdminBySteamID(adminSteamId);
        addPlayerKick(player, admin, reason);
    }

    public synchronized void addPlayerKick(long playerSteamId, AdminEntity admin, String reason) {
        PlayerEntity player = getPlayerBySteamId(playerSteamId);
        addPlayerKick(player, admin, reason);
    }

    public synchronized void addPlayerKick(PlayerEntity player, AdminEntity admin, String reason) {
        LOGGER.info("\u001B[46m \u001B[30m Player '{}' kicked by '{}' by reason '{}' \u001B[0m", player.getName(), admin.getName(), reason);
        persist(
                new PlayerKickEntity()
                        .setPlayer(player)
                        .setAdmin(admin)
                        .setReason(reason)
                        .setCreationTime(new Timestamp(System.currentTimeMillis()))
        );
        refresh(player);
    }

    public synchronized void addLayer(String layer) {
        MapEntity mapEntity = getMapByOneOfNames(layer);
        persist(
                new LayerHistoryEntity()
                        .setLayer(mapEntity)
                        .setServerId(getServerById(SERVER_ID))
                        .setCreationTime(new Timestamp(System.currentTimeMillis()))
        );
    }

    public synchronized List<DiscordMessageIdEntity> getAllDiscordMessagesId() {
        return em.createQuery("SELECT a FROM DiscordMessageIdEntity a", DiscordMessageIdEntity.class)
                .getResultList();
    }

    public synchronized void deleteAllRowsFromDiscordMessagesId() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM DiscordMessageIdEntity").executeUpdate();
        em.getTransaction().commit();
    }

    @Transactional
    public synchronized void deleteRowByMessageId(String messageId) {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM DiscordMessageIdEntity WHERE messageId = :messageId")
                .setParameter("messageId", messageId)
                .executeUpdate();
        em.getTransaction().commit();
    }

    public synchronized void addDiscordMessageId(String id, String title) {
        persist(new DiscordMessageIdEntity().setMessageId(id).setTitle(title));
    }

    public synchronized MapEntity getMapByOneOfNames(String name) {
        return em.createQuery("SELECT t FROM MapEntity t WHERE t.name LIKE :name OR t.rawName LIKE :name  OR t.levelName LIKE :name", MapEntity.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    public synchronized List<MapEntity> getAllMaps() {
        return em.createQuery("SELECT t FROM MapEntity t", MapEntity.class)
                .getResultList();
    }

    public synchronized RotationGroupEntity getActiveRotationGroupByServerId(ServerEntity serverEntity) {
        return em.createQuery("SELECT t FROM RotationGroupEntity t where t.isActive = true AND t.serverID = :serverEntity", RotationGroupEntity.class)
                .setParameter("serverEntity", serverEntity)
                .getSingleResult();
    }

    public synchronized RotationGroupEntity getActiveRotationGroupByServerId(int serverId) {
        ServerEntity serverEntity = getServerById(serverId);
        return em.createQuery("SELECT t FROM RotationGroupEntity t where t.isActive = true AND t.serverID = :serverEntity", RotationGroupEntity.class)
                .setParameter("serverEntity", serverEntity)
                .getSingleResult();
    }

    public synchronized RotationGroupEntity getRotationByRotationIdAndServerId(int rotationId, int serverId) {
        ServerEntity serverEntity = getServerById(serverId);
        return em.createQuery("SELECT t FROM RotationGroupEntity t where t.id = :rotationId AND t.serverID = :serverEntity", RotationGroupEntity.class)
                .setParameter("serverEntity", serverEntity)
                .setParameter("rotationId", rotationId)
                .getSingleResult();
    }

    public synchronized ServerEntity getServerById(int serverId) {
        try {
            return em.createQuery("SELECT t FROM ServerEntity t where t.id = :serverId", ServerEntity.class)
                    .setParameter("serverId", serverId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }

    }
}
