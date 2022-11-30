package com.example.adminpanelbackend.dataBase.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "layers_history", schema = "squad")
public class LayerHistoryEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "layer", nullable = true, length = 255)
    private String layer;

    @Basic
    @Column(name = "creationTime", nullable = false)
    private Timestamp creationTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LayerHistoryEntity that = (LayerHistoryEntity) o;
        return Objects.equals(id, that.id)  && Objects.equals(layer, that.layer) && Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, layer, creationTime);
    }
}
