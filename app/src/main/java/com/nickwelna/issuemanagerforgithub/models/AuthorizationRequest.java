package com.nickwelna.issuemanagerforgithub.models;

import com.nickwelna.issuemanagerforgithub.BuildConfig;
import com.squareup.moshi.Json;

public final class AuthorizationRequest {
    String[] scopes = new String[]{"public_repo"};
    @Json(name = "client_id")
    String clientId = "edbecfebd9eb72b5bb21";
    @Json(name = "client_secret")
    String clientSecret = BuildConfig.CLIENT_SECRET;
}
