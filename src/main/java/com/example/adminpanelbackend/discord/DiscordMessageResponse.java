package com.example.adminpanelbackend.discord;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DiscordMessageResponse {
    public String id;
}
