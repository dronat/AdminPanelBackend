package com.woop.Squad4J.server;

import com.example.adminpanelbackend.db.EntityManager;
import com.example.adminpanelbackend.db.entity.RotationEntity;
import com.woop.Squad4J.util.ConfigLoader;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Data
@Accessors
public class RotationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotationListener.class);
    private static final int SERVER_ID = ConfigLoader.get("server.id", Integer.class);
    private static EntityManager entityManager;
    private volatile static int nextMapPosition;
    private static boolean init;

    public synchronized static void init() {
        if (!init) {
            entityManager = new EntityManager();
            nextMapPosition = 0;
            init = true;
            LOGGER.info("Rotation was initialized");
        }
    }

    public static synchronized boolean updateRotationNextPosition(int nextMapPosition) {
        if (!init) {
            throw new IllegalStateException("This class doesn't initialized.");
        }
        List<RotationEntity> rotationEntities = entityManager.getRotationEntitiesByServerId(SERVER_ID);
        if (nextMapPosition > rotationEntities.size() || nextMapPosition <= 0) {
            return false;
        } else if (rotationEntities.stream().anyMatch(rotationEntity -> rotationEntity.getPosition() == nextMapPosition)) {
            RotationListener.nextMapPosition = nextMapPosition;
            return true;
        }
        return false;
    }

    public static synchronized String incrementNextMapAndGet() {
        nextMapPosition++;
        return getNextMapWithoutIncrement();
    }

    public static synchronized String getNextMapWithoutIncrement() {
        List<RotationEntity> rotationEntities = entityManager.getRotationEntitiesByServerId(SERVER_ID);
        String nextMap = null;
        for (RotationEntity rotationMap : rotationEntities) {
            if (rotationMap.getPosition() == nextMapPosition) {
                nextMap = rotationMap.getMap().getRawName();
            }
        }
        if (nextMap == null || nextMap.isEmpty()) {
            int tmpMapPosition = nextMapPosition;

            RotationEntity tmpRotation = findMapInRotationWithLowestPosition(rotationEntities);
            nextMap = tmpRotation.getMap().getRawName();
            nextMapPosition = tmpRotation.getPosition();
            if (nextMap == null || nextMap.isEmpty()) {
                LOGGER.error("Not found next map in rotation by id: " + tmpMapPosition);
            }
        }
        return nextMap;
    }

    public static boolean isRotationHaveMaps() {
        return entityManager.getRotationEntitiesByServerId(SERVER_ID).size() > 0;
    }

    private static RotationEntity findMapInRotationWithLowestPosition(List<RotationEntity> rotationEntities) {
        int position = rotationEntities.stream().map(RotationEntity::getPosition).mapToInt(rotationMap -> rotationMap).min().orElse(Integer.MAX_VALUE);
        return rotationEntities
                .stream()
                .filter(tmpNextMap -> tmpNextMap.getPosition() == position)
                .findFirst()
                .orElseThrow();
    }
}