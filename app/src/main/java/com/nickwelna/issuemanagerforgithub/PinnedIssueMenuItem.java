package com.nickwelna.issuemanagerforgithub;

class PinnedIssueMenuItem {

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

}
