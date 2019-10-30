package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

public final class GithubUser implements Parcelable {
    public static final Parcelable.Creator<GithubUser> CREATOR = new Parcelable.Creator<GithubUser>() {
        @Override
        public GithubUser createFromParcel(Parcel source) {
            return new GithubUser(source);
        }

        @Override
        public GithubUser[] newArray(int size) {
            return new GithubUser[size];
        }
    };
    int id;
    @Json(name = "node_id")
    String node_id;
    String url;
    private String login;
    @Json(name = "avatar_url")
    private String avatarUrl;
    @Json(name = "gravatar_url")
    private String gravatarUrl;
    @Json(name = "html_url")
    private String htmlUrl;
    @Json(name = "followers_url")
    private String followersUrl;
    @Json(name = "following_url")
    private String followingUrl;
    @Json(name = "gists_url")
    private String gistsUrl;
    @Json(name = "starred_url")
    private String starredUrl;
    @Json(name = "subscriptions_url")
    private String subscriptionsUrl;
    @Json(name = "organizations_url")
    private String organizationsUrl;
    @Json(name = "repos_url")
    private String reposUrl;
    @Json(name = "events_url")
    private String eventsUrl;
    @Json(name = "received_events_url")
    private String receivedEventsUrl;
    private String type;
    @Json(name = "site_admin")
    private boolean siteAdmin;

    public GithubUser() {
    }

    protected GithubUser(Parcel in) {
        this.login = in.readString();
        this.id = in.readInt();
        this.node_id = in.readString();
        this.avatarUrl = in.readString();
        this.gravatarUrl = in.readString();
        this.url = in.readString();
        this.htmlUrl = in.readString();
        this.followersUrl = in.readString();
        this.followingUrl = in.readString();
        this.gistsUrl = in.readString();
        this.starredUrl = in.readString();
        this.subscriptionsUrl = in.readString();
        this.organizationsUrl = in.readString();
        this.reposUrl = in.readString();
        this.eventsUrl = in.readString();
        this.receivedEventsUrl = in.readString();
        this.type = in.readString();
        this.siteAdmin = in.readByte() != 0;
    }

    public String getLogin() {
        return login;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.login);
        dest.writeInt(this.id);
        dest.writeString(this.node_id);
        dest.writeString(this.avatarUrl);
        dest.writeString(this.gravatarUrl);
        dest.writeString(this.url);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.followersUrl);
        dest.writeString(this.followingUrl);
        dest.writeString(this.gistsUrl);
        dest.writeString(this.starredUrl);
        dest.writeString(this.subscriptionsUrl);
        dest.writeString(this.organizationsUrl);
        dest.writeString(this.reposUrl);
        dest.writeString(this.eventsUrl);
        dest.writeString(this.receivedEventsUrl);
        dest.writeString(this.type);
        dest.writeByte(this.siteAdmin ? (byte) 1 : (byte) 0);
    }
}
