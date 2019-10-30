package com.nickwelna.issuemanagerforgithub.models;

import com.jakewharton.nopen.annotation.Open;
import com.squareup.moshi.Json;

@Open
public class IssueCommentCommon {
    String url;
    @Json(name = "html_url")
    String htmlUrl;
    int id;
    @Json(name = "node_id")
    String nodeId;
    @Json(name = "updated_at")
    String updatedAt;
    @Json(name = "author_association")
    String authorAssociation;
    private GithubUser user;
    @Json(name = "created_at")
    private String createdAt;
    private String body;

    public GithubUser getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getId() {
        return id;
    }
}
