package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public class CommentMoshi extends IssueCommentCommonMoshi {
    @Json(name = "issue_url")
    String issueUrl;
}
