package com.nickwelna.issuemanagerforgithub.models;

public class AuthorizationResponse {

    int id;
    String url;
    GithubApp app;
    String token;
    String hashed_token;
    String token_last_eight;
    String note;
    String note_url;
    //TODO: Replace these with Date class
    String created_at;
    String updated_at;
    String[] scopes;
    String fingerprint;

    public String getToken() {

        return token;

    }

}
