package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

import java.util.List;

public final class Issue extends IssueCommentCommon implements Parcelable {
    public static final Parcelable.Creator<Issue> CREATOR = new Parcelable.Creator<Issue>() {
        @Override
        public Issue createFromParcel(Parcel source) {
            return new Issue(source);
        }

        @Override
        public Issue[] newArray(int size) {
            return new Issue[size];
        }
    };
    @Json(name = "repository_url")
    private String repositoryUrl;
    @Json(name = "labels_url")
    private String labelsUrl;
    @Json(name = "comments_url")
    private String commentsUrl;
    @Json(name = "events_url")
    private String eventsUrl;
    private int number;
    private String title;
    private List<Label> labels;
    private String state;
    private boolean locked;
    private GithubUser assignee;
    private GithubUser[] assignees;
    private Milestone milestone;
    private int comments;
    @Json(name = "closed_at")
    private String closedAt;
    @Json(name = "pull_request")
    private PullRequest pullRequest;

    public Issue() {
    }

    protected Issue(Parcel in) {
        this.repositoryUrl = in.readString();
        this.labelsUrl = in.readString();
        this.commentsUrl = in.readString();
        this.eventsUrl = in.readString();
        this.number = in.readInt();
        this.title = in.readString();
        this.labels = in.createTypedArrayList(Label.CREATOR);
        this.state = in.readString();
        this.locked = in.readByte() != 0;
        this.assignee = in.readParcelable(GithubUser.class.getClassLoader());
        this.assignees = in.createTypedArray(GithubUser.CREATOR);
        this.milestone = in.readParcelable(Milestone.class.getClassLoader());
        this.comments = in.readInt();
        this.closedAt = in.readString();
        this.pullRequest = in.readParcelable(PullRequest.class.getClassLoader());
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getState() {
        return state;
    }

    public int getComments() {
        return comments;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.repositoryUrl);
        dest.writeString(this.labelsUrl);
        dest.writeString(this.commentsUrl);
        dest.writeString(this.eventsUrl);
        dest.writeInt(this.number);
        dest.writeString(this.title);
        dest.writeTypedList(this.labels);
        dest.writeString(this.state);
        dest.writeByte(this.locked ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.assignee, flags);
        dest.writeTypedArray(this.assignees, flags);
        dest.writeParcelable(this.milestone, flags);
        dest.writeInt(this.comments);
        dest.writeString(this.closedAt);
        dest.writeParcelable(this.pullRequest, flags);
    }
}
