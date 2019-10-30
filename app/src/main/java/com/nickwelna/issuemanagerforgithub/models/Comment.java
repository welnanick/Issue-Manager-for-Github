package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public final class Comment extends IssueCommentCommon {
    @Json(name = "issue_url")
    String issueUrl;

    public String getIssueUrl() {
        return issueUrl;
    }
}
