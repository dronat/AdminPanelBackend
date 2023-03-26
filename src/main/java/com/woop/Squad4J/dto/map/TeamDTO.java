package com.woop.Squad4J.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDTO {

    @JsonProperty("faction")
    private String faction;

    @JsonProperty("teamSetupName")
    private String teamSetupName;

    @JsonProperty("tickets")
    private String tickets;

    /*@JsonProperty("playerPercent")
    private String playerPercent;*/

    /*@JsonProperty("disabledVeh")
    private String disabledVeh;*/

    /*@JsonProperty("intelOnEnemy")
    private String intelOnEnemy;*/

    /*@JsonProperty("actions")
    private String actions;*/

    /*@JsonProperty("commander")
    private String commander;*/

    @JsonProperty("vehicles")
    private List<VehicleDTO> vehicles;
}
