package com.nickwelna.issuemanagerforgithub.models;

import com.google.gson.annotations.SerializedName;

public class Repository {

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

}
