package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

final class PullRequest implements Parcelable {

    public static final Creator<PullRequest> CREATOR = new Creator<PullRequest>() {

        @Override
        public PullRequest createFromParcel(Parcel source) {
            return new PullRequest(source);
        }

        @Override
        public PullRequest[] newArray(int size) {
            return new PullRequest[size];
        }
    };
    String url;
    @Json(name = "html_url")
    String htmlUrl;
    @Json(name = "diff_url")
    String diffUrl;
    @Json(name = "patch_url")
    String patchUrl;

    public PullRequest() {
    }

    protected PullRequest(Parcel in) {
        this.url = in.readString();
        this.htmlUrl = in.readString();
        this.diffUrl = in.readString();
        this.patchUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.diffUrl);
        dest.writeString(this.patchUrl);
    }
}
