package com.woop.Squad4J.server.tailer;

import com.example.adminpanelbackend.db.EntityManager;
import com.example.adminpanelbackend.db.entity.PlayerBanEntity;
import com.example.adminpanelbackend.discord.Discord;
import com.example.adminpanelbackend.discord.DiscordMessageDTO;
import com.woop.Squad4J.util.ConfigLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.SocketException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

public class FtpBanService implements Runnable {
    public static Timestamp lastSuccessfullyWork = new Timestamp(System.currentTimeMillis());
    private final Logger LOGGER = LoggerFactory.getLogger(FtpBanService.class);
    private final String HOST = ConfigLoader.get("server.host", String.class);
    private final int PORT = ConfigLoader.get("server.ftp.port", Integer.class);
    private final String USERNAME = ConfigLoader.get("server.ftp.user", String.class);
    private final String PASSWORD = ConfigLoader.get("server.ftp.password", String.class);
    private final String ABSOLUTE_FILE_PATH = ConfigLoader.get("server.banAbsolutePath", String.class);
    private final Boolean USE_DISCORD_BANS = ConfigLoader.get("server.useDiscordBans", Boolean.class);
    private final String ENCODING = "UTF-8";
    private final String FILE_NAME = "Bans.cfg";
    private final long DELAY_IN_MILLIS = 60000;
    private Discord discord;
    private volatile boolean run;

    @Override
    public void run() {
        run = true;
        EntityManager entityManager = new EntityManager();
        FTPClient ftpClient = connectFtpServer(HOST, PORT, USERNAME, PASSWORD, ENCODING, BINARY_FILE_TYPE);
        if (USE_DISCORD_BANS) {
            discord = new Discord();
            discord.init();
        }

        while (run) {
            try {
                StringBuilder stringToWrite = new StringBuilder();
                entityManager.getActiveBans().forEach(playerBanEntity ->
                        stringToWrite//\CoRe/ DolbaDigitale [SteamID 76561198054690038] Banned:76561199037059865:1649616436 //sl osk
                                .append(playerBanEntity.getAdmin().getName())
                                .append(" [SteamID ")
                                .append(playerBanEntity.getAdmin().getSteamId())
                                .append("] Banned:")
                                .append(playerBanEntity.getPlayer().getSteamId())
                                .append(":0 //")
                                .append(playerBanEntity.getReason())
                                .append("\n")
                );
                rewriteFile(ftpClient, stringToWrite.toString());
                LOGGER.info("FTP bans file updated");
                lastSuccessfullyWork = new Timestamp(System.currentTimeMillis());

                if (USE_DISCORD_BANS) {
                    List<PlayerBanEntity> activeNonPermanentBans = entityManager.getActiveNonPermanentBans();
                    if (activeNonPermanentBans == null || activeNonPermanentBans.isEmpty()) {
                        discord.sendNoActiveBans();
                    } else {
                        DiscordMessageDTO discordMessage = new DiscordMessageDTO();
                        activeNonPermanentBans.forEach(playerBanEntity ->
                                discordMessage.addEmbded(
                                        new DiscordMessageDTO.Embed()
                                                .setTitle(playerBanEntity.getPlayer().getName() + " (" + playerBanEntity.getPlayer().getSteamId() + ")")
                                                .setColor(15548997)
                                                .setFields(
                                                        List.of(
                                                                new DiscordMessageDTO.Field()
                                                                        .setName("Причина бана")
                                                                        .setValue(playerBanEntity.getReason()),
                                                                new DiscordMessageDTO.InlineField()
                                                                        .setInline(true)
                                                                        .setName("Дата бана")
                                                                        .setValue(getBanCreationTime(playerBanEntity.getCreationTime())),
                                                                new DiscordMessageDTO.InlineField()
                                                                        .setInline(true)
                                                                        .setName("Истечет")
                                                                        .setValue(getBanExpirationTime(playerBanEntity.getExpirationTime())),
                                                                new DiscordMessageDTO.InlineField()
                                                                        .setInline(true)
                                                                        .setName("Админ")
                                                                        .setValue(playerBanEntity.getAdmin().getName())
                                                        )
                                                )
                                )
                        );
                        discord.actualizeBanMessage(discordMessage);
                    }
                }

                Thread.sleep(DELAY_IN_MILLIS);
            } catch (Exception e) {
                LOGGER.error("Error while rewrite FTP file " + FILE_NAME, e);
                reconnect(ftpClient);
            }
        }
        closeFTPConnect(ftpClient);
        LOGGER.info("FTP connection closed");
    }

    private String getBanCreationTime(Timestamp timestamp) {
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(date);
    }

    private String getBanExpirationTime(Timestamp timestamp) {
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy в HH:mm");
        return sdf.format(date);
    }

    public void stop() {
        this.run = false;
    }

