package com.woop.Squad4J.server.tailer;

import org.apache.commons.io.input.TailerListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ntp.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.SocketException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

public class FtpLogTailer implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(FtpLogTailer.class);

    private final TailerListener TAILER_LISTENER;
    private final String HOST;
    private final int PORT;
    private final String USERNAME;
    private final String PASSWORD;
    private final String PATH;
    private final String FILE_NAME;
    private final String ENCODING;
    private final long DELAY_IN_MILLIS;
    private long lastByteRead = 0;
    private String lastRowRead;
    private volatile boolean run;
    public static TimeStamp lastSuccessfullyWork = new TimeStamp(System.currentTimeMillis());

    public FtpLogTailer(TailerListener tailerListener, String host, int port, String userName, String password, String path, String fileName, String encoding, long delayInMillis) {
        HOST = host;
        PORT = port;
        USERNAME = userName;
        PASSWORD = password;
        PATH = path;
        FILE_NAME = fileName;
        ENCODING = encoding;
        DELAY_IN_MILLIS = delayInMillis;
        TAILER_LISTENER = tailerListener;
    }

    @Override
    public void run() {
        run = true;
        FTPClient ftpClient = connectFtpServer(HOST, PORT, USERNAME, PASSWORD, ENCODING, BINARY_FILE_TYPE);

        lastByteRead = getFileSize(ftpClient);

        while (run) {
            try {
                long fileSize = getFileSize(ftpClient);
                if (lastByteRead > fileSize) {
                    lastByteRead = findLastByteReadByLastRowRead(ftpClient);
                }
                if (isFileChange(ftpClient)) {
                    getFileRows(ftpClient).forEach(TAILER_LISTENER::handle);
                }
                LOGGER.info("FTP log file updated");
                lastSuccessfullyWork = new TimeStamp(System.currentTimeMillis());
                Thread.sleep(this.DELAY_IN_MILLIS);
            } catch (Exception e) {
                LOGGER.error("Error while tailing FTP", e);
                ftpClient = reconnect(ftpClient);
            }
        }
        closeFTPConnect(ftpClient);
        LOGGER.info("FTP connection closed");
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
            if (!ftpClient.changeWorkingDirectory(PATH)) {
                throw new IllegalArgumentException("Cant change working directory in FTP");
            }
        } catch (Exception e) {
            LOGGER.error("Exception while trying set working FTP directory " + PATH, e);
            throw new RuntimeException(e);
        }
        LOGGER.info("FTP connected");
        return ftpClient;
    }

    private void closeFTPConnect(FTPClient ftpClient) {
        try {
            LOGGER.info("Closing FTP");
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.abort();
                ftpClient.disconnect();
            }
            LOGGER.info("FTP closed");
        } catch (Exception e) {
            LOGGER.error("Failed to close FTP connection");
        }
    }


    private FTPClient reconnect(FTPClient ftpClient) {
        LOGGER.warn("Reconnect to FTP to tailing log file");
        closeFTPConnect(ftpClient);
        return connectFtpServer(HOST, PORT, USERNAME, PASSWORD, ENCODING, BINARY_FILE_TYPE);
    }

    private boolean isFileChange(FTPClient ftpClient) {
        return getFileSize(ftpClient) != lastByteRead;
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

    private List<String> getFileRows(FTPClient ftpClient) {
        try {
            ftpClient.setRestartOffset(lastByteRead);
            InputStream inputStream = ftpClient.retrieveFileStream(FILE_NAME);
            byte[] receivedBytes = inputStream.readAllBytes();
            inputStream.close();
            ftpClient.completePendingCommand();
            LinkedList<String> rows = new LinkedList<>(Arrays.stream(new String(receivedBytes).split("\r\n")).toList());
            lastByteRead += receivedBytes.length;
            lastRowRead = rows.getLast();
            return rows;
        } catch (Exception e) {
            LOGGER.error("Failed to get FTP file '" + FILE_NAME + "' rows");
            throw new RuntimeException(e);
        }
    }

    private long findLastByteReadByLastRowRead(FTPClient ftpClient) {
        try {
            InputStream inputStream = ftpClient.retrieveFileStream(FILE_NAME);
            String fileText = new String(inputStream.readAllBytes());
            inputStream.close();
            ftpClient.completePendingCommand();
            return fileText.indexOf(lastRowRead + "\r\n") + (lastRowRead + "\r\n").getBytes().length;
        } catch (Exception e) {
            LOGGER.error("Failed to get FTP file '" + FILE_NAME + "' rows");
            throw new RuntimeException(e);
        }
    }
}