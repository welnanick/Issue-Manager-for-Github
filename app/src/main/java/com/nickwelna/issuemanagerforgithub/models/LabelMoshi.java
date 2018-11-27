package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

public class LabelMoshi implements Parcelable {
    int id;
    @Json(name = "node_id")
    private String nodeId;
    String url;
    String name;
    private String description;
    private String color;
    @Json(name = "default")
    private boolean isDefault;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.nodeId);
        dest.writeString(this.url);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.color);
        dest.writeByte(this.isDefault ? (byte) 1 : (byte) 0);
    }

    public LabelMoshi() {
    }

    protected LabelMoshi(Parcel in) {
        this.id = in.readInt();
        this.nodeId = in.readString();
        this.url = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.color = in.readString();
        this.isDefault = in.readByte() != 0;
    }

    public static final Parcelable.Creator<LabelMoshi> CREATOR = new Parcelable.Creator<LabelMoshi>() {
        @Override
        public LabelMoshi createFromParcel(Parcel source) {
            return new LabelMoshi(source);
        }

        @Override
        public LabelMoshi[] newArray(int size) {
            return new LabelMoshi[size];
        }
    };
}
