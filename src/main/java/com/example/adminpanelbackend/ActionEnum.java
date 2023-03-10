package com.example.adminpanelbackend;

import java.util.List;

public enum ActionEnum {
    BAN_PLAYER("BanPlayer"),
    UNBAN("Unban"),
    KICK_PLAYER("KickPlayer"),
    DISBAND_SQUAD("DisbandSquad"),
    REMOVE_PLAYER_FROM_SQUAD("RemovePlayerFromSquad"),
    PLAYER_TEAM_CHANGE("PlayerTeamChange"),
    WARN_PLAYER("WarnPlayer"),
    WARN_SQUAD("WarnSquad"),
    CHANGE_CURRENT_LAYER("ChangeCurrentLayer"),
    CHANGE_NEXT_LAYER("ChangeNextLayer"),
    SEND_BROADCAST("SendBroadcast"),
    ADD_PLAYER_NOTE("AddPlayerNote"),
    DELETE_PLAYER_NOTE("DeletePlayerNote"),
    ADD_PLAYER_ON_CONTROL("AddPlayerOnControl"),
    REMOVE_PLAYER_FROM_CONTROL("RemovePlayerFromControl"),
    ADD_NEW_PLAYER("AddNewPlayer"),
    ADD_ADMIN("AddAdmin"),
    DEACTIVATE_ADMIN("DeactivateAdmin"),
    ENTERED_ADMIN_CAM("EnteredInAdminCam"),
    LEFT_FROM_ADMIN_CAM("LeftFromAdminCam");

    public final String actionName;

    ActionEnum(String actionName) {
        this.actionName = actionName;
    }

    public static List<ActionEnum> getAllActions() {
        return List.of(ActionEnum.values());
    }
}
