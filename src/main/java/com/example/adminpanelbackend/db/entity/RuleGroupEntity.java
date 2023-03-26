package com.example.adminpanelbackend.db.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "rule_groups", schema = "squad")
public class RuleGroupEntity implements Serializable {
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

    @JsonManagedReference("rule-group")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "ruleGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleEntity> rules;
}
