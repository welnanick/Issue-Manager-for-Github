package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

public class MilestoneMoshi implements Parcelable {
    String url;
    @Json(name = "html_url")
    private String htmlUrl;
    @Json(name = "labels_url")
    private String labelsUrl;
    int id;
    @Json(name = "node_id")
    private String nodeId;
    private int number;
    private String state;
    private String title;
    private String description;
    GithubUserMoshi creator;
    @Json(name = "open_issues")
    private int openIssues;
    @Json(name = "closed_issues")
    private int closedIssues;
    @Json(name = "created_at")
    private String createdAt;
    @Json(name = "updated_at")
    private String updatedAt;
    @Json(name = "closed_at")
    private String closedAt;
    @Json(name = "due_on")
    private String dueOn;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.labelsUrl);
        dest.writeInt(this.id);
        dest.writeString(this.nodeId);
        dest.writeInt(this.number);
        dest.writeString(this.state);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeParcelable(this.creator, flags);
        dest.writeInt(this.openIssues);
        dest.writeInt(this.closedIssues);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);
        dest.writeString(this.closedAt);
        dest.writeString(this.dueOn);
    }

    public MilestoneMoshi() {
    }

    protected MilestoneMoshi(Parcel in) {
        this.url = in.readString();
        this.htmlUrl = in.readString();
        this.labelsUrl = in.readString();
        this.id = in.readInt();
        this.nodeId = in.readString();
        this.number = in.readInt();
        this.state = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.creator = in.readParcelable(GithubUserMoshi.class.getClassLoader());
        this.openIssues = in.readInt();
        this.closedIssues = in.readInt();
        this.createdAt = in.readString();
        this.updatedAt = in.readString();
        this.closedAt = in.readString();
        this.dueOn = in.readString();
    }

    public static final Parcelable.Creator<MilestoneMoshi> CREATOR = new Parcelable.Creator<MilestoneMoshi>() {
        @Override
        public MilestoneMoshi createFromParcel(Parcel source) {
            return new MilestoneMoshi(source);
        }

        @Override
        public MilestoneMoshi[] newArray(int size) {
            return new MilestoneMoshi[size];
        }
    };
}
