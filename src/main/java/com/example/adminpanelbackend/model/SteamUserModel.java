package com.example.adminpanelbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SteamUserModel {
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Accessors(chain = true)
    public static class Response {
        private List<Player> players;

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Player {
            private String steamid;
            private String personaname;
            private String profileurl;
            private String avatar;
            private String avatarmedium;
            private String avatarfull;
            private String avatarhash;
            private Integer lastlogoff;
            private Integer timecreated;
        }
    }
}
