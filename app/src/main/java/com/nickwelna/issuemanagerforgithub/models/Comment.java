package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;

public class Comment extends IssueCommentCommon {

    String issue_url;

    @Override
    public int describeContents() {

        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.issue_url);
        dest.writeString(this.url);
        dest.writeString(this.html_url);
        dest.writeInt(this.id);
        dest.writeString(this.node_id);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.author_association);
        dest.writeString(this.body);

    }

    public Comment() {

    }

    protected Comment(Parcel in) {

        this.issue_url = in.readString();
        this.url = in.readString();
        this.html_url = in.readString();
        this.id = in.readInt();
        this.node_id = in.readString();
        this.user = in.readParcelable(GithubUser.class.getClassLoader());
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.author_association = in.readString();
        this.body = in.readString();

    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {

        @Override
        public Comment createFromParcel(Parcel source) {

            return new Comment(source);

        }

        @Override
        public Comment[] newArray(int size) {

            return new Comment[size];

        }

    };

}
