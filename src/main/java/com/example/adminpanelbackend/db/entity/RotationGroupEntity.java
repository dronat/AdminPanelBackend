package com.example.adminpanelbackend.db.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "rotation_groups", schema = "squad")
public class RotationGroupEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "serverId", referencedColumnName = "id", nullable = false)
    private ServerEntity serverID;

    @Basic
    @Column(name = "name", nullable = false)
    private String name;

    @Basic
    @Column(name = "isActive", nullable = false)
    private Boolean isActive;

    @JsonBackReference
    @OneToMany(mappedBy = "rotationGroup")
    private List<RotationMapEntity> maps;
}
