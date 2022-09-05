package com.example.adminpanelbackend.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class VerifySteamModel {
    private String callbackURL;
    private HashMap<String, String> openIdInfo;
    /*private String openid.return_to;
    private String openid.identity;
    private String openid.op_endpoint;
    private String openid.assoc_handle;
    private String openid.mode;
    private String openid.signed;
    private String openid.sig;
    private String openid.claimed_id;
    private String openid.response_nonce;
    private String openid.ns;*/
}
