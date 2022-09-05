package com.example.adminpanelbackend.dataBase.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

enum EmfContext {
    INSTANCE;

    private Logger log = LoggerFactory.getLogger(EmfContext.class);
    private final Map< ConnectionConfig, EntityManagerFactory> container = new HashMap();

    private EmfContext() {
    }

    public synchronized void reloadConnectionConfigContainer() {
        this.container.clear();
    }

    synchronized EntityManagerFactory get( ConnectionConfig  ConnectionConfig) {
        if (this.container.containsKey( ConnectionConfig)) {
            return (EntityManagerFactory)this.container.get( ConnectionConfig);
        } else {
            this.log.warn("### Init EntityManagerFactory ###");
            Map<String, String> settings = new HashMap();
            if ( ConnectionConfig.jdbcUrl == null) {
                settings.put("hibernate.connection.url",  ConnectionConfig.jdbcPrefix + "://" +  ConnectionConfig.dbHost + ":" +  ConnectionConfig.dbPort + "/" +  ConnectionConfig.dbName);
            } else {
                settings.put("hibernate.connection.url",  ConnectionConfig.jdbcUrl);
            }
            settings.put("hibernate.connection.driver_class",  ConnectionConfig.jdbcDriver);
            settings.put("hibernate.connection.username",  ConnectionConfig.username);
            settings.put("hibernate.connection.password",  ConnectionConfig.password);
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory( ConnectionConfig.persistenceUnitName, settings);
            this.container.put( ConnectionConfig, entityManagerFactory);
            return entityManagerFactory;
        }
    }

    Collection<EntityManagerFactory> storedEmf() {
        return this.container.values();
    }
}
