package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;

public class Issue extends IssueCommentCommon {

    String repository_url;
    String labels_url;
    String comments_url;
    String events_url;
    int number;
    String title;
    Label[] labels;
    String state;
    boolean locked;
    GithubUser assignee;
    GithubUser[] assignees;
    Milestone milestone;
    int comments;
    //TODO: Use dates for these, not strings
    String closed_at;
    PullRequest pull_request;

    public int getNumber() {

        return number;

    }

    public String getTitle() {

        return title;

    }

    public String getState() {

        return state;

    }

    public boolean isLocked() {

        return locked;

    }

    @Override
    public int describeContents() {

        return 0;

    }

    public int getComments() {

        return comments;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.url);
        dest.writeString(this.repository_url);
        dest.writeString(this.labels_url);
        dest.writeString(this.comments_url);
        dest.writeString(this.events_url);
        dest.writeString(this.html_url);
        dest.writeInt(this.id);
        dest.writeString(this.node_id);
        dest.writeInt(this.number);
        dest.writeString(this.title);
        dest.writeParcelable(this.user, flags);
        dest.writeTypedArray(this.labels, flags);
        dest.writeString(this.state);
        dest.writeByte(this.locked ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.assignee, flags);
        dest.writeTypedArray(this.assignees, flags);
        dest.writeParcelable(this.milestone, flags);
        dest.writeInt(this.comments);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.closed_at);
        dest.writeString(this.author_association);
        dest.writeParcelable(this.pull_request, flags);
        dest.writeString(this.body);

    }

    public Issue() {

    }

    protected Issue(Parcel in) {

        this.url = in.readString();
        this.repository_url = in.readString();
        this.labels_url = in.readString();
        this.comments_url = in.readString();
        this.events_url = in.readString();
        this.html_url = in.readString();
        this.id = in.readInt();
        this.node_id = in.readString();
        this.number = in.readInt();
        this.title = in.readString();
        this.user = in.readParcelable(GithubUser.class.getClassLoader());
        this.labels = in.createTypedArray(Label.CREATOR);
        this.state = in.readString();
        this.locked = in.readByte() != 0;
        this.assignee = in.readParcelable(GithubUser.class.getClassLoader());
        this.assignees = in.createTypedArray(GithubUser.CREATOR);
        this.milestone = in.readParcelable(Milestone.class.getClassLoader());
        this.comments = in.readInt();
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.closed_at = in.readString();
        this.author_association = in.readString();
        this.pull_request = in.readParcelable(PullRequest.class.getClassLoader());
        this.body = in.readString();

    }

    public static final Creator<Issue> CREATOR = new Creator<Issue>() {

        @Override
        public Issue createFromParcel(Parcel source) {

            return new Issue(source);

        }

        @Override
        public Issue[] newArray(int size) {

            return new Issue[size];

        }

    };

}
