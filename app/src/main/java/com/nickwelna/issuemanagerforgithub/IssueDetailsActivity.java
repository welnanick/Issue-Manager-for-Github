package com.nickwelna.issuemanagerforgithub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.Comment;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueCloseOpenRequest;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssueDetailsActivity extends AppCompatActivity {

    @BindView(R.id.comment_recycler_view)
    RecyclerView commentRecyclerView;
    @BindView(R.id.add_comment)
    FloatingActionButton addComment;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.menu_recycler_view)
    RecyclerView menuRecyclerView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    CommentAdapter commentAdapter;
    String repositoryName;
    Issue issue;
    boolean fromPinned;
    boolean firstRun = true;
    GitHubService service;
    SharedPreferences preferences;
    GithubUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_details);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();

        issue = extras.getParcelable("Issue");
        repositoryName = extras.getString("repo-name");
        fromPinned = extras.getBoolean("from-pinned");
        user = extras.getParcelable("user");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(issue.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                refreshIssue();

            }

        });
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setRefreshing(true);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(repositoryName, user);
        commentRecyclerView.setAdapter(commentAdapter);

        addComment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent addCommentIntent =
                        new Intent(IssueDetailsActivity.this, EditCommentActivity.class);
                Bundle extras = new Bundle();
                extras.putString("action", "add");
                extras.putString("type", "comment");
                extras.putInt("issue_number", issue.getNumber());
                extras.putString("repo_name", repositoryName);
                addCommentIntent.putExtras(extras);
                startActivity(addCommentIntent);

            }

        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String token = preferences.getString("OAuth_token", null);

        service = ServiceGenerator.createService(token);
        loadPinnedIssues(user);
        loadComments();

    }

    public void refreshIssue() {

        String[] repoNameSplit = repositoryName.split("/");
        service.getIssue(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                .enqueue(new Callback<Issue>() {

                    @Override
                    public void onResponse(Call<Issue> call, Response<Issue> response) {

                        issue = response.body();
                        invalidateOptionsMenu();
                        loadComments();

                    }

                    @Override
                    public void onFailure(Call<Issue> call, Throwable t) {

                    }
                });

    }

    /**
     * Thi method fetches the pinned issues from firebase, and adds them to the navigation drawer
     *
     * @param user
     */
    private void loadPinnedIssues(GithubUser user) {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        menuRecyclerView.setLayoutManager(linearLayoutManager);
        final PinnedIssueAdapter pinnedIssueAdapter = new PinnedIssueAdapter(user);
        menuRecyclerView.setAdapter(pinnedIssueAdapter);
        PinnedIssueMenuItem[] pinnedIssueMenuItems = new PinnedIssueMenuItem[5];
        pinnedIssueMenuItems[0] = new PinnedIssueMenuItem(0);
        pinnedIssueMenuItems[1] = new PinnedIssueMenuItem("JakeWharton/butterknife", 1);
        pinnedIssueMenuItems[2] = new PinnedIssueMenuItem(
                "Use butterknife with Android Gradle Plugin version 3.1.+ cannot compile ~",
                "#1293", 2);
        pinnedIssueMenuItems[3] =
                new PinnedIssueMenuItem("Multi module project with multiple R2 files", "#1292", 2);
        pinnedIssueMenuItems[4] = new PinnedIssueMenuItem(
                "Android Gradle plugin 3.1.0 must not be applied to project " +
                        "'/Users/android_package/butterKnife/app' since version 3.1.0 was already" +
                        " applied to this project", "#1290", 2);
        pinnedIssueAdapter.updatePinnedRepositories(pinnedIssueMenuItems);

    }

    private void loadComments() {

        String[] repoNameSplit = repositoryName.split("/");

        service.getComments(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                .enqueue(new Callback<Comment[]>() {

                    @Override
                    public void onResponse(Call<Comment[]> call, Response<Comment[]> response) {

                        Comment[] comments = response.body();

                        final IssueCommentCommon[] allComments =
                                new IssueCommentCommon[comments.length + 1];
                        allComments[0] = issue;
                        System.arraycopy(comments, 0, allComments, 1, comments.length);
                        commentAdapter.updateComments(allComments);
                        addComment.show();
                        firstRun = false;
                        swipeRefresh.setRefreshing(false);

                    }

                    @Override
                    public void onFailure(Call<Comment[]> call, Throwable t) {

                    }
                });

    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {

        return fromPinned || super.supportShouldUpRecreateTask(targetIntent);

    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {

        Intent parentIntent = super.getSupportParentActivityIntent();

        Bundle extras = new Bundle();
        extras.putString("repository", repositoryName);
        extras.putParcelable("user", user);
        parentIntent.putExtras(extras);

        return parentIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.issue_menu, menu);

        MenuItem closeOpen = menu.findItem(R.id.action_close_open);
        MenuItem lockUnlock = menu.findItem(R.id.action_lock_unlock);
        MenuItem pinUnpin = menu.findItem(R.id.action_pin_unpin);
        if (issue.getState().equals("closed")) {

            closeOpen.setTitle("Open Issue");

        }
        if (issue.isLocked()) {

            lockUnlock.setTitle("Unlock Issue");

        }
        if (isPinned()) {

            pinUnpin.setTitle("Unpin issue");
            pinUnpin.setIcon(R.drawable.ic_thumbtack_white_24dp);

        }

        return super.onCreateOptionsMenu(menu);

    }

    private boolean isPinned() {

        return issue.getNumber() % 2 == 0;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String[] repoNameSplit = repositoryName.split("/");
        switch (item.getItemId()) {

            case R.id.action_close_open:
                swipeRefresh.setRefreshing(true);
                IssueCloseOpenRequest closeOpenRequest = new IssueCloseOpenRequest();
                if (issue.getState().equals("closed")) {

                    closeOpenRequest.setState("open");
                    service.openCloseIssue(repoNameSplit[0], repoNameSplit[1], issue.getNumber(),
                            closeOpenRequest).enqueue(

                            new Callback<Issue>() {

                                @Override
                                public void onResponse(Call<Issue> call, Response<Issue> response) {

                                    if (response.code() == 200) {

                                        issue = response.body();
                                        invalidateOptionsMenu();
                                        loadComments();
                                        Toast.makeText(IssueDetailsActivity.this, "Issue Opened",
                                                Toast.LENGTH_LONG).show();

                                    }
                                    else {

                                        swipeRefresh.setRefreshing(false);
                                        Gson gson = new Gson();
                                        APIRequestError error = null;
                                        try {
                                            error = gson.fromJson(response.errorBody().string(),
                                                    APIRequestError.class);
                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(IssueDetailsActivity.this,
                                                error.getMessage(), Toast.LENGTH_LONG).show();

                                    }

                                }

                                @Override
                                public void onFailure(Call<Issue> call, Throwable t) {

                                }
                            });

                }
                else {

                    closeOpenRequest.setState("closed");
                    service.openCloseIssue(repoNameSplit[0], repoNameSplit[1], issue.getNumber(),
                            closeOpenRequest).enqueue(

                            new Callback<Issue>() {

                                @Override
                                public void onResponse(Call<Issue> call, Response<Issue> response) {

                                    if (response.code() == 200) {

                                        issue = response.body();
                                        invalidateOptionsMenu();
                                        loadComments();
                                        Toast.makeText(IssueDetailsActivity.this, "Issue Closed",
                                                Toast.LENGTH_LONG).show();

                                    }
                                    else {

                                        swipeRefresh.setRefreshing(false);
                                        Gson gson = new Gson();
                                        APIRequestError error = null;
                                        try {
                                            error = gson.fromJson(response.errorBody().string(),
                                                    APIRequestError.class);
                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(IssueDetailsActivity.this,
                                                error.getMessage(), Toast.LENGTH_LONG).show();

                                    }

                                }

                                @Override
                                public void onFailure(Call<Issue> call, Throwable t) {

                                }
                            });

                }
                return true;

            case R.id.action_lock_unlock:
                swipeRefresh.setRefreshing(true);
                if (issue.isLocked()) {

                    service.unlockIssue(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                            .enqueue(

                                    new Callback<Issue>() {

                                        @Override
                                        public void onResponse(Call<Issue> call,
                                                               Response<Issue> response) {

                                            if (response.code() == 204) {

                                                refreshIssue();

                                                Toast.makeText(IssueDetailsActivity.this,
                                                        "Issue Unlocked", Toast.LENGTH_LONG).show();

                                            }
                                            else {

                                                Gson gson = new Gson();
                                                APIRequestError error = null;
                                                try {
                                                    error = gson.fromJson(
                                                            response.errorBody().string(),
                                                            APIRequestError.class);
                                                }
                                                catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                Toast.makeText(IssueDetailsActivity.this,
                                                        error.getMessage(), Toast.LENGTH_LONG)
                                                        .show();

                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<Issue> call, Throwable t) {

                                        }
                                    });

                }
                else {

                    service.lockIssue(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                            .enqueue(

                                    new Callback<Issue>() {

                                        @Override
                                        public void onResponse(Call<Issue> call,
                                                               Response<Issue> response) {

                                            if (response.code() == 204) {

                                                refreshIssue();
                                                Toast.makeText(IssueDetailsActivity.this,
                                                        "Issue Locked", Toast.LENGTH_LONG).show();

                                            }
                                            else {

                                                Gson gson = new Gson();
                                                APIRequestError error = null;
                                                try {
                                                    error = gson.fromJson(
                                                            response.errorBody().string(),
                                                            APIRequestError.class);
                                                }
                                                catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                Toast.makeText(IssueDetailsActivity.this,
                                                        error.getMessage(), Toast.LENGTH_LONG)
                                                        .show();

                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<Issue> call, Throwable t) {

                                        }
                                    });

                }
                return true;
            case R.id.action_pin_unpin:
                if (isPinned()) {

                    Toast.makeText(this, "Issue Unpinned", Toast.LENGTH_LONG).show();

                }
                else {

                    Toast.makeText(this, "Issue pinned", Toast.LENGTH_LONG).show();

                }
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!addComment.isShown() && !firstRun) {

            addComment.show();

        }
        if (!firstRun) {

            swipeRefresh.setRefreshing(true);
            refreshIssue();

        }

    }

    @Override
    protected void onPause() {

        super.onPause();
        addComment.hide();

    }

    @Override
    public void onBackPressed() {

        // Back button press should dismiss navigation drawer. This is common behavior in android
        // apps. For example, the gmail, google+, google keep, twitter, and reddit apps all
        // follow this behavior
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

            drawerLayout.closeDrawer(GravityCompat.START);

        }
        else {

            super.onBackPressed();

        }

    }

}
