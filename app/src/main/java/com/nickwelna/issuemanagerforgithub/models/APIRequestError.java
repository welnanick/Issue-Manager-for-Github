package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public final class APIRequestError {
    @Json(name = "documentation_url")
    String documentationUrl;
    private String message;

    public String getMessage() {
        return message;
    }
}
