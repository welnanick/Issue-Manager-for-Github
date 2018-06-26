package com.nickwelna.issuemanagerforgithub;

class PinnedIssueMenuItem {

    String text;
    String subText;
    int viewType;

    public PinnedIssueMenuItem(String text, int viewType) {

        this.text = text;
        this.viewType = viewType;

    }

    public PinnedIssueMenuItem(String text, String subText, int viewType) {

        this.text = text;
        this.subText = subText;
        this.viewType = viewType;

    }

    public PinnedIssueMenuItem(int viewType) {

        this.viewType = viewType;

    }
}
