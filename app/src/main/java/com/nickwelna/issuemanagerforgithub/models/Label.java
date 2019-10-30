package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

public final class Label implements Parcelable {
    public static final Parcelable.Creator<Label> CREATOR = new Parcelable.Creator<Label>() {
        @Override
        public Label createFromParcel(Parcel source) {
            return new Label(source);
        }

        @Override
        public Label[] newArray(int size) {
            return new Label[size];
        }
    };
    int id;
    String url;
    String name;
    @Json(name = "node_id")
    private String nodeId;
    private String description;
    private String color;
    @Json(name = "default")
    private boolean isDefault;

    public Label() {
    }

    protected Label(Parcel in) {
        this.id = in.readInt();
        this.nodeId = in.readString();
        this.url = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.color = in.readString();
        this.isDefault = in.readByte() != 0;
    }

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
}
