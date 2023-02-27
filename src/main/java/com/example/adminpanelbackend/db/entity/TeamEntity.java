package com.example.adminpanelbackend.db.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "map_team", schema = "squad")
public class TeamEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "faction", nullable = false, length = 255)
    private String faction;

    @Basic
    @Column(name = "teamSetupName", nullable = false, length = 255)
    private String teamSetupName;

    @Basic
    @Column(name = "tickets", nullable = false, length = 255)
    private String tickets;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private Collection<VehiclesEntity> vehicles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamEntity that = (TeamEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(faction, that.faction) && Objects.equals(teamSetupName, that.teamSetupName) && Objects.equals(tickets, that.tickets) && Objects.equals(vehicles, that.vehicles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, faction, teamSetupName, tickets, vehicles);
    }
}
