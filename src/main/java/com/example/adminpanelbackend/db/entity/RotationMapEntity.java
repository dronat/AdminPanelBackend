package com.example.adminpanelbackend.db.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "rotation_maps", schema = "squad")
public class RotationMapEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "map", referencedColumnName = "id", nullable = false)
    private MapEntity map;

    @Basic
    @Column(name = "position", nullable = false)
    private Integer position;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "rotationGroup", referencedColumnName = "id", nullable = false)
    private RotationGroupEntity rotationGroup;
}
