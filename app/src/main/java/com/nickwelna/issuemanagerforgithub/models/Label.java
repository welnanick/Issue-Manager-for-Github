package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

class Label implements Parcelable {

    int id;
    String node_id;
    String url;
    String name;
    String description;
    String color;
    @SerializedName("default")
    boolean isDefault;

    @Override
    public int describeContents() {

        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(this.id);
        dest.writeString(this.node_id);
        dest.writeString(this.url);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.color);
        dest.writeByte(this.isDefault ? (byte) 1 : (byte) 0);

    }

    public Label() {

    }

    protected Label(Parcel in) {

        this.id = in.readInt();
        this.node_id = in.readString();
        this.url = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.color = in.readString();
        this.isDefault = in.readByte() != 0;

    }

    public static final Creator<Label> CREATOR = new Creator<Label>() {

        @Override
        public Label createFromParcel(Parcel source) {

            return new Label(source);

        }

        @Override
        public Label[] newArray(int size) {

            return new Label[size];

        }

    };

}
