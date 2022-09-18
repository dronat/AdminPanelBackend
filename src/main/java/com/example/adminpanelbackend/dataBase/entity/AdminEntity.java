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
@Table(name = "admins", schema = "squad")
public class AdminEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Basic
    @Column(name = "steamId", nullable = false)
    private Long steamId;

    @Basic
    @Column(name = "name", nullable = false, length = 16)
    private String name;

    @Basic
    @Column(name = "steamSign", nullable = true, length = 32)
    private String steamSign;

    @Basic
    @Column(name = "role", nullable = false)
    private Integer role;

    @Basic
    @Column(name = "avatar", nullable = true, length = 255)
    private String avatar;

    @Basic
    @Column(name = "avatarMedium", nullable = true, length = 255)
    private String avatarMedium;

    @Basic
    @Column(name = "avatarFull", nullable = true, length = 255)
    private String avatarFull;

    @Basic
    @Column(name = "createTime", nullable = false)
    private Timestamp createTime;

    @Basic
    @Column(name = "modifiedTime", nullable = false)
    private Timestamp modifiedTime;

    @OneToMany(mappedBy = "adminsByAdminId")
    private Collection<AdminActionLogEntity> adminsActionLogsById;

    /*@OneToOne(mappedBy = "primary_id")
    private SpringSessionEntity springSessionById;*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminEntity that = (AdminEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(steamId, that.steamId) && Objects.equals(steamSign, that.steamSign) && Objects.equals(role, that.role) && Objects.equals(avatar, that.avatar) && Objects.equals(avatarMedium, that.avatarMedium) && Objects.equals(avatarFull, that.avatarFull) && Objects.equals(createTime, that.createTime) && Objects.equals(modifiedTime, that.modifiedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, steamId, steamSign, role, avatar, avatarMedium, avatarFull, createTime, modifiedTime);
    }
}
