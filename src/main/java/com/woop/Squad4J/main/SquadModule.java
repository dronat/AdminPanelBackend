package com.woop.Squad4J.main;

import com.example.adminpanelbackend.db.EntityManager;
import com.example.adminpanelbackend.db.entity.MapEntity;
import com.example.adminpanelbackend.db.entity.TeamEntity;
import com.example.adminpanelbackend.db.entity.VehicleEntity;
import com.example.adminpanelbackend.db.entity.VehiclesEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woop.Squad4J.a2s.Query;
import com.woop.Squad4J.connector.MySQLConnector;
import com.woop.Squad4J.dto.map.MapDTO;
import com.woop.Squad4J.dto.map.MapsDTO;
import com.woop.Squad4J.dto.map.VehicleDTO;
import com.woop.Squad4J.rcon.Rcon;
import com.woop.Squad4J.server.EventEmitter;
import com.woop.Squad4J.server.RotationListener;
import com.woop.Squad4J.server.SquadServer;
import com.woop.Squad4J.server.tailer.FtpBanService;
import com.woop.Squad4J.server.tailer.TailerService;
import com.woop.Squad4J.util.logger.LoggerUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * _____                       _ _  _       _
 * / ____|                     | | || |     | |
 * | (___   __ _ _   _  __ _  __| | || |_    | |
 * \___ \ / _` | | | |/ _` |/ _` |__   _|   | |
 * ____) | (_| | |_| | (_| | (_| |  | || |__| |
 * |_____/ \__, |\__,_|\__,_|\__,_|  |_| \____/
 * | |
 * |_|
 * <p>
 * Main entry point for Squad4J. Initializes all services needed to run Squad4J.
 *
 * @author Robert Engle
 */

public class SquadModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(SquadModule.class);
    private static final EntityManager entityManager = new EntityManager();

    public static void init() {
        initMaps();

        //Initialize logger before pushing any output to console
        LoggerUtil.init();

        //Initailize query service
        Query.init();

        //Initialize connectors
        MySQLConnector.init();

        //Initialize services
        //Initialize RCON service
        Rcon.init();

        //Initialize log tailer service
        TailerService.init();

        new Thread(new FtpBanService()).start();

        RotationListener.init();

        //Initialize servers
        //Initialize squad server
        SquadServer.init();

        //Initialize event emitter last
        //Intialize event emitter service
        EventEmitter.init();


        //TODO: Remove me after debugging
        //Event testEvent = new ServerTickRateEvent(new Date(), EventType.SERVER_TICK_RATE, 0, 35.5);
        //LOGGER.debug("Emitting {}", testEvent);
        //EventEmitter.emit(testEvent);
        printLogo();
    }

    private static void printLogo() {
        String color = "\u001B[46m \u001B[30m";
        LOGGER.info(color);
        LOGGER.info(color + "          //\\\\                 //\\\\              //   ||          //\\\\              //\\\\          ||===========");
        LOGGER.info(color + "         //  \\\\               //  \\\\            //               //  \\\\            //  \\\\         ||           ");
        LOGGER.info(color + "        //    \\\\             //    \\\\          //     ||        //    \\\\          //    \\\\        ||           ");
        LOGGER.info(color + "       //      \\\\           //      \\\\        //      ||       //      \\\\        //      \\\\       ||           ");
        LOGGER.info(color + "      //        \\\\         //        \\\\      //       ||      //        \\\\      //        \\\\      ||===========");
        LOGGER.info(color + "     //==========\\\\       //          \\\\    //        ||     //          \\\\    //          \\\\     ||           ");
        LOGGER.info(color + "    //            \\\\     //            \\\\  //         ||    //            \\\\  //            \\\\    ||           ");
        LOGGER.info(color + "   //              \\\\   //              \\\\//          ||   //              \\\\//              \\\\   ||===========");
        LOGGER.info(color + "");
        LOGGER.info(color + "");
        LOGGER.info(color + "");
        LOGGER.info(color + "     /=======\\          /=======\\       \\\\              //          //\\\\              //       /=======\\  ");
        LOGGER.info(color + "    //       \\\\        //       \\\\       \\\\            //          //  \\\\            //       //       \\\\ ");
        LOGGER.info(color + "   //                 //         \\\\       \\\\          //          //    \\\\          //       //         \\\\");
        LOGGER.info(color + "  ||                 ||           ||       \\\\        //          //      \\\\        //       ||           || ");
        LOGGER.info(color + "  ||      =====\\     ||           ||        \\\\      //          //        \\\\      //        ||           || ");
        LOGGER.info(color + "   \\\\         //      \\\\         //          \\\\    //          //          \\\\    //          \\\\         //  ");
        LOGGER.info(color + "    \\\\       //        \\\\       //            \\\\  //          //            \\\\  //            \\\\       //   ");
        LOGGER.info(color + "     \\======//          \\=======/              \\\\//          //              \\\\//              \\=======/    ");
        LOGGER.info(color);
        LOGGER.info("\u001B[0m");
        System.out.println();
    }

    private static void initMaps() {
        LOGGER.info("Initializing maps");
        List<String> dbRawNames = entityManager.getAllMaps().stream().map(MapEntity::getRawName).toList();
        List<String> addedRawNames = new ArrayList<>();
        List<String> duplicatedRawNames = new ArrayList<>();
        List<String> wrongRawNames = new ArrayList<>();


        try (InputStream fis = new FileInputStream("maps.json")) {
            String fileContent = IOUtils.toString(fis, StandardCharsets.UTF_8);
            if (fileContent == null || fileContent.isEmpty()) {
                throw new RuntimeException();
            }

            MapsDTO mapsDTO = new ObjectMapper().readValue(fileContent, MapsDTO.class);
            if (mapsDTO == null) {
                throw new RuntimeException();
            }
            mapsDTO.getMaps().forEach(mapDTO -> {
                try {

                    MapEntity mapEntity = new MapEntity();
                    mapEntity.setName(mapDTO.getName())
                            .setMapName(mapDTO.getMapName())
                            .setRawName(mapDTO.getRawName())
                            .setLevelName(mapDTO.getLevelName())
                            .setLighting(mapDTO.getLighting())
                            .setTeamOne(
                                    new TeamEntity()
                                            .setFaction(mapDTO.getTeamOne().getFaction())
                                            .setTeamSetupName(mapDTO.getTeamOne().getTeamSetupName())
                                            .setTickets(mapDTO.getTeamOne().getTickets())
                            )
                            .setTeamTwo(
                                    new TeamEntity()
                                            .setFaction(mapDTO.getTeamTwo().getFaction())
                                            .setTeamSetupName(mapDTO.getTeamTwo().getTeamSetupName())
                                            .setTickets(mapDTO.getTeamTwo().getTickets())
                            )
                            .setMapName(mapDTO.getMapName())
                            .setGameMode(mapDTO.getGameMode())
                            .setLayerVersion(mapDTO.getLayerVersion())
                            .setMapSize(mapDTO.getMapSize())
                            .setNumOfGames(0);

                    mapEntity.getTeamOne().setVehicles(
                            mapDTO.getTeamOne().getVehicles() == null ?
                                    null
                                    : mapDTO.getTeamOne()
                                    .getVehicles()
                                    .stream()
                                    .map(vehicle ->
                                            new VehiclesEntity()
                                                    .setVehicle(
                                                            new VehicleEntity()
                                                                    .setType(vehicle.getType())
                                                                    .setCount(vehicle.getCount())
                                                                    .setDelay(vehicle.getDelay())
                                                                    .setRespawnTime(vehicle.getRespawnTime())
                                                                    .setRawType(vehicle.getRawType())
                                                                    .setIcon(vehicle.getIcon())
                                                                    .setSpawnerSize(vehicle.getSpawnerSize())
                                                    )
                                                    .setTeam(mapEntity.getTeamOne())
                                    )
                                    .collect(Collectors.toList())
                    );

                    mapEntity.getTeamTwo().setVehicles(
                            mapDTO.getTeamOne().getVehicles() == null ?
                                    null
                                    : mapDTO.getTeamOne()
                                    .getVehicles()
                                    .stream()
                                    .map(vehicle ->
                                            new VehiclesEntity()
                                                    .setVehicle(
                                                            new VehicleEntity()
                                                                    .setType(vehicle.getType())
                                                                    .setCount(vehicle.getCount())
                                                                    .setDelay(vehicle.getDelay())
                                                                    .setRespawnTime(vehicle.getRespawnTime())
                                                                    .setRawType(vehicle.getRawType())
                                                                    .setIcon(vehicle.getIcon())
                                                                    .setSpawnerSize(vehicle.getSpawnerSize())
                                                    )
                                                    .setTeam(mapEntity.getTeamTwo())
                                    )
                                    .collect(Collectors.toList())
                    );

                    if (dbRawNames.contains(mapEntity.getRawName())) {
                        duplicatedRawNames.add(mapDTO.getRawName());
                    } else {
                        if (checkDtoValid(mapDTO)) {
                            entityManager.persist(mapEntity.getTeamOne().getVehicles().stream().map(VehiclesEntity::getVehicle).toList());
                            entityManager.persist(mapEntity.getTeamTwo().getVehicles().stream().map(VehiclesEntity::getVehicle).toList());
                            entityManager.persist(mapEntity);
                            addedRawNames.add(mapDTO.getRawName());
                        } else {
                            wrongRawNames.add(mapDTO.getName());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    wrongRawNames.add(mapDTO.getName());
                }

            });
        } catch (Exception e) {
            LOGGER.error("Can't parse maps.json", e);
        }
        LOGGER.info("Maps initialized");
        if (!addedRawNames.isEmpty()) {
            LOGGER.info("Added maps: " + addedRawNames.size());
        }
        if (!duplicatedRawNames.isEmpty()) {
            LOGGER.warn("Not added because of duplicate: " + duplicatedRawNames.size());
        }
        wrongRawNames.forEach(wrongRawName -> LOGGER.warn("Map '" + wrongRawName + "' not added because have some errors in json validation"));
    }

    private static boolean checkDtoValid(MapDTO dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) return false;
        if (dto.getRawName() == null || dto.getRawName().isEmpty()) return false;
        if (dto.getLevelName() == null || dto.getLevelName().isEmpty()) return false;
        if (dto.getLighting() == null || dto.getLighting().isEmpty()) return false;

        if (dto.getTeamOne() == null) return false;
        if (dto.getTeamOne().getFaction() == null || dto.getTeamOne().getFaction().isEmpty()) return false;
        if (dto.getTeamOne().getTeamSetupName() == null || dto.getTeamOne().getTeamSetupName().isEmpty()) return false;
        if (dto.getTeamOne().getTickets() == null || dto.getTeamOne().getTickets().isEmpty()) return false;
        if (dto.getTeamOne().getVehicles() == null || dto.getTeamOne().getVehicles().isEmpty()) return false;
        for (VehicleDTO vehicle : dto.getTeamOne().getVehicles()) {
            if (vehicle.getType() == null || vehicle.getType().isEmpty()) return false;
            if (vehicle.getRawType() == null || vehicle.getRawType().isEmpty()) return false;
            if (vehicle.getIcon() == null || vehicle.getIcon().isEmpty()) return false;
            if (vehicle.getSpawnerSize() == null || vehicle.getSpawnerSize().isEmpty()) return false;
        }

        if (dto.getTeamTwo() == null) return false;
        if (dto.getTeamTwo().getFaction() == null || dto.getTeamTwo().getFaction().isEmpty()) return false;
        if (dto.getTeamTwo().getTeamSetupName() == null || dto.getTeamTwo().getTeamSetupName().isEmpty()) return false;
        if (dto.getTeamTwo().getTickets() == null || dto.getTeamTwo().getTickets().isEmpty()) return false;
        if (dto.getTeamTwo().getVehicles() == null || dto.getTeamTwo().getVehicles().isEmpty()) return false;
        for (VehicleDTO vehicle : dto.getTeamTwo().getVehicles()) {
            if (vehicle.getType() == null || vehicle.getType().isEmpty()) return false;
            if (vehicle.getRawType() == null || vehicle.getRawType().isEmpty()) return false;
            if (vehicle.getIcon() == null || vehicle.getIcon().isEmpty()) return false;
            if (vehicle.getSpawnerSize() == null || vehicle.getSpawnerSize().isEmpty()) return false;
        }

        if (dto.getMapName() == null || dto.getMapName().isEmpty()) return false;
        if (dto.getGameMode() == null || dto.getGameMode().isEmpty()) return false;
        if (dto.getLayerVersion() == null || dto.getLayerVersion().isEmpty()) return false;
        if (dto.getMapSize() == null || dto.getMapSize().isEmpty()) return false;
        return true;
    }
}
