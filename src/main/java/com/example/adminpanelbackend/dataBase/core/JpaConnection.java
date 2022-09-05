package com.example.adminpanelbackend.dataBase.core;

import com.woop.Squad4J.util.ConfigLoader;

public interface JpaConnection {

    EmfThreadLocal EMF_THREAD_LOCAL = new EmfBuilder()
            .mySql()
            .withDbHost((String) ConfigLoader.get("connectors.mysql.host"))
            .withDbPort((Integer) ConfigLoader.get("connectors.mysql.port"))
            .withDbName((String) ConfigLoader.get("connectors.mysql.database"))
            .withUsername((String) ConfigLoader.get("connectors.mysql.username"))
            .withPassword((String) ConfigLoader.get("connectors.mysql.password"))
            .withPersistenceUnitName("AdminPanelPersistence")
            .buildThreadLocal();
}
