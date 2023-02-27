package com.example.adminpanelbackend.db.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "players_bans", schema = "squad")
public class PlayerBanEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "reason", nullable = true, length = 255)
    private String reason;

    @Basic
    @Column(name = "isUnbannedManually", nullable = true, length = 255)
    private Boolean isUnbannedManually;

    @Basic
    @Column(name = "unbannedTime", nullable = true)
    private Timestamp unbannedTime;

    @Basic
    @Column(name = "expirationTime", nullable = true)
    private Timestamp expirationTime;

    @Basic
    @Column(name = "creationTime", nullable = false)
    private Timestamp creationTime;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "player", referencedColumnName = "steamId", nullable = false)
    private PlayerEntity player;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "admin", referencedColumnName = "steamId", nullable = false)
    private AdminEntity admin;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "unbannedAdmin", referencedColumnName = "steamId", nullable = true)
    private AdminEntity unbannedAdmin;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerBanEntity that = (PlayerBanEntity) o;
        return Objects.equals(admin, that.admin) && Objects.equals(id, that.id) && Objects.equals(reason, that.reason) && Objects.equals(expirationTime, that.expirationTime) && Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, admin, reason, expirationTime, creationTime);
    }
}
