package com.example.adminpanelbackend.model;

import com.example.adminpanelbackend.db.entity.RuleGroupEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedList;

@Data
@Accessors(chain = true)
public class RuleGroupModel {
    private LinkedList<RuleGroupEntity> ruleGroup;
}
