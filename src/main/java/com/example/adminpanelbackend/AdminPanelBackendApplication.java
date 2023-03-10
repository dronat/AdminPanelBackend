package com.example.adminpanelbackend;


import com.woop.Squad4J.main.SquadModule;
import com.woop.Squad4J.util.ConfigLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.util.HashMap;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class AdminPanelBackendApplication {

    public static void main(String[] args) {
        SquadModule.init();

        SpringApplication springApplication = new SpringApplication(AdminPanelBackendApplication.class);
        springApplication.setDefaultProperties(new HashMap<>() {{
            put("server.port", ConfigLoader.get("server.adminPanelPort"));
            put("spring.datasource.url", "jdbc:mysql://" + ConfigLoader.get("connectors.mysql.host") + ":" + ConfigLoader.get("connectors.mysql.port") + "/" + ConfigLoader.get("connectors.mysql.database"));
            put("spring.session.jdbc.initialize-schema", "always");
            put("spring.datasource.username", ConfigLoader.get("connectors.mysql.username"));
            put("spring.datasource.password", ConfigLoader.get("connectors.mysql.password"));
            put("spring.datasource.type", "com.mysql.cj.jdbc.MysqlDataSource");
            put("spring.datasource.driver-class-name", "com.mysql.jdbc.Driver");
            put("spring.session.store-type", "jdbc");
        }});

        //springApplication.addListeners(new ApplicationPidFileWriter("./bin/shutdown.pid"));
        springApplication.run(args);
    }
}