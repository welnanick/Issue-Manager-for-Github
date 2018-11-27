package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public class APIRequestErrorMoshi {
    private String message;
    @Json(name = "documentation_url")
    String documentationUrl;

    public String getMessage() {
        return message;
    }
}
