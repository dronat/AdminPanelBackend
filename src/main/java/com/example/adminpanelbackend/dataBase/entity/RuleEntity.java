package com.example.adminpanelbackend.dataBase.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "rules", schema = "squad")
public class RuleEntity implements Serializable {
    @JsonBackReference
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "position", nullable = false)
    private Integer position;

    @Basic
    @Column(name = "name", nullable = false, length = 1000)
    private String name;

    @JsonBackReference("rule-group")
    @ManyToOne
    @JoinColumn(name = "ruleGroup", referencedColumnName = "id", nullable = false)
    private RuleGroupEntity ruleGroup;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleEntity that = (RuleEntity) o;
        return Objects.equals(id, that.id)  && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
