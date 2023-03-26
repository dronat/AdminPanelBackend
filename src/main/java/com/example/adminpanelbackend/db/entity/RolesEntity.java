package com.example.adminpanelbackend.db.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "roles", schema = "squad")
public class RolesEntity implements Serializable {

    @JsonBackReference
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonBackReference("roles")
    @ManyToOne
    @JoinColumn(name = "roleGroup", referencedColumnName = "id", nullable = false)
    private RoleGroupEntity roleGroup;


    @OneToOne
    @JoinColumn(name = "role", referencedColumnName = "id", nullable = false)
    private RoleEntity role;
}
