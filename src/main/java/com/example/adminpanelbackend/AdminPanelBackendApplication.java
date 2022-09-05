package com.example.adminpanelbackend;


import com.woop.Squad4J.main.SquadModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminPanelBackendApplication {

    public static void main(String[] args) {
        SquadModule.init();
        SpringApplication.run(AdminPanelBackendApplication.class, args);
    }
}