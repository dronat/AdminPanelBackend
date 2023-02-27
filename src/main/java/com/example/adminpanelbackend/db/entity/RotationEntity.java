package com.example.adminpanelbackend.db.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "rotation", schema = "squad")
public class RotationEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "serverId", referencedColumnName = "id", nullable = false)
    private ServersEntity serverId;

    @ManyToOne
    @JoinColumn(name = "map", referencedColumnName = "id", nullable = false)
    private MapEntity map;

    @Basic
    @Column(name = "position", nullable = false)
    private Integer position;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RotationEntity mapEntity = (RotationEntity) o;
        return Objects.equals(position, mapEntity.position) && Objects.equals(map, mapEntity.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, map);
    }
}
