package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GithubUser implements Parcelable {

    String login;
    int id;
    String node_id;
    String avatar_url;
    String gravatar_url;
    String url;
    String html_url;
    String followers_url;
    String following_url;
    String gists_url;
    String starred_url;
    String subscriptions_url;
    String organizations_url;
    String repos_url;
    String events_url;
    String received_events_url;
    String type;
    boolean site_admin;

    public String getLogin() {

        return login;

    }

    public String getAvatar_url() {

        return avatar_url;

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
        dest.writeString(this.avatar_url);
        dest.writeString(this.gravatar_url);
        dest.writeString(this.url);
        dest.writeString(this.html_url);
        dest.writeString(this.followers_url);
        dest.writeString(this.following_url);
        dest.writeString(this.gists_url);
        dest.writeString(this.starred_url);
        dest.writeString(this.subscriptions_url);
        dest.writeString(this.organizations_url);
        dest.writeString(this.repos_url);
        dest.writeString(this.events_url);
        dest.writeString(this.received_events_url);
        dest.writeString(this.type);
        dest.writeByte(this.site_admin ? (byte) 1 : (byte) 0);

    }

    public GithubUser() {

    }

    protected GithubUser(Parcel in) {

        this.login = in.readString();
        this.id = in.readInt();
        this.node_id = in.readString();
        this.avatar_url = in.readString();
        this.gravatar_url = in.readString();
        this.url = in.readString();
        this.html_url = in.readString();
        this.followers_url = in.readString();
        this.following_url = in.readString();
        this.gists_url = in.readString();
        this.starred_url = in.readString();
        this.subscriptions_url = in.readString();
        this.organizations_url = in.readString();
        this.repos_url = in.readString();
        this.events_url = in.readString();
        this.received_events_url = in.readString();
        this.type = in.readString();
        this.site_admin = in.readByte() != 0;

    }

    public static final Creator<GithubUser> CREATOR = new Creator<GithubUser>() {

        @Override
        public GithubUser createFromParcel(Parcel source) {

            return new GithubUser(source);

        }

        @Override
        public GithubUser[] newArray(int size) {

            return new GithubUser[size];

        }

    };

}
