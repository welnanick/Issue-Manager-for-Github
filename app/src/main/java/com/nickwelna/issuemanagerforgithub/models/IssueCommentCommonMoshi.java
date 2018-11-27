package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public class IssueCommentCommonMoshi {
    String url;
    @Json(name = "html_url")
    String htmlUrl;
    int id;
    @Json(name = "node_id")
    String nodeId;
    private GithubUserMoshi user;
    @Json(name = "created_at")
    private String createdAt;
    @Json(name = "updated_at")
    String updatedAt;
    @Json(name = "author_association")
    String authorAssociation;
    private String body;

    public GithubUserMoshi getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
