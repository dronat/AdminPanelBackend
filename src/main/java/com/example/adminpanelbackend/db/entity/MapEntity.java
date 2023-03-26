package com.example.adminpanelbackend.db.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "map", schema = "squad")
public class MapEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Basic
    @Column(name = "rawName", nullable = false, length = 255)
    private String rawName;

    @Basic
    @Column(name = "levelName", nullable = false, length = 255)
    private String levelName;

    @Basic
    @Column(name = "lighting", nullable = false, length = 255)
    private String lighting;

    @Basic
    @Column(name = "mapName", nullable = false, length = 255)
    private String mapName;

    @Basic
    @Column(name = "gameMode", nullable = false, length = 255)
    private String gameMode;

    @Basic
    @Column(name = "layerVersion", nullable = false, length = 255)
    private String layerVersion;

    @Basic
    @Column(name = "mapSize", nullable = false, length = 255)
    private String mapSize;

    @Basic
    @Column(name = "numOfGames", nullable = false)
    private Integer numOfGames;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "teamOne", referencedColumnName = "id", nullable = false)
    private TeamEntity teamOne;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "teamTwo", referencedColumnName = "id", nullable = false)
    private TeamEntity teamTwo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapEntity mapEntity = (MapEntity) o;
        return Objects.equals(name, mapEntity.name) && Objects.equals(rawName, mapEntity.rawName) && Objects.equals(levelName, mapEntity.levelName) && Objects.equals(lighting, mapEntity.lighting) && Objects.equals(id, mapEntity.id) && Objects.equals(mapName, mapEntity.mapName) && Objects.equals(gameMode, mapEntity.gameMode) && Objects.equals(layerVersion, mapEntity.layerVersion) && Objects.equals(mapSize, mapEntity.mapSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rawName, levelName, lighting, id, mapName, gameMode, layerVersion, mapSize);
    }
}
