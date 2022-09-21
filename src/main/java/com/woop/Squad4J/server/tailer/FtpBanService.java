package com.woop.Squad4J.server.tailer;

import com.example.adminpanelbackend.dataBase.EntityManager;
import com.woop.Squad4J.util.ConfigLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

public class FtpBanService implements Runnable{
    private final Logger LOGGER = LoggerFactory.getLogger(FtpBanService.class);
    private final String FTP_HOST = ConfigLoader.get("server.host", String.class);
    private final int FTP_PORT = ConfigLoader.get("server.ftp.port", Integer.class);
    private final String FTP_USERNAME = ConfigLoader.get("server.ftp.user", String.class);
    private final String FTP_PASSWORD = ConfigLoader.get("server.ftp.password", String.class);
    private final String FTP_BAN_ABSOLUTE_PATH = ConfigLoader.get("server.banAbsolutePath", String.class);
    private final String FTP_ENCODING = "UTF-8";
    private final String FILE_NAME = "Bans_test.cfg";
    private final long DELAY_IN_MILLIS = 30 * 60000;
    private volatile boolean run;

    @Override
    public void run() {
        run = true;
        EntityManager entityManager = new EntityManager();
        FTPClient ftpClient = connectFtpServer(FTP_HOST, FTP_PORT, FTP_USERNAME, FTP_PASSWORD, FTP_ENCODING, BINARY_FILE_TYPE);

        try {
            ftpClient.changeWorkingDirectory(FTP_BAN_ABSOLUTE_PATH);
        } catch (Exception e) {
            LOGGER.error("Exception while trying set working FTP directory " + FTP_BAN_ABSOLUTE_PATH, e);
            throw new RuntimeException(e);
        }

        while (run) {
            try {
                StringBuilder stringToWrite = new StringBuilder();
                entityManager.getActiveBans().forEach(playerBanEntity ->
                        stringToWrite//\CoRe/ DolbaDigitale [SteamID 76561198054690038] Banned:76561199037059865:1649616436 //sl osk
                                .append(playerBanEntity.getAdminsBySteamId().getName())
                                .append(" [SteamID ")
                                .append(playerBanEntity.getAdminsBySteamId().getSteamId())
                                .append("] Banned:")
                                .append(playerBanEntity.getPlayersBySteamId().getSteamId())
                                .append(":0 //")
                                .append(playerBanEntity.getReason())
                                .append("\n")
                );
                rewriteFile(ftpClient, stringToWrite.toString());
                LOGGER.info("FTP bans file updated");
                Thread.sleep(DELAY_IN_MILLIS);
            } catch (Exception e) {
                LOGGER.error("Error while rewrite FTP file " + FILE_NAME, e);
            }
        }
        closeFTPConnect(ftpClient);
        LOGGER.info("FTP connection closed");
    }

    public void stop() {
        this.run = false;
    }

    private FTPClient connectFtpServer(String addr, int port, String username, String password, String controlEncoding, int fileType) {
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
            ftpClient.setFileType(fileType);
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
        ftpClient.setControlKeepAliveTimeout(300);
        ftpClient.enterLocalPassiveMode();
        return ftpClient;
    }

    private void closeFTPConnect(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.abort();
                ftpClient.disconnect();
            }
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
        try (InputStream inputStream = IOUtils.toInputStream(fileContent, FTP_ENCODING)) {
            result = ftpClient.storeFile(FILE_NAME, inputStream);
        } catch (Exception e) {
            LOGGER.error("Failed to rewrite FTP file '" + FILE_NAME + "'");
            throw new RuntimeException(e);
        }
        if (!result) {
            LOGGER.error("Failed to rewrite FTP file '" + FILE_NAME + "'");
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
