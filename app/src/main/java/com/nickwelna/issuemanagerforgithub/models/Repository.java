package com.nickwelna.issuemanagerforgithub.models;

import com.squareup.moshi.Json;

public final class Repository {
    int id;
    @Json(name = "node_id")
    String nodeId;
    String name;
    GithubUser owner;
    @Json(name = "private")
    boolean isPrivate;
    @Json(name = "html_url")
    String htmlUrl;
    String description;
    boolean fork;
    String url;
    @Json(name = "forks_url")
    String forksUrl;
    @Json(name = "keys_url")
    String keysUrl;
    @Json(name = "collaborators_url")
    String collaboratorsUrl;
    @Json(name = "teams_url")
    String teamsUrl;
    @Json(name = "hooks_url")
    String hooksUrl;
    @Json(name = "issue_events_url")
    String issueEventsUrl;
    @Json(name = "events_url")
    String eventsUrl;
    @Json(name = "assignees_url")
    String assigneesUrl;
    @Json(name = "branches_url")
    String branchesUrl;
    @Json(name = "tags_url")
    String tagsUrl;
    @Json(name = "blobs_url")
    String blobsUrl;
    @Json(name = "git_tags_url")
    String gitTagsUrl;
    @Json(name = "git_refs_url")
    String gitRefsUrl;
    @Json(name = "trees_url")
    String treesUrl;
    @Json(name = "statuses_url")
    String statusesUrl;
    @Json(name = "languages_url")
    String languagesUrl;
    @Json(name = "stargazers_url")
    String stargazersUrl;
    @Json(name = "contributors_url")
    String contributorsUrl;
    @Json(name = "subscribers_url")
    String subscribersUrl;
    @Json(name = "subscription_url")
    String subscriptionUrl;
    @Json(name = "commits_url")
    String commitsUrl;
    @Json(name = "git_commits_url")
    String gitCommitsUrl;
    @Json(name = "comments_url")
    String commentsUrl;
    @Json(name = "issue_comment_url")
    String issueCommentUrl;
    @Json(name = "contents_url")
    String contentsUrl;
    @Json(name = "compare_url")
    String compareUrl;
    @Json(name = "merges_url")
    String mergesUrl;
    @Json(name = "archive_url")
    String archiveUrl;
    @Json(name = "downloads_url")
    String downloadsUrl;
    @Json(name = "issues_url")
    String issuesUrl;
    @Json(name = "pulls_url")
    String pullsUrl;
    @Json(name = "milestones_url")
    String milestonesUrl;
    @Json(name = "notifications_url")
    String notificationsUrl;
    @Json(name = "labels_url")
    String labelsUrl;
    @Json(name = "releases_url")
    String releasesUrl;
    @Json(name = "deployments_url")
    String deploymentsUrl;
    @Json(name = "created_at")
    String createdAt;
    @Json(name = "updated_at")
    String updatedAt;
    @Json(name = "pushed_at")
    String pushedAt;
    @Json(name = "git_url")
    String gitUrl;
    @Json(name = "ssh_url")
    String sshUrl;
    @Json(name = "clone_url")
    String cloneUrl;
    @Json(name = "svn_url")
    String svnUrl;
    String homepage;
    int size;
    @Json(name = "stargazers_count")
    int stargazersCount;
    @Json(name = "watchers_count")
    int watchersCount;
    String language;
    @Json(name = "has_issues")
    boolean hasIssues;
    @Json(name = "has_projects")
    boolean hasProjects;
    @Json(name = "has_downloads")
    boolean hasDownloads;
    @Json(name = "has_wiki")
    boolean hasWiki;
    @Json(name = "has_pages")
    boolean hasPages;
    @Json(name = "forks_count")
    int forksCount;
    @Json(name = "mirror_url")
    String mirrorUrl;
    boolean archived;
    @Json(name = "open_issues_count")
    int openIssuesCount;
    License license;
    int forks;
    @Json(name = "open_issues")
    int openIssues;
    int watchers;
    @Json(name = "default_branch")
    String defaultBranch;
    double score;
    @Json(name = "full_name")
    private String fullName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
