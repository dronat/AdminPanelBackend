package com.example.adminpanelbackend.db.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

enum EmfContext {
    INSTANCE;

    private final Map<ConnectionConfig, EntityManagerFactory> container = new HashMap();
    private Logger log = LoggerFactory.getLogger(EmfContext.class);

    private EmfContext() {
    }

    public synchronized void reloadConnectionConfigContainer() {
        this.container.clear();
    }

    synchronized EntityManagerFactory get(ConnectionConfig ConnectionConfig) {
        if (this.container.containsKey(ConnectionConfig)) {
            return (EntityManagerFactory) this.container.get(ConnectionConfig);
        } else {
            this.log.info("### Init EntityManagerFactory ###");
            Map<String, String> settings = new HashMap();
            if (ConnectionConfig.jdbcUrl == null) {
                settings.put("hibernate.connection.url", ConnectionConfig.jdbcPrefix + "://" + ConnectionConfig.dbHost + ":" + ConnectionConfig.dbPort + "/" + ConnectionConfig.dbName);
            } else {
                settings.put("hibernate.connection.url", ConnectionConfig.jdbcUrl);
            }
            settings.put("hibernate.connection.driver_class", ConnectionConfig.jdbcDriver);
            settings.put("hibernate.connection.username", ConnectionConfig.username);
            settings.put("hibernate.connection.password", ConnectionConfig.password);
            /*settings.put("connection.pool_size", "100");
            settings.put("hibernate.c3p0.acquire_increment", "1");
            settings.put("hibernate.c3p0.idle_test_period", "60");
            settings.put("hibernate.c3p0.min_size", "1");
            settings.put("hibernate.c3p0.max_size", "2");
            settings.put("hibernate.c3p0.max_statements", "50");
            settings.put("hibernate.c3p0.timeout", "0");
            settings.put("hibernate.c3p0.acquireRetryAttempts", "1");
            settings.put("hibernate.c3p0.acquireRetryDelay", "250");
            settings.put("hibernate.show_sql", "true");
            settings.put("hibernate.use_sql_comments", "true");*/

            // HikariCP settings

            // Maximum waiting time for a connection from the pool
            settings.put("hibernate.hikari.connectionTimeout", "20000");
            // Minimum number of ideal connections in the pool
            settings.put("hibernate.hikari.minimumIdle", "10");
            // Maximum number of actual connection in the pool
            settings.put("hibernate.hikari.maximumPoolSize", "20");
            // Maximum time that a connection is allowed to sit ideal in the pool
            settings.put("hibernate.hikari.idleTimeout", "300000");
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(ConnectionConfig.persistenceUnitName, settings);
            this.container.put(ConnectionConfig, entityManagerFactory);
            return entityManagerFactory;
        }
    }

    Collection<EntityManagerFactory> storedEmf() {
        return this.container.values();
    }
}
