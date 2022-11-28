package com.example.adminpanelbackend.dataBase.core;

import com.woop.Squad4J.server.SquadServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.function.Consumer;

public class JpaManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SquadServer.class);
    protected volatile EntityManager em;

    public JpaManager(EntityManager em) {
        this.em = em;
    }

    protected synchronized void transaction(Consumer<EntityManager> action) {
        EntityTransaction tx = this.em.getTransaction();

        try {
            if (!tx.isActive()) {
                tx.begin();
            }
            action.accept(this.em);
            tx.commit();
        } catch (RuntimeException var4) {
            tx.rollback();
            LOGGER.error("Failed to transaction", var4);
            throw var4;
        }
    }

    public synchronized  <T> void persist(T entity) {
        this.transaction((em) -> {
            em.persist(entity);
        });
    }

    public synchronized <T> void update(T entity) {
        this.transaction((em) -> {
            em.merge(entity);
        });
    }

    public synchronized <T> void refresh(T entity) {
        this.em.refresh(entity);
    }

    public synchronized <T> void remove(T entity) {
        this.transaction((em) -> {
            em.remove(entity);
        });
    }
}
