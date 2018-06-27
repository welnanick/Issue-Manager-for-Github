package com.nickwelna.issuemanagerforgithub.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Repository implements Parcelable {

    int id;
    String node_id;
    String name;
    String full_name;
    GithubUser owner;
    @SerializedName("private")
    boolean isPrivate;
    String html_url;
    String description;
    boolean fork;
    String url;
    String forks_url;
    String keys_url;
    String collaborators_url;
    String teams_url;
    String hooks_url;
    String issue_events_url;
    String events_url;
    String assignees_url;
    String branches_url;
    String tags_url;
    String blobs_url;
    String git_tags_url;
    String git_refs_url;
    String trees_url;
    String statuses_url;
    String languages_url;
    String stargazers_url;
    String contributors_url;
    String subscribers_url;
    String subscription_url;
    String commits_url;
    String git_commits_url;
    String comments_url;
    String issue_comment_url;
    String contents_url;
    String compare_url;
    String merges_url;
    String archive_url;
    String downloads_url;
    String issues_url;
    String pulls_url;
    String milestones_url;
    String notifications_url;
    String labels_url;
    String releases_url;
    String deployments_url;
    //TODO: Switch to use date class rather than strings
    String created_at;
    String updated_at;
    String pushed_at;
    String git_url;
    String ssh_url;
    String clone_url;
    String svn_url;
    String homepage;
    int size;
    int stargazers_count;
    int watchers_count;
    String language;
    boolean has_issues;
    boolean has_projects;
    boolean has_downloads;
    boolean has_wiki;
    boolean has_pages;
    int forks_count;
    String mirror_url;
    boolean archived;
    int open_issues_count;
    License license;
    int forks;
    int open_issues;
    int watchers;
    String default_branch;
    double score;

    public String getFull_name() {

        return full_name;

    }

    public void setFullName(String fullName) {

        this.full_name = fullName;

    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Repository) {

            Repository object = (Repository) obj;
            return this.full_name.equals(object.getFull_name());

        }
        return false;

    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(this.id);
        dest.writeString(this.node_id);
        dest.writeString(this.name);
        dest.writeString(this.full_name);
        dest.writeParcelable(this.owner, flags);
        dest.writeByte(this.isPrivate ? (byte) 1 : (byte) 0);
        dest.writeString(this.html_url);
        dest.writeString(this.description);
        dest.writeByte(this.fork ? (byte) 1 : (byte) 0);
        dest.writeString(this.url);
        dest.writeString(this.forks_url);
        dest.writeString(this.keys_url);
        dest.writeString(this.collaborators_url);
        dest.writeString(this.teams_url);
        dest.writeString(this.hooks_url);
        dest.writeString(this.issue_events_url);
        dest.writeString(this.events_url);
        dest.writeString(this.assignees_url);
        dest.writeString(this.branches_url);
        dest.writeString(this.tags_url);
        dest.writeString(this.blobs_url);
        dest.writeString(this.git_tags_url);
        dest.writeString(this.git_refs_url);
        dest.writeString(this.trees_url);
        dest.writeString(this.statuses_url);
        dest.writeString(this.languages_url);
        dest.writeString(this.stargazers_url);
        dest.writeString(this.contributors_url);
        dest.writeString(this.subscribers_url);
        dest.writeString(this.subscription_url);
        dest.writeString(this.commits_url);
        dest.writeString(this.git_commits_url);
        dest.writeString(this.comments_url);
        dest.writeString(this.issue_comment_url);
        dest.writeString(this.contents_url);
        dest.writeString(this.compare_url);
        dest.writeString(this.merges_url);
        dest.writeString(this.archive_url);
        dest.writeString(this.downloads_url);
        dest.writeString(this.issues_url);
        dest.writeString(this.pulls_url);
        dest.writeString(this.milestones_url);
        dest.writeString(this.notifications_url);
        dest.writeString(this.labels_url);
        dest.writeString(this.releases_url);
        dest.writeString(this.deployments_url);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.pushed_at);
        dest.writeString(this.git_url);
        dest.writeString(this.ssh_url);
        dest.writeString(this.clone_url);
        dest.writeString(this.svn_url);
        dest.writeString(this.homepage);
        dest.writeInt(this.size);
        dest.writeInt(this.stargazers_count);
        dest.writeInt(this.watchers_count);
        dest.writeString(this.language);
        dest.writeByte(this.has_issues ? (byte) 1 : (byte) 0);
        dest.writeByte(this.has_projects ? (byte) 1 : (byte) 0);
        dest.writeByte(this.has_downloads ? (byte) 1 : (byte) 0);
        dest.writeByte(this.has_wiki ? (byte) 1 : (byte) 0);
        dest.writeByte(this.has_pages ? (byte) 1 : (byte) 0);
        dest.writeInt(this.forks_count);
        dest.writeString(this.mirror_url);
        dest.writeByte(this.archived ? (byte) 1 : (byte) 0);
        dest.writeInt(this.open_issues_count);
        dest.writeParcelable(this.license, flags);
        dest.writeInt(this.forks);
        dest.writeInt(this.open_issues);
        dest.writeInt(this.watchers);
        dest.writeString(this.default_branch);
        dest.writeDouble(this.score);
    }

    public Repository() {

    }

    protected Repository(Parcel in) {

        this.id = in.readInt();
        this.node_id = in.readString();
        this.name = in.readString();
        this.full_name = in.readString();
        this.owner = in.readParcelable(GithubUser.class.getClassLoader());
        this.isPrivate = in.readByte() != 0;
        this.html_url = in.readString();
        this.description = in.readString();
        this.fork = in.readByte() != 0;
        this.url = in.readString();
        this.forks_url = in.readString();
        this.keys_url = in.readString();
        this.collaborators_url = in.readString();
        this.teams_url = in.readString();
        this.hooks_url = in.readString();
        this.issue_events_url = in.readString();
        this.events_url = in.readString();
        this.assignees_url = in.readString();
        this.branches_url = in.readString();
        this.tags_url = in.readString();
        this.blobs_url = in.readString();
        this.git_tags_url = in.readString();
        this.git_refs_url = in.readString();
        this.trees_url = in.readString();
        this.statuses_url = in.readString();
        this.languages_url = in.readString();
        this.stargazers_url = in.readString();
        this.contributors_url = in.readString();
        this.subscribers_url = in.readString();
        this.subscription_url = in.readString();
        this.commits_url = in.readString();
        this.git_commits_url = in.readString();
        this.comments_url = in.readString();
        this.issue_comment_url = in.readString();
        this.contents_url = in.readString();
        this.compare_url = in.readString();
        this.merges_url = in.readString();
        this.archive_url = in.readString();
        this.downloads_url = in.readString();
        this.issues_url = in.readString();
        this.pulls_url = in.readString();
        this.milestones_url = in.readString();
        this.notifications_url = in.readString();
        this.labels_url = in.readString();
        this.releases_url = in.readString();
        this.deployments_url = in.readString();
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.pushed_at = in.readString();
        this.git_url = in.readString();
        this.ssh_url = in.readString();
        this.clone_url = in.readString();
        this.svn_url = in.readString();
        this.homepage = in.readString();
        this.size = in.readInt();
        this.stargazers_count = in.readInt();
        this.watchers_count = in.readInt();
        this.language = in.readString();
        this.has_issues = in.readByte() != 0;
        this.has_projects = in.readByte() != 0;
        this.has_downloads = in.readByte() != 0;
        this.has_wiki = in.readByte() != 0;
        this.has_pages = in.readByte() != 0;
        this.forks_count = in.readInt();
        this.mirror_url = in.readString();
        this.archived = in.readByte() != 0;
        this.open_issues_count = in.readInt();
        this.license = in.readParcelable(License.class.getClassLoader());
        this.forks = in.readInt();
        this.open_issues = in.readInt();
        this.watchers = in.readInt();
        this.default_branch = in.readString();
        this.score = in.readDouble();
    }

    public static final Creator<Repository> CREATOR = new Creator<Repository>() {

        @Override
        public Repository createFromParcel(Parcel source) {

            return new Repository(source);
        }

        @Override
        public Repository[] newArray(int size) {

            return new Repository[size];
        }
    };
}
