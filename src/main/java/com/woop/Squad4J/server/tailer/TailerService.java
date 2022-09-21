package com.woop.Squad4J.server.tailer;

import com.woop.Squad4J.server.A2SUpdater;
import com.woop.Squad4J.util.ConfigLoader;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TailerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TailerService.class);
    private static boolean initialized = false;
    private static final String FTP_HOST = ConfigLoader.get("server.host", String.class);
    private static final int FTP_PORT = ConfigLoader.get("server.ftp.port", Integer.class);
    private static final String FTP_USER = ConfigLoader.get("server.ftp.user", String.class);
    private static final String FTP_PASSWORD = ConfigLoader.get("server.ftp.password", String.class);
    private static final String FTP_LOG_ABSOLUTE_PATH = ConfigLoader.get("server.logAbsolutePath", String.class);
    private static final String FTP_ENCODING = "ISO-8859-1";
    private static final String FILE_NAME = "SquadGame.log";

    private TailerService() {
        throw new IllegalStateException("This class cannot be instantiated.");
    }

    public static void init() {
        if (initialized) {
            throw new IllegalStateException(A2SUpdater.class.getSimpleName() + " has already been initialized.");
        }
        LOGGER.debug("File Path in Config: {}", FTP_LOG_ABSOLUTE_PATH);
        switch (ConfigLoader.get("server.logReaderMode", String.class).toLowerCase()) {
            case "ftp" -> initFtp();
            case "local" -> initLocal();
            default -> {
                LOGGER.error("Unknown value in config.json 'logReaderMode':" + ConfigLoader.get("server.logReaderMode", String.class));
                System.exit(1);
            }
        }
    }

    public static void initLocal() {
        //Configure log file tailer
        TailerListener listener = new LogTailer();
        File configFile = null;
        if (FTP_LOG_ABSOLUTE_PATH != null) {
            configFile = new File(FTP_LOG_ABSOLUTE_PATH);
        } else {
            LOGGER.error("There was an error reading the file path to the server log file. See above.");
            System.exit(1);
        }
        Tailer tailer = new Tailer(configFile,
                listener,
                50,
                true,
                false,
                10000);
        String absolutePath = configFile.getAbsolutePath();
        LOGGER.info("Watching logfile {}", absolutePath);
        new Thread(tailer).start();

        initialized = true;
        LOGGER.info("Log tailer service initialized");
    }

    public static void initFtp() {
        TailerListener tailerListener = new LogTailer();
        FtpLogTailer ftpLogTailerListener = new FtpLogTailer(tailerListener,
                FTP_HOST,
                FTP_PORT,
                FTP_USER,
                FTP_PASSWORD,
                FTP_LOG_ABSOLUTE_PATH,
                FILE_NAME,
                FTP_ENCODING,
                2000);

        LOGGER.info("Watching logfile {} on FTP {} with path {}", FILE_NAME, FTP_HOST, FTP_LOG_ABSOLUTE_PATH);
        new Thread(ftpLogTailerListener).start();

        initialized = true;
        LOGGER.info("Log tailer service initialized");
    }
}
