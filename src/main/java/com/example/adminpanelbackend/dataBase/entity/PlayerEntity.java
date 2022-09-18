package com.example.adminpanelbackend.dataBase.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "players", schema = "squad")
public class PlayerEntity implements Serializable {
    @Id
    @Column(name = "steamId", nullable = false)
    private Long steamId;

    @Basic
    @Column(name = "name", nullable = true, length = 255)
    private String name;

    @Basic
    @Column(name = "createTime", nullable = false)
    private Timestamp createTime;

    @OneToMany(mappedBy = "playersBySteamId")
    private Collection<PlayerBanEntity> playersBansBySteamId;

    @OneToMany(mappedBy = "playersBySteamId")
    private Collection<PlayerMessageEntity> playersMessagesBySteamId;

    @OneToMany(mappedBy = "playersBySteamId")
    private Collection<PlayerNoteEntity> playersNotesBySteamId;

    @OneToMany(mappedBy = "playersBySteamId")
    private Collection<PlayerKickEntity> playersKicksBySteamId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEntity that = (PlayerEntity) o;
        return Objects.equals(steamId, that.steamId) && Objects.equals(name, that.name) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steamId, name, createTime);
    }
}
