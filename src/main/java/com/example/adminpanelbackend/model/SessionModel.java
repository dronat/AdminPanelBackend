package com.example.adminpanelbackend.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SessionModel {
    private String steamId;
    private String steamName;
    private String steamSign;
}