    private FTPClient connectFtpServer(String addr, int port, String username, String password, String controlEncoding, int fileType) {
        LOGGER.info("Connecting to FTP");
        FTPClient ftpClient = new FTPClient();

        ftpClient.setControlEncoding(controlEncoding);

        try {
            ftpClient.connect(addr, port);
        } catch (Exception e) {
            LOGGER.error("Exception while connecting to FTP " + addr + port, e);
            throw new RuntimeException(e);
        }

        try {
            if (!ftpClient.login(username, password)) {
                LOGGER.error("Wrong credentials to connect FTP server");
                throw new RuntimeException();
            }
        } catch (Exception e) {
            LOGGER.error("Exception while login on FTP " + addr + port + " with user " + username + " and pass" + password, e);
            throw new RuntimeException(e);
        }

        try {
            if (!ftpClient.setFileType(fileType)) {
                throw new IllegalArgumentException("Cant set file type in FTP");
            }
        } catch (Exception e) {
            LOGGER.error("Exception while trying set BINARY_FILE_TYPE on FTP " + addr + port, e);
            throw new RuntimeException(e);
        }

        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            try {
                ftpClient.abort();
                ftpClient.disconnect();
            } catch (Exception e) {
                LOGGER.error("Exception while disconnect from FTP " + addr + port, e);
            }
            LOGGER.error("FTP return reply code " + reply);
            throw new RuntimeException();
        }
        try {
            ftpClient.setControlKeepAliveTimeout(1);
            ftpClient.setControlKeepAliveReplyTimeout(5000);
            ftpClient.setConnectTimeout(5000);
            ftpClient.setSoTimeout(5000);
            ftpClient.setDataTimeout(10000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        ftpClient.enterLocalPassiveMode();
        try {
            if (!ftpClient.changeWorkingDirectory(ABSOLUTE_FILE_PATH)) {
                throw new IllegalArgumentException("Cant change working directory in FTP");
            }
        } catch (Exception e) {
            LOGGER.error("Exception while trying set working FTP directory " + ABSOLUTE_FILE_PATH, e);
            throw new RuntimeException(e);
        }
        LOGGER.info("FTP connected");
        return ftpClient;
    }

    private FTPClient reconnect(FTPClient ftpClient) {
        LOGGER.warn("Reconnect to FTP to tailing ban file");
        closeFTPConnect(ftpClient);
        return connectFtpServer(HOST, PORT, USERNAME, PASSWORD, ENCODING, BINARY_FILE_TYPE);
    }

    private void closeFTPConnect(FTPClient ftpClient) {
        try {
            LOGGER.info("Closing FTP");
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.abort();
                } catch (Exception ignored) {
                }
                try {
                    ftpClient.disconnect();
                } catch (Exception e) {
                    throw e;
                }
            }
            LOGGER.info("FTP closed");
        } catch (Exception e) {
            LOGGER.error("Failed to close FTP connection");
        }
    }

    private List<String> getAllFileRows(FTPClient ftpClient) {
        try {
            InputStream inputStream = ftpClient.retrieveFileStream(FILE_NAME);
            byte[] receivedBytes = inputStream.readAllBytes();
            inputStream.close();
            ftpClient.completePendingCommand();
            LinkedList<String> rows = new LinkedList<>(Arrays.stream(new String(receivedBytes).split("\r\n")).toList());
            return rows;
        } catch (Exception e) {
            LOGGER.error("Failed to get FTP file '" + FILE_NAME + "' rows");
            throw new RuntimeException(e);
        }
    }

    private String getFileAsString(FTPClient ftpClient) {
        try {
            InputStream inputStream = ftpClient.retrieveFileStream(FILE_NAME);
            byte[] receivedBytes = inputStream.readAllBytes();
            inputStream.close();
            ftpClient.completePendingCommand();
            String resultString = new String(receivedBytes);
            return resultString;
        } catch (Exception e) {
            LOGGER.error("Failed to get FTP file '" + FILE_NAME + "' rows");
            throw new RuntimeException(e);
        }
    }

    private void rewriteFile(FTPClient ftpClient, String fileContent) {
        boolean result;
        try (InputStream inputStream = IOUtils.toInputStream(fileContent, ENCODING)) {
            result = ftpClient.storeFile(FILE_NAME, inputStream);
        } catch (Exception e) {
            LOGGER.error("Failed to rewrite FTP file " + FILE_NAME);
            throw new RuntimeException(e);
        }
        if (!result) {
            LOGGER.error("Failed to rewrite FTP file " + FILE_NAME);
            throw new RuntimeException();
        }
    }

    private long getFileSize(FTPClient ftpClient) {
        FTPFile file;
        try {
            file = ftpClient.listFiles(FILE_NAME)[0];
        } catch (Exception e) {
            LOGGER.error("Failed to get FTP file " + FILE_NAME + " size");
            throw new RuntimeException(e);
        }
        return file.getSize();
    }
}
