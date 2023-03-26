package com.woop.Squad4J.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BorderDTO {

    @JsonProperty("point")
    private String point;

    @JsonProperty("location_x")
    private String locationX;

    @JsonProperty("location_y")
    private String locationY;

    @JsonProperty("location_z")
    private String locationZ;
}
