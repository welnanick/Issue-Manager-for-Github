package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

class Milestone implements Parcelable {

    String url;
    String html_url;
    String labels_url;
    int id;
    String node_id;
    int number;
    String state;
    String title;
    String description;
    GithubUser creator;
    int open_issues;
    int closed_issues;
    //TODO: Use dates for these instead of strings
    String created_at;
    String updated_at;
    String closed_at;
    String due_on;

    @Override
    public int describeContents() {

        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.url);
        dest.writeString(this.html_url);
        dest.writeString(this.labels_url);
        dest.writeInt(this.id);
        dest.writeString(this.node_id);
        dest.writeInt(this.number);
        dest.writeString(this.state);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeParcelable(this.creator, flags);
        dest.writeInt(this.open_issues);
        dest.writeInt(this.closed_issues);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.closed_at);
        dest.writeString(this.due_on);

    }

    public Milestone() {

    }

    protected Milestone(Parcel in) {

        this.url = in.readString();
        this.html_url = in.readString();
        this.labels_url = in.readString();
        this.id = in.readInt();
        this.node_id = in.readString();
        this.number = in.readInt();
        this.state = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.creator = in.readParcelable(GithubUser.class.getClassLoader());
        this.open_issues = in.readInt();
        this.closed_issues = in.readInt();
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.closed_at = in.readString();
        this.due_on = in.readString();

    }

    public static final Creator<Milestone> CREATOR = new Creator<Milestone>() {

        @Override
        public Milestone createFromParcel(Parcel source) {

            return new Milestone(source);

        }

        @Override
        public Milestone[] newArray(int size) {

            return new Milestone[size];

        }

    };

}
