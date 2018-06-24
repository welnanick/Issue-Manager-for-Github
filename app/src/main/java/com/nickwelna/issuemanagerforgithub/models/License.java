package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class License implements Parcelable {

    String key;
    String name;
    String spdx_id;
    String url;
    String node_id;

    @Override
    public int describeContents() {

        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.key);
        dest.writeString(this.name);
        dest.writeString(this.spdx_id);
        dest.writeString(this.url);
        dest.writeString(this.node_id);

    }

    public License() {

    }

    protected License(Parcel in) {

        this.key = in.readString();
        this.name = in.readString();
        this.spdx_id = in.readString();
        this.url = in.readString();
        this.node_id = in.readString();

    }

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

}
