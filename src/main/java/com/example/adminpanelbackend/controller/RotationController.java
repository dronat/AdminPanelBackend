package com.example.adminpanelbackend.controller;


import com.example.adminpanelbackend.Role;
import com.example.adminpanelbackend.db.entity.AdminEntity;
import com.example.adminpanelbackend.db.entity.MapEntity;
import com.example.adminpanelbackend.db.entity.RotationEntity;
import com.example.adminpanelbackend.model.RotationModel;
import com.woop.Squad4J.server.RotationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @PostMapping(path = "/set-rotation")
    public ResponseEntity<Object> setRotation(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RotationModel rotationModel) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        Set<Integer> set = new HashSet<>(rotationModel.getRotationList().stream().map(RotationModel.Rotation::getPosition).toList());
        if (set.size() != rotationModel.getRotationList().size()) {
            return ResponseEntity.status(400).body("Duplicate value 'position' in some rotations");
        }

        rotationService.deleteAllByServerId(SERVER_ID);
        rotationModel.getRotationList().forEach(rotationMap ->
                rotationService.saveAndFlush(
                        new RotationEntity()
                                .setPosition(rotationMap.getPosition())
                                .setMap(mapService.findById(rotationMap.getMapId()).get())
                                .setServerId(serversService.findById(SERVER_ID).orElseThrow())
                )
        );
        return ResponseEntity.ok().build();
    }

    @Role(role = ROTATION_MANAGEMENT)
    @GetMapping(path = "/get-rotation")
    public ResponseEntity<List<RotationEntity>> getRotation(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        return ResponseEntity.ok(entityManager.getRotationEntitiesByServerId(SERVER_ID));
    }

    @Role(role = ROTATION_MANAGEMENT)
    @GetMapping(path = "/set-next-rotation-map-position")
    public ResponseEntity<Object> setNextRotationMapPosition(
            @SessionAttribute AdminEntity userInfo,
            HttpSession httpSession,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam int position) {
        LOGGER.debug("Received secured {} request on '{}' with userInfo in cookie '{}'", request.getMethod(), request.getRequestURL(), userInfo);
        AtomicBoolean flag = new AtomicBoolean(false);
        entityManager.getRotationEntitiesByServerId(SERVER_ID).forEach(rotationEntity -> {
            if (rotationEntity.getPosition() == position) {
                flag.set(true);
            }
        });
        if (!flag.get()) {
            return ResponseEntity.status(400).body("Map with position '" + position + "' was not found in rotation");
        }
        if (RotationListener.updateRotationNextPosition(position)) {
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
