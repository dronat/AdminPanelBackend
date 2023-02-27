package com.woop.Squad4J.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanesDTO {

    @JsonProperty("Alpha")
    private List<String> alpha;

    @JsonProperty("Bravo")
    private List<String> bravo;

    @JsonProperty("Charlie")
    private List<String> charlie;
}
