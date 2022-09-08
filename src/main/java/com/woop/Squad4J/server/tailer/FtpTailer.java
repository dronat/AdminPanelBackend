package com.woop.Squad4J.server.tailer;

import org.apache.commons.io.input.TailerListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

public class FtpTailer implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(FtpTailer.class);
    private final String HOST;
    private final int PORT;
    private final String USERNAME;
    private final String PASSWORD;
    private final String PATH;
    private final String FILE_NAME;
    private final String ENCODING;
    private final long DELAY_IN_MILLIS;

    private volatile boolean run;
    private long lastByteRead = 0;
    private final TailerListener TAILER_LISTENER;

    public FtpTailer(TailerListener tailerListener, String host, int port, String userName, String password, String path, String fileName, String encoding, long delayInMillis) {
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
        ftpClient.setControlKeepAliveTimeout(300);
        ftpClient.enterLocalPassiveMode();
        try {
            ftpClient.changeWorkingDirectory(PATH);
        } catch (Exception e) {
            LOGGER.error("Exception while trying set working FTP directory " + PATH, e);
            throw new RuntimeException(e);
        }

        lastByteRead = getFileSize(ftpClient);

        try {
            while(run) {
                if (isFileChange(ftpClient)) {
                    getFileRows(ftpClient).forEach(TAILER_LISTENER::handle);
                }
                Thread.sleep(this.DELAY_IN_MILLIS);
            }
        } catch (Exception e) {
            LOGGER.error("Error while tailing FTP", e);
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

    private boolean isFileChange(FTPClient ftpClient) {
        return getFileSize(ftpClient) != lastByteRead;
    }

    private long getFileSize(FTPClient ftpClient) {
        FTPFile file;
        try {
            file = ftpClient.listFiles(FILE_NAME)[0];
        } catch (Exception e) {
            LOGGER.error("Failed to get FTP file size");
            throw new RuntimeException(e);
        }
        return file.getSize();
    }

    private List<String> getFileRows(FTPClient ftpClient) {
        try {
            InputStream inputStream = ftpClient.retrieveFileStream(FILE_NAME);
            inputStream.skipNBytes(lastByteRead);
            byte[] receivedBytes = inputStream.readAllBytes();
            inputStream.close();
            ftpClient.completePendingCommand();
            lastByteRead += receivedBytes.length;
            return Arrays.stream(new String(receivedBytes).split("\r\n")).toList();
        } catch (Exception e) {
            LOGGER.error("Failed to get FTP file rows");
            throw new RuntimeException(e);
        }
    }
}