package com.example.adminpanelbackend.dataBase.entity;

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
@Table(name = "admins_action_log", schema = "squad")
public class AdminActionLogEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Basic
    @Column(name = "reason")
    private String reason;

    @Basic
    @Column(name = "createTime", nullable = false, insertable = false)
    private Timestamp createTime;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "admin", referencedColumnName = "steamId", nullable = false)
    private AdminEntity admin;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "player", referencedColumnName = "steamId")
    private PlayerEntity player;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminActionLogEntity that = (AdminActionLogEntity) o;
        return Objects.equals(id, that.id)  && Objects.equals(action, that.action) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, action, createTime);
    }
}
