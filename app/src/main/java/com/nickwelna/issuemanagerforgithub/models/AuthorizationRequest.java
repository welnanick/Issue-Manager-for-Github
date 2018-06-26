package com.nickwelna.issuemanagerforgithub.models;

import com.nickwelna.issuemanagerforgithub.BuildConfig;

public class AuthorizationRequest {

    String[] scopes = new String[]{"public_repo"};
    String client_id = "edbecfebd9eb72b5bb21";
    String client_secret = BuildConfig.CLIENT_SECRET;

}
