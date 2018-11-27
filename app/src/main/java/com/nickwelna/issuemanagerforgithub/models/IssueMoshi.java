package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

import java.util.List;

public class IssueMoshi extends IssueCommentCommonMoshi implements Parcelable {
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
    private List<LabelMoshi> labels;
    private String state;
    private boolean locked;
    private GithubUserMoshi assignee;
    private GithubUserMoshi[] assignees;
    private MilestoneMoshi milestone;
    private int comments;
    @Json(name = "closed_at")
    private String closedAt;
    @Json(name = "pull_request")
    private PullRequest pullRequest;

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

    public IssueMoshi() {
    }

    protected IssueMoshi(Parcel in) {
        this.repositoryUrl = in.readString();
        this.labelsUrl = in.readString();
        this.commentsUrl = in.readString();
        this.eventsUrl = in.readString();
        this.number = in.readInt();
        this.title = in.readString();
        this.labels = in.createTypedArrayList(LabelMoshi.CREATOR);
        this.state = in.readString();
        this.locked = in.readByte() != 0;
        this.assignee = in.readParcelable(GithubUserMoshi.class.getClassLoader());
        this.assignees = in.createTypedArray(GithubUserMoshi.CREATOR);
        this.milestone = in.readParcelable(MilestoneMoshi.class.getClassLoader());
        this.comments = in.readInt();
        this.closedAt = in.readString();
        this.pullRequest = in.readParcelable(PullRequest.class.getClassLoader());
    }

    public static final Parcelable.Creator<IssueMoshi> CREATOR = new Parcelable.Creator<IssueMoshi>() {
        @Override
        public IssueMoshi createFromParcel(Parcel source) {
            return new IssueMoshi(source);
        }

        @Override
        public IssueMoshi[] newArray(int size) {
            return new IssueMoshi[size];
        }
    };
}
