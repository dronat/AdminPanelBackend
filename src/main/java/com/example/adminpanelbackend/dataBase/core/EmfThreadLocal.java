package com.example.adminpanelbackend.dataBase.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class EmfThreadLocal {
    private static EntityManagerFactory emf;
    private static ThreadLocal<EntityManager> emThreadLocal = new ThreadLocal();
    private static Logger log = LoggerFactory.getLogger(EmfThreadLocal.class);

    EmfThreadLocal(EntityManagerFactory emf) {
        EmfThreadLocal.emf = emf;
    }

    public EntityManagerFactory getEmf() {
        return emf;
    }

    public EntityManager getEntityManager() {
        if (emThreadLocal.get() == null) {
            Class var1 = EmfThreadLocal.class;
            synchronized(EmfThreadLocal.class) {
                if (emThreadLocal.get() == null) {
                    emThreadLocal.set(emf.createEntityManager());
                }
            }
        }

        return (EntityManager)emThreadLocal.get();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            log.warn("### Close EntityManagerFactory ###");
            emf.close();
        }

    }
}
