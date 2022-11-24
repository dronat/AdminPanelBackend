package com.example.adminpanelbackend;

import com.example.adminpanelbackend.model.SteamUserModel;
import com.woop.Squad4J.util.ConfigLoader;
import org.springframework.web.client.RestTemplate;

public class SteamService {
    private static final String steamApiKey = ConfigLoader.get("server.steamApiKey", String.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    public static SteamUserModel.Response.Player getSteamUserInfo(String steamId) {
        return getSteamUserInfo(Long.parseLong(steamId));
    }

    public static SteamUserModel.Response.Player getSteamUserInfo(Long steamId) {
        return restTemplate
                .getForObject("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + steamApiKey + "&steamids=" + steamId, SteamUserModel.class)
                .getResponse()
                .getPlayers()
                .get(0);
    }
}
