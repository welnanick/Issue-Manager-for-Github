package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcelable;

public abstract class IssueCommentCommon implements Parcelable {

    String url;
    String html_url;
    int id;
    String node_id;
    GithubUser user;
    String created_at;
    String updated_at;
    String author_association;
    String body;

    public GithubUser getUser() {

        return user;

    }

    public String getBody() {

        return body;

    }

}
