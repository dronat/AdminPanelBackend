package com.example.adminpanelbackend.controller;

import com.example.adminpanelbackend.SteamOpenID;
import com.example.adminpanelbackend.SteamService;
import com.example.adminpanelbackend.dataBase.EntityManager;
import com.example.adminpanelbackend.dataBase.entity.AdminEntity;
import com.example.adminpanelbackend.model.SteamUserModel;
import com.example.adminpanelbackend.model.VerifySteamModel;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;

@RestController()
@CrossOrigin
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 604800)
public class NotSecureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotSecureController.class);
    private final SteamOpenID steamOpenID = new SteamOpenID();
    EntityManager entityManager = new EntityManager();

    @PostMapping(path = "/get-steam-link")
    public ResponseEntity<HashMap<String, String>> getSteamLink(HttpSession httpSession,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                @RequestParam String callbackURL) {
        LOGGER.debug("Received unsecured GET request on '{}' with callbackURL '{}'", request.getRequestURL(), callbackURL);
        String steamLink = steamOpenID.login(callbackURL);
        if (steamLink == null || steamLink.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok(
                new HashMap<>() {{
                    put("steamLink", steamOpenID.login(callbackURL));
                }}
        );
    }

    @PostMapping(path = "/verify-steam")
    public ResponseEntity<String> verifySteam(HttpSession httpSession,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              @RequestBody VerifySteamModel verifySteamModel) {
        LOGGER.debug("Received unsecured POST request on '{}' with body '{}'", request.getRequestURL(), verifySteamModel);
        String steamId = steamOpenID.verify(verifySteamModel);
        if (steamId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SteamUserModel.Response.Player steamUser = SteamService.getSteamUserInfo(steamId);
        AdminEntity adminEntity = entityManager.getAdminBySteamID(Long.parseLong(steamId));
        if (adminEntity == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        adminEntity.setName(steamUser.getPersonaname())
                .setAvatar(steamUser.getAvatar())
                .setAvatarMedium(steamUser.getAvatarmedium())
                .setAvatarFull(steamUser.getAvatarfull());

        entityManager.update(adminEntity);
        httpSession.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, String.valueOf(adminEntity.getSteamId()));
        httpSession.setAttribute("userInfo", adminEntity);
        return ResponseEntity.ok(Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("SESSION")).findFirst().orElseThrow().getValue());
    }

    /*@GetMapping(path = "/verify-steam-return")
    public ResponseEntity<Void> verifySteame(@RequestParam("openid.return_to") String return_to,
                                                                @RequestParam("openid.identity") String identity,
                                                                @RequestParam("openid.op_endpoint") String op_endpoint,
                                                                @RequestParam("openid.assoc_handle") String assoc_handle,
                                                                @RequestParam("openid.mode") String mode,
                                                                @RequestParam("openid.signed") String signed,
                                                                @RequestParam("openid.sig") String sig,
                                                                @RequestParam("openid.claimed_id") String claimed_id,
                                                                @RequestParam("openid.response_nonce") String response_nonce,
                                                                @RequestParam("openid.ns") String ns,
                                                                HttpSession httpSession,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
        LOGGER.debug("Received unsecured GET request on '" + request.getRequestURL() + "'");
        VerifySteamModel verifySteamModel = new VerifySteamModel()
                .setCallbackURL(return_to)
                //.setCallbackURL("http://10.66.66.10/verify-steam-return")
                .setOpenIdInfo(new HashMap<>() {{
                    put("openid.return_to", return_to);
                    put("openid.identity", identity);
                    put("openid.op_endpoint", op_endpoint);
                    put("openid.assoc_handle", assoc_handle);
                    put("openid.mode", mode);
                    put("openid.signed", signed);
                    put("openid.sig", sig);
                    put("openid.claimed_id", claimed_id);
                    put("openid.response_nonce", response_nonce);
                    put("openid.ns", ns);
                }});

        String steamId = steamOpenID.verify(verifySteamModel);
        if (steamId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String steamApiKey = ConfigLoader.get("server.steamApiKey", String.class);
        SteamUserModel.Response.Player steamUser = restTemplate
                .getForObject("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + steamApiKey + "&steamids=" + steamId, SteamUserModel.class)
                .getResponse()
                .getPlayers()
                .get(0);
        AdminEntity adminEntity = entityManager.getAdminBySteamID(Long.parseLong(steamId));
        if (adminEntity == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        adminEntity
                .setName(steamUser.getPersonaname())
                .setAvatar(steamUser.getAvatar())
                .setAvatarMedium(steamUser.getAvatarmedium())
                .setAvatarFull(steamUser.getAvatarfull());

        entityManager.update(adminEntity);
        httpSession.setAttribute("userInfo", adminEntity);
        return ResponseEntity.ok().build();
    }*/



    /*@GetMapping("/indx")
    public String index(Model model, HttpSession session) {
        List<String> msgs = (List<String>) session.getAttribute("MY_MESSAGES");
        if(msgs == null) {
            msgs = new ArrayList<>();
        }
        model.addAttribute("messages", msgs);
        return "index";
    }

    @GetMapping("/messages")
    public String saveMessage(@RequestParam String msg, HttpServletRequest request)
    {
        List<String> msgs = (List<String>) request.getSession().getAttribute("MY_MESSAGES");
        if(msgs == null) {
            msgs = new ArrayList<>();
            request.getSession().setAttribute("MY_MESSAGES", msgs);
        }
        msgs.add(msg);
        return "redirect:/";
    }*/

    // public boolean checkAuth(SteamUserModel)
}
