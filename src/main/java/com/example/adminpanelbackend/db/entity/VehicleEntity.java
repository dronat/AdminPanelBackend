package com.example.adminpanelbackend.db.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "map_team_vehicle", schema = "squad")
public class VehicleEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "type", nullable = false, length = 255)
    private String type;
    @Basic
    @Column(name = "count", nullable = false)
    private Integer count;
    @Basic
    @Column(name = "delay", nullable = false)
    private Integer delay;
    @Basic
    @Column(name = "respawnTime", nullable = false)
    private Integer respawnTime;
    @Basic
    @Column(name = "rawType", nullable = false, length = 255)
    private String rawType;
    @Basic
    @Column(name = "icon", nullable = false, length = 255)
    private String icon;
    @Basic
    @Column(name = "spawnerSize", nullable = false, length = 255)
    private String spawnerSize;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleEntity that = (VehicleEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(count, that.count) && Objects.equals(delay, that.delay) && Objects.equals(respawnTime, that.respawnTime) && Objects.equals(rawType, that.rawType) && Objects.equals(icon, that.icon) && Objects.equals(spawnerSize, that.spawnerSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, count, delay, respawnTime, rawType, icon, spawnerSize);
    }
}
