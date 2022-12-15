package com.example.adminpanelbackend.dataBase.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "discord_messages_id", schema = "squad")
public class DiscordMessageIdEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "message_id", nullable = false)
    private String messageId;
    @Basic
    @Column(name = "title", nullable = false)
    private String title;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordMessageIdEntity that = (DiscordMessageIdEntity) o;
        return Objects.equals(id, that.id)  && Objects.equals(messageId, that.messageId)  && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, messageId, title);
    }
}
