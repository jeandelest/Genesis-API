package fr.insee.genesis.controller.sources.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LunaticJsonDataResponse {

    @JsonProperty("EXTERNAL")
    private LunaticJSonExternalVariables externalVariables;

    @JsonProperty("COLLECTED")
    private LunaticJsonCollectedVariables collectedVariables;
}
