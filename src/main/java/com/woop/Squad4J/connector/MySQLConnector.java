package com.woop.Squad4J.connector;

import com.woop.Squad4J.a2s.Query;
import com.woop.Squad4J.a2s.response.A2SInfoResponse;
import com.woop.Squad4J.server.LayerClassnameFormatter;
import com.woop.Squad4J.server.SquadServer;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Robert Engle
 */
public class MySQLConnector extends Connector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLConnector.class);

    private static Connection conn;
    private static Statement statement;

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final Integer serverID = ConfigLoader.get("server.id", Integer.class);

    private MySQLConnector() {
        super("mysql");
    }


    public static void init() {
        String ip = ConfigLoader.get("connectors.mysql.host", String.class);
        Integer port = ConfigLoader.get("connectors.mysql.port", Integer.class);
        String user = ConfigLoader.get("connectors.mysql.username", String.class);
        String password = ConfigLoader.get("connectors.mysql.password", String.class);
        String schema = ConfigLoader.get("connectors.mysql.database", String.class);

        String baseConnectionString = String.format("jdbc:mysql://%s:%d/%s?timezone=UTC&autoReconnect=true", ip, port, schema);

        try {
            LOGGER.info("Attempting to connect to MySQL server.");
            conn = DriverManager.getConnection(baseConnectionString, user, password);
            LOGGER.info("Connected to MySQL server.");
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            checkAndCreateTables();
            /*if (!serverExists()) {
                LOGGER.info("Server {} does not exist in DBLog_Servers, adding", serverID);
                createServer();
            }*/
        } catch (SQLException e) {
            LOGGER.error("SQL Exception.", e);
        }
    }

    /*private static boolean serverExists() {
        try {
            String serverName = Query.queryInfo().getName();
            ResultSet rs = statement.executeQuery("SELECT `id`, `server` FROM DBLog_Servers WHERE id = " + serverID + ";");
            if (rs.first()) {
                LOGGER.trace("Server with id {} already exists.", serverID);
                if (!rs.getString("server").equals(serverName)) {
                    //Server exists but with a different name, update name
                    updateServerName(serverName);
                }
                return true;
            }
            rs.close();
        } catch (SQLException e) {
            LOGGER.error("SQL Error.", e);
        }
        return false;
    }

    private static void updateServerName(String newServerName) {
        try {
            LOGGER.info("Server in database with id {} has a different name than the current name of the server. Updating name to {}", serverID, newServerName);
            statement.executeUpdate("UPDATE DBLog_Servers " +
                    "SET `server` = \'" + newServerName + "\'" +
                    "WHERE `id` = " + MySQLConnector.serverID + ";");
        } catch (SQLException e) {
            LOGGER.error("Error updating server name.", e);
        }
    }*/

    private static void checkAndCreateTables() throws SQLException {
        //Dont query information schema, just try to select from tables and if error, then they dont exist
        /*try {
            statement.executeQuery("SELECT COUNT(*) FROM DBLog_Servers");
        } catch (SQLException e) {
            createDbLogServers();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM DBLog_Matches");
        } catch (SQLException e) {
            createDbLogMatches();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM DBLog_Tickrates");
        } catch (SQLException e) {
            createDbLogTickRates();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM DBLog_PlayerCounts");
        } catch (SQLException e) {
            createDbLogPlayerCounts();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM DBLog_Wounds");
        } catch (SQLException e) {
            createDbLogWounds();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM DBLog_Deaths");
        } catch (SQLException e) {
            createDbLogDeaths();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM DBLog_Revives");
        } catch (SQLException e) {
            createDbLogRevives();
        }*/
        try {
            statement.executeQuery("SELECT COUNT(*) FROM SPRING_SESSION");
            statement.executeQuery("SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES");
        } catch (SQLException e) {
            createSpringSessions();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM admins");
        } catch (SQLException e) {
            createAdmin();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM players");
        } catch (SQLException e) {
            createPlayers();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM admins_action_log");
        } catch (SQLException e) {
            createAdminActionLog();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM players_bans");
        } catch (SQLException e) {
            createPlayersBans();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM players_kicks");
        } catch (SQLException e) {
            createPlayersKicks();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM players_messages");
        } catch (SQLException e) {
            createPlayersMessages();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM players_notes");
        } catch (SQLException e) {
            createPlayersNotes();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM layers_history");
        } catch (SQLException e) {
            createLayersHistory();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM rule_groups");
        } catch (SQLException e) {
            createRuleGroups();
        }
        try {
            statement.executeQuery("SELECT COUNT(*) FROM rules");
        } catch (SQLException e) {
            createRules();
        }
    }

    /*private static void createDbLogServers() throws SQLException {
        LOGGER.info("Creating table DBLog_Servers");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS DBLog_Servers (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "server VARCHAR(255)" +
                ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
    }

    private static void createDbLogMatches() throws SQLException {
        LOGGER.info("Creating table DBLog_Matches");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS DBLog_Matches (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "dlc VARCHAR(255)," +
                "mapClassname VARCHAR(255)," +
                "layerClassname VARCHAR(255)," +
                "map VARCHAR(255) NOT NULL," +
                "layer VARCHAR(255) NOT NULL," +
                "startTime DATETIME," +
                "endTime DATETIME," +
                "winner VARCHAR(255)," +
                "server INT NOT NULL," +
                "KEY `server` (`server`)," +
                "CONSTRAINT `squad4j_dblog_matches_fk_server` FOREIGN KEY (`server`) REFERENCES `DBLog_Servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void createDbLogTickRates() throws SQLException {
        LOGGER.info("Creating table DBLog_Tickrates");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS DBLog_Tickrates (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "time DATETIME," +
                "tickRate DOUBLE NOT NULL," +
                "server INT NOT NULL," +
                "`match` INT DEFAULT NULL," +
                "KEY `server` (`server`)," +
                "KEY `match` (`match`)," +
                "CONSTRAINT `squad4j_dblog_tickrates_fk_server` FOREIGN KEY (`server`) REFERENCES `DBLog_Servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_tickrates_fk_match` FOREIGN KEY (`match`) REFERENCES `DBLog_Matches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void createDbLogPlayerCounts() throws SQLException {
        LOGGER.info("Creating table DBLog_PlayerCounts");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS DBLog_PlayerCounts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "time DATETIME NOT NULL," +
                "players INT NOT NULL DEFAULT 0," +
                "publicQueue INT NOT NULL DEFAULT 0," +
                "reserveQueue INT NOT NULL DEFAULT 0," +
                "server INT NOT NULL," +
                "`match` INT DEFAULT NULL," +
                "KEY `server` (`server`)," +
                "KEY `match` (`match`)," +
                "CONSTRAINT `squad4j_dblog_playercounts_fk_server` FOREIGN KEY (`server`) REFERENCES `DBLog_Servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_playercoutns_fk_match` FOREIGN KEY (`match`) REFERENCES `DBLog_Matches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE) " +
                "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void createDbLogWounds() throws SQLException {
        LOGGER.info("Creating table DBLog_Wounds");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS DBLog_Wounds(" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "`time` DATETIME NOT NULL," +
                "victimName VARCHAR(255)," +
                "victimTeamID INT," +
                "victimSquadID INT," +
                "attackerName VARCHAR(255)," +
                "attackerTeamID INT," +
                "attackerSquadID INT," +
                "damage DOUBLE," +
                "teamkill BOOLEAN," +
                "server INT NOT NULL," +
                "attacker VARCHAR(255) DEFAULT NULL," +
                "victim VARCHAR(255) DEFAULT NULL," +
                "`match` INT DEFAULT NULL," +
                "KEY `server` (`server`)," +
                "KEY `attacker` (`attacker`)," +
                "KEY `victim` (`victim`)," +
                "KEY `match` (`match`)," +
                "CONSTRAINT `squad4j_dblog_wounds_fk_server` FOREIGN KEY (`server`) REFERENCES `DBLog_Servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_wounds_fk_attacker` FOREIGN KEY (`attacker`) REFERENCES old_players (`steamID`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_wounds_fk_victim` FOREIGN KEY (`victim`) REFERENCES old_players (`steamID`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_wounds_fk_match` FOREIGN KEY (`match`) REFERENCES `DBLog_Matches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void createDbLogDeaths() throws SQLException {
        LOGGER.info("Creating table DBLog_Deaths");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS DBLog_Deaths(" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "`time` DATETIME NOT NULL," +
                "woundTime DATE," +
                "victimName VARCHAR(255)," +
                "victimTeamID INT," +
                "victimSquadID INT," +
                "attackerName VARCHAR(255)," +
                "attackerTeamID INT," +
                "attackerSquadID INT," +
                "damage DOUBLE," +
                "weapon VARCHAR(255)," +
                "teamkill BOOLEAN," +
                "server INT NOT NULL," +
                "attacker VARCHAR(255) DEFAULT NULL," +
                "victim VARCHAR(255) DEFAULT NULL," +
                "`match` INT DEFAULT NULL," +
                "KEY `server` (`server`)," +
                "KEY `attacker` (`attacker`)," +
                "KEY `victim` (`victim`)," +
                "KEY `match` (`match`)," +
                "CONSTRAINT `squad4j_dblog_deaths_fk_server` FOREIGN KEY (`server`) REFERENCES `DBLog_Servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_deaths_fk_attacker` FOREIGN KEY (`attacker`) REFERENCES old_players (`steamID`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_deaths_fk_victim` FOREIGN KEY (`victim`) REFERENCES old_players (`steamID`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_deaths_fk_match` FOREIGN KEY (`match`) REFERENCES `DBLog_Matches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void createDbLogRevives() throws SQLException {
        LOGGER.info("Creating table DBLog_Revives");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS DBLog_Revives(" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "`time` DATETIME NOT NULL," +
                "woundTime DATE," +
                "victimName VARCHAR(255)," +
                "victimTeamID INT," +
                "victimSquadID INT," +
                "attackerName VARCHAR(255)," +
                "attackerTeamID INT," +
                "attackerSquadID INT," +
                "damage DOUBLE," +
                "weapon VARCHAR(255)," +
                "teamkill BOOLEAN," +
                "reviverName VARCHAR(255)," +
                "reviverTeamID INT," +
                "reviverSQuadID INT," +
                "server INT NOT NULL," +
                "attacker varchar(255) DEFAULT NULL," +
                "victim varchar(255) DEFAULT NULL," +
                "reviver varchar(255) DEFAULT NULL," +
                "`match` int DEFAULT NULL," +
                "KEY `server` (`server`)," +
                "KEY `attacker` (`attacker`)," +
                "KEY `victim` (`victim`)," +
                "KEY `reviver` (`reviver`)," +
                "KEY `match` (`match`)," +
                "CONSTRAINT `squad4j_dblog_revives_fk_server` FOREIGN KEY (`server`) REFERENCES `DBLog_Servers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_revives_fk_attacker` FOREIGN KEY (`attacker`) REFERENCES old_players (`steamID`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_revives_fk_victim` FOREIGN KEY (`victim`) REFERENCES old_players (`steamID`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_revives_fk_reviver` FOREIGN KEY (`reviver`) REFERENCES old_players (`steamID`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "CONSTRAINT `squad4j_dblog_revives_fk_match` FOREIGN KEY (`match`) REFERENCES `DBLog_Matches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }*/

    private static void createAdmin() throws SQLException {
        LOGGER.info("Creating table Admins");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS admins(" +
                "steamId BIGINT NOT NULL PRIMARY KEY," +
                "name VARCHAR(16) NOT NULL," +
                "steamSign VARCHAR(32) null," +
                "role INT NOT NULL," +
                "avatar VARCHAR(255) null," +
                "avatarMedium VARCHAR(255) null," +
                "avatarFull VARCHAR(255) null," +
                "session CHAR(36) null," +
                "createTime DATETIME DEFAULT NOW() NOT NULL," +
                "modifiedTime DATETIME DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP NOT NULL," +
                "CONSTRAINT steam_id_UNIQUE UNIQUE (steamId)," +
                "CONSTRAINT adminSessionId FOREIGN KEY (session) REFERENCES SPRING_SESSION (PRIMARY_ID));");
    }

    public static void createAdminActionLog() throws SQLException {
        LOGGER.info("Creating table Admin_Action_Log");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS admins_action_log (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "admin BIGINT NOT NULL, " +
                "player BIGINT NULL," +
                "action VARCHAR(100) NOT NULL, " +
                "reason VARCHAR(255) NULL, " +
                "createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                "CONSTRAINT logAdminId FOREIGN KEY (admin) REFERENCES admins (steamId)," +
                "CONSTRAINT logPlayerId FOREIGN KEY (player) REFERENCES players (steamId));");
    }

    private static void createPlayers() throws SQLException {
        LOGGER.info("Creating table Players");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                "steamId BIGINT PRIMARY KEY," +
                "name VARCHAR(255)," +
                "onControl BOOL DEFAULT FALSE NOT NULL," +
                "createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL)");
    }

    private static void createPlayersBans() throws SQLException {
        LOGGER.info("Creating table Players_Bans");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS players_bans (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player BIGINT NOT NULL," +
                "admin BIGINT NOT NULL," +
                "reason VARCHAR(255) NULL," +
                "isUnbannedManually BOOL DEFAULT false NOT NULL, " +
                "unbannedAdmin BIGINT NULL," +
                "unbannedTime DATETIME NULL," +
                "expirationTime DATETIME NULL," +
                "creationTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "CONSTRAINT bansSteamId FOREIGN KEY (player) REFERENCES players (steamId)," +
                "CONSTRAINT bansAdminId FOREIGN KEY (admin) REFERENCES admins (steamId)," +
                "CONSTRAINT unbanAdminId FOREIGN KEY (unbannedAdmin) REFERENCES admins(steamId));");
    }

    private static void createPlayersNotes() throws SQLException {
        LOGGER.info("Creating table Players_Notes");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS players_notes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player BIGINT NOT NULL," +
                "admin BIGINT NOT NULL," +
                "note VARCHAR(255) NOT NULL," +
                "creationTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "CONSTRAINT notesSteamId FOREIGN KEY (player) REFERENCES players (steamId)," +
                "CONSTRAINT notesAdminId FOREIGN KEY (admin) REFERENCES admins (steamId));");
    }

    private static void createPlayersMessages() throws SQLException {
        LOGGER.info("Creating table Players_Messages");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS players_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player BIGINT NOT NULL," +
                "chatType VARCHAR(16) NOT NULL," +
                "message VARCHAR(255) NOT NULL," +
                "creationTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "CONSTRAINT messagesSteamId FOREIGN KEY (player) REFERENCES players (steamId));");
    }

    private static void createPlayersKicks() throws SQLException {
        LOGGER.info("Creating table Players_Kicks");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS players_kicks (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player BIGINT NOT NULL," +
                "admin BIGINT NOT NULL," +
                "reason VARCHAR(255) NULL," +
                "creationTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "CONSTRAINT kicksSteamId FOREIGN KEY (player) REFERENCES players (steamId)," +
                "CONSTRAINT kicksAdminId FOREIGN KEY (admin) REFERENCES admins (steamId));");
    }

    private static void createLayersHistory() throws SQLException {
        LOGGER.info("Creating table layers_history");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS layers_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "layer VARCHAR(255) NOT NULL," +
                "creationTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL)");
    }

    private static void createRuleGroups() throws SQLException {
        LOGGER.info("Creating table rule_groups");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS rule_groups (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "position INT NOT NULL, " +
                "name VARCHAR(1000) NOT NULL)");
    }

    private static void createRules() throws SQLException {
        LOGGER.info("Creating table rule");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS rules (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "position INT NOT NULL, " +
                "name VARCHAR(1000) NOT NULL, " +
                "ruleGroup int NOT NULL, " +
                "CONSTRAINT rulesGroupId FOREIGN KEY (ruleGroup) REFERENCES rule_groups (id) ON DELETE CASCADE);");
    }

    public static void createSpringSessions() throws SQLException {
        LOGGER.info("Creating table Spring_session and spring_session_attributes");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS SPRING_SESSION ( " +
                " PRIMARY_ID CHAR(36) NOT NULL, " +
                " SESSION_ID CHAR(36) NOT NULL, " +
                " CREATION_TIME BIGINT NOT NULL, " +
                " LAST_ACCESS_TIME BIGINT NOT NULL, " +
                " MAX_INACTIVE_INTERVAL INT NOT NULL, " +
                " EXPIRY_TIME BIGINT NOT NULL, " +
                " PRINCIPAL_NAME VARCHAR(100), " +
                " CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID) " +
                ") ENGINE=InnoDB ROW_FORMAT=DYNAMIC;");
        statement.executeUpdate("CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);");
        statement.executeUpdate("CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);");
        statement.executeUpdate("CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS  SPRING_SESSION_ATTRIBUTES ( " +
                " SESSION_PRIMARY_ID CHAR(36) NOT NULL, " +
                " ATTRIBUTE_NAME VARCHAR(200) NOT NULL, " +
                " ATTRIBUTE_BYTES BLOB NOT NULL, " +
                " CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME), " +
                " CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE " +
                ") ENGINE=InnoDB ROW_FORMAT=DYNAMIC;");
    }

    /*private static void createServer() {
        //TODO: If query times out, info is null. Fix
        A2SInfoResponse info = Query.queryInfo();
        String serverName = info.getName();
        try {
            String query = "INSERT INTO DBLog_Servers (id, server) VALUES(" +
                    serverID + ", " +
                    "'" + serverName + "'" +
                    ");";
            LOGGER.trace(query);
            statement.executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.error("SQL exception while creating server.", e);
        }
    }*/

    /*public static Integer getCurrentMatchId() {
        try {
            String query = String.format("SELECT `id`, `layer` FROM DBLog_Matches WHERE `server` = %s AND `endTime` IS NULL ORDER BY `id` DESC LIMIT 1;", serverID);
            if (statement == null) {
                LOGGER.warn("Cannot get current match ID yet because MySQLConnector hasn't been initialized yet.");
                return -1;
            }
            ResultSet rs = statement.executeQuery(query);

            String currentMap = SquadServer.getCurrentMap();
            String currentLayer = SquadServer.getCurrentLayer();

            if (rs.first() && currentLayer != null && currentLayer.equals(rs.getString("layer"))) {
                int matchId = rs.getInt("id");
                rs.close();
                //Current layer matches most recent match in DB, go ahead and return it.
                return matchId;
            }
            rs.close();
            LOGGER.debug("Most recent match in DB does not match current or does not exist, creating new match.");
            //Insert new match since current match is not being tracked
            //TODO: Fill dlc and classname values properly
            insertMatch("",
                    LayerClassnameFormatter.formatMap(currentMap),
                    LayerClassnameFormatter.formatLayer(currentLayer),
                    currentMap,
                    currentLayer,
                    null);
            //Recalling this method will get the newly-made match id
            return getCurrentMatchId();
        } catch (SQLException e) {
            LOGGER.error("SQL exception while getting current match.", e);
            return null;
        }
    }

    public static void insertPlayerCount(Date time, Integer players, Integer publicQueue, Integer reserveQueue, Integer match) {
        try {
            String query = "INSERT INTO DBLog_PlayerCounts (`time`, `players`, `publicQueue`, `reserveQueue`, `server`, `match`) VALUES(" +
                    "\'" + dateTimeFormat.format(time) + "\'," +
                    players + "," +
                    publicQueue + "," +
                    reserveQueue + "," +
                    serverID + "," +
                    (match == null ? "NULL" : match) +
                    ");";
            LOGGER.trace(query);
            statement.executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.error("SQL exception inserting player count.", e);
        }
    }

    public static void insertMatch(String dlc, String mapClassname, String layerClassname, String map, String layer, Date startTime) {
        try {
            String query = "INSERT INTO DBLog_Matches (dlc, mapClassname, layerClassname, map, layer, startTime, endTime, winner, `server`) VALUES (" +
                    "\'" + dlc + "\', " +
                    "\'" + mapClassname + "\', " +
                    "\'" + layerClassname + "\', " +
                    "\'" + map + "\', +" +
                    "\'" + layer + "\', " +
                    (startTime == null ? "NULL" : dateTimeFormat.format(startTime)) + ", " +
                    "NULL, " +
                    "NULL, " +
                    serverID +
                    ");";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.error("SQL exception inserting match.", e);
        }
    }

    public static void updateMatch(Date time, String winningFaction) {

    }

    public static void insertTickRate(Date time, Double tickRate, Integer match) {
        try {
            String query = "INSERT INTO DBLog_Tickrates (`time`, `tickRate`, `server`, `match`) VALUES" +
                    "(" +
                    "\'" + dateTimeFormat.format(time) + "\'" + ", " +
                    tickRate + ", " +
                    serverID + ", " +
                    match +
                    ");";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.error("Error inserting tick rate.", e);
        }
    }

    public static Boolean isUserExist(String steamId) {
        try {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM admins WHERE steamId=" + steamId);
            if (rs.first()) {
                return rs.getInt(0) > 0;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.error("SQL exception while check admin by steamID.", e);
            return null;
        }
    }

    public static Boolean isUserLogin(String steamId) {
        try {
            ResultSet rs = statement.executeQuery("SELECT steamSign FROM admins WHERE steamId=" + steamId);
            if (rs.first()) {
                return rs.getString(3) != null && !rs.getString(3).isEmpty();
            }
            return false;
        } catch (SQLException e) {
            LOGGER.error("SQL exception while check admin by steamID.", e);
            return null;
        }
    }

    public static void addUserSteamSign(String steamId, String steamSign) {
        try {
            statement.executeQuery("UPDATE admins SET steamSign= " + steamSign + " WHERE steamId=" + steamId);
        } catch (SQLException e) {
            LOGGER.error("SQL exception while check admin by steamID.", e);
        }
    }*/
}
