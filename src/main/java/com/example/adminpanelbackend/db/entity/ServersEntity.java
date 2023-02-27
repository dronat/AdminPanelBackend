package com.example.adminpanelbackend.db.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "servers", schema = "squad")
public class ServersEntity implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "fullName")
    private String fullName;

    @Basic
    @Column(name = "shortName", nullable = false)
    private String shortName;
}
