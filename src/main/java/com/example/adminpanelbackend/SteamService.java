package com.example.adminpanelbackend;

import com.example.adminpanelbackend.model.SteamUserModel;
import com.woop.Squad4J.server.LogParser;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class SteamService {
    private static final String steamApiKey = ConfigLoader.get("server.steamApiKey", String.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    private static final Logger LOGGER = LoggerFactory.getLogger(SteamService.class);


    public static SteamUserModel.Response.Player getSteamUserInfo(String steamId) {
        return getSteamUserInfo(Long.parseLong(steamId));
    }

    public static SteamUserModel.Response.Player getSteamUserInfo(Long steamId) {
        SteamUserModel.Response.Player steamUserModel = null;
        Exception exception = null;
        int i = 0;
        while (steamUserModel == null && i < 10) {
            i++;
            try {
                steamUserModel = restTemplate
                        .getForObject("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + steamApiKey + "&steamids=" + steamId, SteamUserModel.class)
                        .getResponse()
                        .getPlayers()
                        .get(0);
            } catch (Exception e) {
                exception = e;
                LOGGER.warn("Exception while trying get user from steam api", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
        if (steamUserModel == null) {
            LOGGER.error("Can't get steamUser from steam api", exception);
        }
        return steamUserModel;
    }
}
