package com.example.adminpanelbackend.db.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "layers_history", schema = "squad")
public class LayerHistoryEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "serverId", referencedColumnName = "id", nullable = false)
    private ServersEntity serverId;

    @ManyToOne
    @JoinColumn(name = "layer", referencedColumnName = "rawName", nullable = false)
    private MapEntity layer;

    @Basic
    @Column(name = "creationTime", nullable = false)
    private Timestamp creationTime;
}
