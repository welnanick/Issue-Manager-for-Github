package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public class AuthorizationResponseMoshi {
    int id;
    String url;
    GithubApp app;
    private String token;
    @Json(name = "hashed_token")
    String hashedToken;
    @Json(name = "token_last_eight")
    String tokenLastEight;
    String note;
    @Json(name = "note_url")
    String noteUrl;
    @Json(name = "created_at")
    String createdAt;
    @Json(name = "updated_at")
    String updatedAt;
    String[] scopes;
    String fingerprint;

    public String getToken() {
        return token;
    }
}
