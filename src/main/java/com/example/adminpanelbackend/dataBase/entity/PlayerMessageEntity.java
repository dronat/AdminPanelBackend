package com.example.adminpanelbackend.dataBase.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "players_messages", schema = "squad")
public class PlayerMessageEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "chatType", nullable = false, length = 16)
    private String chatType;

    @Basic
    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Basic
    @Column(name = "creationTime", nullable = false)
    private Timestamp creationTime;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "playerSteamId", referencedColumnName = "steamId", nullable = false)
    private PlayerEntity playersBySteamId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerMessageEntity that = (PlayerMessageEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(chatType, that.chatType) && Objects.equals(message, that.message) && Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatType, message, creationTime);
    }
}
