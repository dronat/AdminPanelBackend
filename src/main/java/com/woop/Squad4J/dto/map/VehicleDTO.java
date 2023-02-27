package com.woop.Squad4J.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleDTO {

    @JsonProperty("type")
    private String type;

    @JsonProperty("count")
    private int count;

    @JsonProperty("delay")
    private int delay;

    @JsonProperty("respawnTime")
    private int respawnTime;

    @JsonProperty("rawType")
    private String rawType;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("spawner_Size")
    private String spawnerSize;
}
