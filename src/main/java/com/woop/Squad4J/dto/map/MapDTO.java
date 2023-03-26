package com.woop.Squad4J.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapDTO {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("rawName")
    private String rawName;

    @JsonProperty("levelName")
    private String levelName;

    /*@JsonProperty("minimapTexture")
    private String minimapTexture;*/

    /*@JsonProperty("lightingLevel")
    private String lightingLevel;*/

    @JsonProperty("lighting")
    private String lighting;

    /*@JsonProperty("border")
    private List<BorderDTO> border;*/

    @JsonProperty("team1")
    private TeamDTO teamOne;

    @JsonProperty("team2")
    private TeamDTO teamTwo;

    /*@JsonProperty("type")
    private String type;*/

    /*@JsonProperty("lanes")
    private LanesDTO lanes;*/

    /*@JsonProperty("capturePoints")
    private String capturePoints;*/

    @JsonProperty("mapName")
    private String mapName;

    @JsonProperty("gamemode")
    private String gameMode;

    @JsonProperty("layerVersion")
    private String layerVersion;

    @JsonProperty("mapSize")
    private String mapSize;

    /*@JsonProperty("mapSizeType")
    private String mapSizeType;*/

    /*@JsonProperty("Flag")
    private List<String> flag;*/

    /*@JsonProperty("Number of Hexes")
    private String numberOfHexes;*/
}
