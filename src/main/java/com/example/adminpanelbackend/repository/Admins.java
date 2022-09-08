package com.example.adminpanelbackend.repository;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Setter @Getter
@Accessors(chain = true)
@Entity
public class Admins implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "name", nullable = false, length = 16)
    private String name;
    @Basic
    @Column(name = "steamId", nullable = false)
    private Long steamId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Admins that = (Admins) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (steamId != null ? !steamId.equals(that.steamId) : that.steamId != null) return false;
        if (steamSign != null ? !steamSign.equals(that.steamSign) : that.steamSign != null) return false;
        if (role != null ? !role.equals(that.role) : that.role != null) return false;
        if (avatar != null ? !avatar.equals(that.avatar) : that.avatar != null) return false;
        if (avatarMedium != null ? !avatarMedium.equals(that.avatarMedium) : that.avatarMedium != null) return false;
        if (avatarFull != null ? !avatarFull.equals(that.avatarFull) : that.avatarFull != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (modifiedTime != null ? !modifiedTime.equals(that.modifiedTime) : that.modifiedTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (steamId != null ? steamId.hashCode() : 0);
        result = 31 * result + (steamSign != null ? steamSign.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (avatarMedium != null ? avatarMedium.hashCode() : 0);
        result = 31 * result + (avatarFull != null ? avatarFull.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (modifiedTime != null ? modifiedTime.hashCode() : 0);
        return result;
    }
}
