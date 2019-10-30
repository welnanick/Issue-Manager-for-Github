package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

public final class License implements Parcelable {

    public static final Creator<License> CREATOR = new Creator<License>() {

        @Override
        public License createFromParcel(Parcel source) {
            return new License(source);
        }

        @Override
        public License[] newArray(int size) {
            return new License[size];
        }
    };
    String key;
    String name;
    @Json(name = "spdx_id")
    String spdxId;
    String url;
    @Json(name = "node_id")
    String nodeId;

    public License() {
    }

    protected License(Parcel in) {
        this.key = in.readString();
        this.name = in.readString();
        this.spdxId = in.readString();
        this.url = in.readString();
        this.nodeId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.name);
        dest.writeString(this.spdxId);
        dest.writeString(this.url);
        dest.writeString(this.nodeId);
    }
}
