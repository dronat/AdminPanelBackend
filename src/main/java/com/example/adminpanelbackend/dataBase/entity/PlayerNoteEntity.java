package com.example.adminpanelbackend.dataBase.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "players_notes", schema = "squad")
public class PlayerNoteEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "note", nullable = false, length = 255)
    private String note;

    @Basic
    @Column(name = "creationTime", nullable = false)
    private Timestamp creationTime;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "playerSteamId", referencedColumnName = "steamId", nullable = false)
    private PlayerEntity playersBySteamId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "adminSteamId", referencedColumnName = "steamId", nullable = false)
    private AdminEntity adminsBySteamId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerNoteEntity that = (PlayerNoteEntity) o;
        return Objects.equals(adminsBySteamId, that.adminsBySteamId) && Objects.equals(id, that.id) && Objects.equals(note, that.note) && Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, adminsBySteamId, note, creationTime);
    }
}
