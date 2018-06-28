package com.nickwelna.issuemanagerforgithub;

import android.os.Parcel;
import android.os.Parcelable;

class PinnedIssueMenuItem implements Parcelable {

    String text;
    int number;
    int viewType;

    public PinnedIssueMenuItem(String text, int viewType) {

        this.text = text;
        this.viewType = viewType;

    }

    public PinnedIssueMenuItem(String text, int number, int viewType) {

        this.text = text;
        this.number = number;
        this.viewType = viewType;

    }

    public PinnedIssueMenuItem(int viewType) {

        this.viewType = viewType;

    }

    @Override
    public int describeContents() {

        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.text);
        dest.writeInt(this.number);
        dest.writeInt(this.viewType);

    }

    protected PinnedIssueMenuItem(Parcel in) {

        this.text = in.readString();
        this.number = in.readInt();
        this.viewType = in.readInt();

    }

    public static final Creator<PinnedIssueMenuItem> CREATOR = new Creator<PinnedIssueMenuItem>() {

        @Override
        public PinnedIssueMenuItem createFromParcel(Parcel source) {

            return new PinnedIssueMenuItem(source);

        }

        @Override
        public PinnedIssueMenuItem[] newArray(int size) {

            return new PinnedIssueMenuItem[size];

        }

    };

}
