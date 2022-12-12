package com.example.adminpanelbackend.dataBase.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "role_group", schema = "squad")
public class RoleGroupEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "name", nullable = false)
    private String name;

    @JsonManagedReference("roles")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "roleGroup")
    private List<RolesEntity> roles;
}
