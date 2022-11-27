package com.example.adminpanelbackend;


import com.example.adminpanelbackend.dataBase.core.EmfThreadLocal;
import com.woop.Squad4J.concurrent.GlobalThreadPool;
import com.woop.Squad4J.main.SquadModule;
import com.woop.Squad4J.rcon.RconImpl;
import com.woop.Squad4J.server.tailer.FtpBanService;
import com.woop.Squad4J.server.tailer.FtpLogTailer;
import com.woop.Squad4J.util.ConfigLoader;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Objects;

@SpringBootApplication//(exclude = {DataSourceAutoConfiguration.class })
public class AdminPanelBackendApplication {
    static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        SquadModule.init();

        SpringApplication application = new SpringApplication(AdminPanelBackendApplication.class);
        application.setDefaultProperties(new HashMap<>() {{
            put("server.port", ConfigLoader.get("server.adminPanelPort"));
            put("spring.datasource.url", "jdbc:mysql://" + ConfigLoader.get("connectors.mysql.host") + ":" + ConfigLoader.get("connectors.mysql.port") + "/" + ConfigLoader.get("connectors.mysql.database"));
            put("spring.session.jdbc.initialize-schema", "always");
            put("spring.datasource.username", ConfigLoader.get("connectors.mysql.username"));
            put("spring.datasource.password", ConfigLoader.get("connectors.mysql.password"));
            put("spring.datasource.type", "com.mysql.cj.jdbc.MysqlDataSource");
            put("spring.datasource.driver-class-name", "com.mysql.jdbc.Driver");
            put("spring.session.store-type", "jdbc");
        }});

        application.addListeners(new ApplicationPidFileWriter("./bin/shutdown.pid"));
        context = application.run(args);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        restart();
    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            GlobalThreadPool.getScheduler().shutdownNow();
            FtpLogTailer.stop();
            FtpBanService.stop();
            RconImpl.stop();
            //EmfThreadLocal.shutdown();
            int code = SpringApplication.exit(context, () -> 0);
            System.gc();


            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            SpringApplication application = new SpringApplication(AdminPanelBackendApplication.class);
            application.setDefaultProperties(new HashMap<>() {{
                put("server.port", ConfigLoader.get("server.adminPanelPort"));
                put("spring.datasource.url", "jdbc:mysql://" + ConfigLoader.get("connectors.mysql.host") + ":" + ConfigLoader.get("connectors.mysql.port") + "/" + ConfigLoader.get("connectors.mysql.database"));
                put("spring.session.jdbc.initialize-schema", "always");
                put("spring.datasource.username", ConfigLoader.get("connectors.mysql.username"));
                put("spring.datasource.password", ConfigLoader.get("connectors.mysql.password"));
                put("spring.datasource.type", "com.mysql.cj.jdbc.MysqlDataSource");
                put("spring.datasource.driver-class-name", "com.mysql.jdbc.Driver");
                put("spring.session.store-type", "jdbc");
            }});
            application.addListeners(new ApplicationPidFileWriter("./bin/shutdown.pid"));
            application.run(args.getSourceArgs());
            //context = SpringApplication.run(AdminPanelBackendApplication.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }
}