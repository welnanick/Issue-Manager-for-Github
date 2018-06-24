package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

class PullRequest implements Parcelable {

    String url;
    String html_url;
    String diff_url;
    String patch_url;

    @Override
    public int describeContents() {

        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.url);
        dest.writeString(this.html_url);
        dest.writeString(this.diff_url);
        dest.writeString(this.patch_url);

    }

    public PullRequest() {

    }

    protected PullRequest(Parcel in) {

        this.url = in.readString();
        this.html_url = in.readString();
        this.diff_url = in.readString();
        this.patch_url = in.readString();

    }

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

}
