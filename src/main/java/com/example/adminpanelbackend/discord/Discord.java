package com.example.adminpanelbackend.discord;

import com.example.adminpanelbackend.db.EntityManager;
import com.example.adminpanelbackend.db.entity.DiscordMessageIdEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woop.Squad4J.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;


public class Discord {
    private final Logger LOGGER = LoggerFactory.getLogger(Discord.class);
    private final String url = ConfigLoader.get("server.discordUrl", String.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String noBansJson = "{\"content\": null,\"embeds\": [{\"title\": \"Активные баны отсутствуют\",\"color\": 15548997}],\"attachments\": []}";
    private final ObjectMapper serializer = new ObjectMapper();
    private boolean init = false;
    private HashMap<String, String> sentMessages;
    private EntityManager entityManager;

    public void init() {
        if (init) {
            return;
        }
        entityManager = EntityManager.getInstance();
        sentMessages = new HashMap<>();
        List<DiscordMessageIdEntity> result = entityManager.getAllDiscordMessagesId();
        if (!result.isEmpty()) {
            result.forEach(discordMessage -> sentMessages.put(discordMessage.getMessageId(), discordMessage.getTitle()));
        }
        init = true;
        LOGGER.info("Discord module was initialized");
    }

    public void actualizeBanMessage(DiscordMessageDTO dto) {
        if (!init) {
            throw new RuntimeException("Discord module called, but not initialized");
        }

        List<DiscordMessageDTO.Embed> embeds = dto.getEmbeds();
        dto.getEmbeds().forEach(embed -> {
            if (!sentMessages.containsValue(embed.getTitle())) {
                try {
                    sendNewMessage(serializer.writeValueAsString(new DiscordMessageDTO().addEmbded(embed)), embed.getTitle());
                } catch (JsonProcessingException e) {
                    LOGGER.error("Exception while trying serialize DiscordMessageDTO to JSON", e);
                }
            }
        });

        List<String> embdedTitles = embeds.stream().map(DiscordMessageDTO.Embed::getTitle).toList();
        Iterator<Map.Entry<String, String>> it = sentMessages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> item = it.next();
            if (!embdedTitles.contains(item.getValue())) {
                deleteMessage(item.getKey());
                it.remove();
            }
        }
        LOGGER.info("Discord messages were updated");
    }

    private void sendNewMessage(String jsonBody, String title) {
        try {
            ResponseEntity<DiscordMessageResponse> response = restTemplate.postForEntity(url + "?wait=true", getHttpEntityWithJsonBody(jsonBody), DiscordMessageResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("Discord message sent");
                sentMessages.put(Objects.requireNonNull(response.getBody()).getId(), title);
                entityManager.addDiscordMessageId(response.getBody().getId(), title);

            } else {
                throw new RuntimeException("Discord response status not 2xx");
            }
        } catch (Exception e) {
            LOGGER.error("Error while trying send message to discord", e);
        }
    }

    private void deleteMessage(String messageId) {
        try {
            restTemplate.delete(url + "/messages/" + messageId);
            entityManager.deleteRowByMessageId(messageId);
        } catch (Exception e) {
            LOGGER.error("Error while trying send message to discord", e);
            throw new RuntimeException(e);
        }
    }

    public void sendNoActiveBans() {
        if (!init) {
            return;
        }
        try {
            if (!sentMessages.isEmpty()) {
                Iterator<Map.Entry<String, String>> it = sentMessages.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> item = it.next();
                    if (item.getValue().equals("Активные баны отсутствуют")) {
                        continue;
                    }
                    deleteMessage(item.getKey());
                    it.remove();
                }
            }
            if (sentMessages.size() == 1 && sentMessages.containsValue("Активные баны отсутствуют")) {
                return;
            }
            ResponseEntity<DiscordMessageResponse> response = restTemplate.postForEntity(url + "?wait=true", getHttpEntityWithJsonBody(noBansJson), DiscordMessageResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("Discord message with empty bans was sent");
            } else {
                LOGGER.error("Can't init discord module");
                throw new RuntimeException("Discord response status not 2xx");
            }
            if (response.getBody().getId() == null || response.getBody().getId().isEmpty()) {
                LOGGER.error("Can't init discord module");
                throw new RuntimeException("Discord response id is null or empty");
            }
            sentMessages.put(response.getBody().getId(), "Активные баны отсутствуют");
            entityManager.addDiscordMessageId(response.getBody().getId(), "Активные баны отсутствуют");
        } catch (Exception e) {
            LOGGER.error("Error while trying send noActiveBans message to discord", e);
            throw new RuntimeException(e);
        }
    }

    private HttpEntity<String> getHttpEntityWithJsonBody(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonBody, headers);
    }
}