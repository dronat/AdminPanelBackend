package com.example.adminpanelbackend.controller;


import com.example.adminpanelbackend.Role;
import com.example.adminpanelbackend.db.entity.AdminEntity;
import com.example.adminpanelbackend.db.entity.MapEntity;
import com.example.adminpanelbackend.db.entity.RotationGroupEntity;
import com.example.adminpanelbackend.db.entity.RotationMapEntity;
import com.example.adminpanelbackend.model.RotationGroupModel;
import com.woop.Squad4J.rcon.Rcon;
import com.woop.Squad4J.server.RotationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.adminpanelbackend.ActionEnum.CHANGE_NEXT_LAYER;
import static com.example.adminpanelbackend.RoleEnum.BASE;
import static com.example.adminpanelbackend.RoleEnum.ROTATION_MANAGEMENT;

@RestController
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
@CrossOrigin
public class RotationController extends BaseSecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RotationController.class);


    @Role(role = BASE)
    @GetMapping(path = "/get-maps")
    public ResponseEntity<List<MapEntity>> getMaps(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(mapService.findAll());
    }

    @Role(role = ROTATION_MANAGEMENT)
    @PostMapping(path = "/add-new-rotation-group")
    public ResponseEntity<Object> addNewRotationGroup(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RotationGroupModel rotationGroupModel) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Set<Integer> set = new HashSet<>(rotationGroupModel.getMaps().stream().map(RotationGroupModel.RotationMapModel::getPosition).toList());
        if (set.size() != rotationGroupModel.getMaps().size()) {
            return ResponseEntity.status(400).body("Duplicate value 'position' in some rotations");
        }

        RotationGroupEntity rotationGroup = new RotationGroupEntity()
                .setServerID(serversService.findById(SERVER_ID).orElseThrow())
                .setName(rotationGroupModel.getName())
                .setIsActive(false);
        rotationGroupService.saveAndFlush(rotationGroup);

        rotationGroupModel.getMaps().forEach(mapModel ->
                rotationMapService.saveAndFlush(
                        new RotationMapEntity()
                                .setMap(mapService.findById(mapModel.getMapId()).orElseThrow())
                                .setPosition(mapModel.getPosition())
                                .setRotationGroup(rotationGroup)
                )
        );
        return ResponseEntity.ok().build();
    }

    @Role(role = ROTATION_MANAGEMENT)
    @PostMapping(path = "/change-rotation-group")
    public ResponseEntity<Object> changeRotationGroup(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RotationGroupModel rotationGroupModel) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Set<Integer> set = new HashSet<>(rotationGroupModel.getMaps().stream().map(RotationGroupModel.RotationMapModel::getPosition).toList());
        if (set.size() != rotationGroupModel.getMaps().size()) {
            return ResponseEntity.status(400).body("Duplicate value 'position' in some rotations");
        }
        RotationGroupEntity rotationGroup = rotationGroupService.findById(rotationGroupModel.getId()).orElseThrow().setMaps(new ArrayList<>());
        rotationMapService.deleteAllByRotationGroup(rotationGroup);
        rotationGroupService.saveAndFlush(rotationGroup.setName(rotationGroupModel.getName()));

        rotationGroupModel.getMaps().forEach(mapModel -> {
            RotationMapEntity rotationMapEntity = rotationMapService.saveAndFlush(
                    new RotationMapEntity()
                            .setMap(mapService.findById(mapModel.getMapId()).orElseThrow())
                            .setPosition(mapModel.getPosition())
                            .setRotationGroup(rotationGroup)
            );
            rotationGroup.getMaps().add(rotationMapEntity);
        });
        rotationGroupService.saveAndFlush(rotationGroup);
        return ResponseEntity.ok().build();
    }

    @Role(role = ROTATION_MANAGEMENT)
    @GetMapping(path = "/get-all-rotation-groups")
    public ResponseEntity<HashMap<String, Object>> getAllRotationGroups(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(
                new HashMap<>() {{
                    put("nextMapPosition", RotationListener.isRotationHaveMaps() ? RotationListener.getNextMapWithoutIncrement() : null);
                    put("rotations", rotationGroupService.findAllByServerID(serversService.findById(SERVER_ID).orElseThrow()));
                }}
        );
    }

    @Role(role = ROTATION_MANAGEMENT)
    @PostMapping(path = "/delete-rotation-group")
    public ResponseEntity<Void> deleteRotationGroup(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            int roleGroupId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        rotationGroupService.deleteById(roleGroupId);
        return ResponseEntity.ok().build();
    }

    @Role(role = ROTATION_MANAGEMENT)
    @PostMapping(path = "/activate-rotation-group")
    public ResponseEntity<Void> activateRotationGroup(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            int roleGroupId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        RotationGroupEntity newActiveRotationGroup = rotationGroupService.findById(roleGroupId).orElseThrow();
        try {
            RotationGroupEntity oldRotationGroupEntity = rotationGroupService.findByServerIDAndIsActiveIsTrue(serversService.findById(SERVER_ID).orElseThrow());
            rotationGroupService.saveAndFlush(oldRotationGroupEntity.setIsActive(false));
        } catch (Exception ignored) {
        }
        rotationGroupService.saveAndFlush(newActiveRotationGroup.setIsActive(true));
        String map = RotationListener.incrementNextMapAndGet();
        LOGGER.info("Setting next map by rotation: " + map);
        String rconResponse = Rcon.command("AdminSetNextLayer " + map);
        if (rconResponse == null || rconResponse.isEmpty()) {
            LOGGER.error("Error while trying set next map to '" + map + "', because RCON returned null or empty string in response");
            LOGGER.error("RCON RESPONSE: " + rconResponse);
        }
        entityManager.addAdminActionInLog(1, null, CHANGE_NEXT_LAYER, map);
        LOGGER.info("Next map '" + map + "' was set");
        return ResponseEntity.ok().build();
    }

    @Role(role = ROTATION_MANAGEMENT)
    @PostMapping(path = "/deactivate-rotation-group")
    public ResponseEntity<Void> deactivateRotationGroup(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            int roleGroupId) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        rotationGroupService
                .findById(roleGroupId)
                .ifPresent(rotationGroupEntity ->
                        rotationGroupService.saveAndFlush(rotationGroupEntity.setIsActive(false))
                );
        return ResponseEntity.ok().build();
    }

    @Role(role = ROTATION_MANAGEMENT)
    @PostMapping(path = "/set-next-rotation-map-position")
    public ResponseEntity<Object> setNextRotationMapPosition(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            int position) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (!RotationListener.isRotationHaveMaps()) {
            return ResponseEntity.status(400).body("Rotation module doesn't have active rotation");
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        rotationGroupService.findByServerIDAndIsActiveIsTrue(serversService.findById(SERVER_ID).orElseThrow()).getMaps().forEach(map -> {
            if (map.getPosition() == position) {
                flag.set(true);
            }
        });
        if (!flag.get()) {
            return ResponseEntity.status(400).body("Map with position '" + position + "' was not found in rotation");
        }
        if (RotationListener.updateRotationNextPosition(position)) {
                String map = RotationListener.getNextMapWithoutIncrement();
                LOGGER.info("Setting next map by rotation: " + map);
                String rconResponse = Rcon.command("AdminSetNextLayer " + map);
                if (rconResponse == null || rconResponse.isEmpty()) {
                    LOGGER.error("Error while trying set next map to '" + map + "', because RCON returned null or empty string in response");
                    LOGGER.error("RCON RESPONSE: " + rconResponse);
                }
                entityManager.addAdminActionInLog(1, null, CHANGE_NEXT_LAYER, map);
                LOGGER.info("Next map '" + map + "' was set");
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /*@Role(role = ADMINS_MANAGEMENT)
    @PostMapping(path = "/delete-maps")
    public ResponseEntity<HashMap<String, Object>> deleteMaps(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        mapService.deleteAll();
        return ResponseEntity.ok().build();
    }

    @Role(role = ADMINS_MANAGEMENT)
    @PostMapping(path = "/set-maps")
    public ResponseEntity<HashMap<String, Object>> setMaps(
            @RequestParam("file") MultipartFile file,
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        if (file == null) {
            return ResponseEntity.badRequest().build();
        }

        List<String> dbRawNames = mapService.findAllRawNames();
        List<String> addedRawNames = new ArrayList<>();
        List<String> duplicatedRawNames = new ArrayList<>();
        List<String> wrongRawNames = new ArrayList<>();

        try {
            String fileContent = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);
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
                            vehicleService.saveAllAndFlush(mapEntity.getTeamOne().getVehicles().stream().map(VehiclesEntity::getVehicle).toList());
                            vehicleService.saveAllAndFlush(mapEntity.getTeamTwo().getVehicles().stream().map(VehiclesEntity::getVehicle).toList());
                            mapService.saveAndFlush(mapEntity);
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
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(new HashMap<>() {{
            put("addedRawNames", addedRawNames);
            put("duplicatedRawNames", duplicatedRawNames);
            put("wrongRawNames", wrongRawNames);
        }});
    }

    private boolean checkDtoValid(MapDTO dto) {
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
    */
}
