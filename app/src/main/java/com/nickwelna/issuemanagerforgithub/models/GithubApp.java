package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public final class GithubApp {

    String name;
    String url;
    @Json(name = "client_id")
    String clientId;

}
