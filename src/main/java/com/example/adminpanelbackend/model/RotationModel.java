package com.example.adminpanelbackend.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RotationModel {
    private List<Rotation> rotationList;

    @Data
    @Accessors(chain = true)
    public static class Rotation {
        private int position;
        private int mapId;
    }
}
