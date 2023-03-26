package com.example.adminpanelbackend.db.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
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
    @Column(name = "onControl", nullable = false)
    private Boolean onControl = false;

    @Basic
    @Column(name = "createTime", nullable = false)
    private Timestamp createTime;

    @JsonBackReference
    @LazyCollection(LazyCollectionOption.TRUE)
    @OneToMany(mappedBy = "player")
    private List<PlayerBanEntity> playerBans;

    @JsonBackReference
    @LazyCollection(LazyCollectionOption.TRUE)
    @OneToMany(mappedBy = "player")
    private List<PlayerMessageEntity> playerMessages;

    @JsonBackReference
    @LazyCollection(LazyCollectionOption.TRUE)
    @OneToMany(mappedBy = "player")
    private List<PlayerNoteEntity> playerNotes;

    @JsonBackReference
    @LazyCollection(LazyCollectionOption.TRUE)
    @OneToMany(mappedBy = "player")
    private List<PlayerKickEntity> playerKicks;

    @JsonBackReference
    @LazyCollection(LazyCollectionOption.TRUE)
    @OneToMany(mappedBy = "player")
    private List<AdminActionLogEntity> playerAdminActions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEntity that = (PlayerEntity) o;
        return Objects.equals(steamId, that.steamId) && Objects.equals(name, that.name) && Objects.equals(onControl, that.onControl) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steamId, name, onControl, createTime);
    }
}
