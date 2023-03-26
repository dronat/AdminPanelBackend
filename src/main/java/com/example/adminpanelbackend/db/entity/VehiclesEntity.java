package com.example.adminpanelbackend.db.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "map_team_vehicles", schema = "squad")
//@IdClass(MapTeamVehiclesEntityPK.class)
public class VehiclesEntity implements Serializable {

    @JsonBackReference
    @ManyToOne(cascade = ALL)
    @Id
    @JoinColumn(name = "team", referencedColumnName = "id", nullable = false)
    private TeamEntity team;

    @ManyToOne(cascade = ALL)
    @Id
    @JoinColumn(name = "vehicle", referencedColumnName = "id", nullable = false)
    private VehicleEntity vehicle;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehiclesEntity that = (VehiclesEntity) o;
        return true/*Objects.equals(team, that.team) && Objects.equals(vehicle, that.vehicle)*/;
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, vehicle);
    }
}
