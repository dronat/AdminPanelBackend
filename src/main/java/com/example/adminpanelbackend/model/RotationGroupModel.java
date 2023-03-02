package com.example.adminpanelbackend.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RotationGroupModel {
    private String name;

    private List<RotationMapModel> maps;

    @Data
    @Accessors(chain = true)
    public static class RotationMapModel {

        private int mapId;

        private int position;
    }
}
