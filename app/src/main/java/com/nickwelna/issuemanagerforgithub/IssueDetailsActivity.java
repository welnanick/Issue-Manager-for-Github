package com.nickwelna.issuemanagerforgithub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.nickwelna.issuemanagerforgithub.models.Comment;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    CommentAdapter commentAdapter;
    String repositoryName;
    Issue issue;
    boolean fromPinned;
    boolean firstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_details);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();

        issue = extras.getParcelable("Issue");
        repositoryName = extras.getString("repo-name");
        fromPinned = extras.getBoolean("from-pinned");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(issue.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                loadComments();

            }

        });
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setRefreshing(true);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter();
        commentRecyclerView.setAdapter(commentAdapter);
        loadComments();

        addComment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent addCommentIntent = new Intent(IssueDetailsActivity.this, EditComment.class);
                Bundle extras = new Bundle();
                extras.putString("action", "add");
                extras.putString("type", "comment");
                addCommentIntent.putExtras(extras);
                startActivity(addCommentIntent);

            }

        });

        loadPinnedIssues();

    }

    /**
     * Thi method fetches the pinned issues from firebase, and adds them to the navigation drawer
     */
    private void loadPinnedIssues() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        menuRecyclerView.setLayoutManager(linearLayoutManager);
        final PinnedIssueAdapter pinnedIssueAdapter = new PinnedIssueAdapter();
        menuRecyclerView.setAdapter(pinnedIssueAdapter);
        PinnedIssueMenuItem[] pinnedIssueMenuItems = new PinnedIssueMenuItem[5];
        pinnedIssueMenuItems[0] = new PinnedIssueMenuItem("welnanick", 0);
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

        Comment[] comments = getComments(issue);

        final IssueCommentCommon[] allComments = new IssueCommentCommon[comments.length + 1];
        allComments[0] = issue;
        System.arraycopy(comments, 0, allComments, 1, comments.length);

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {

            public void run() {

                commentAdapter.updateComments(allComments);
                addComment.show();
                firstRun = false;
                swipeRefresh.setRefreshing(false);

            }

        };
        handler.postDelayed(r, 5000);

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
        parentIntent.putExtras(extras);

        return parentIntent;
    }

    public Comment[] getComments(Issue issue) {

        Gson gson = new Gson();
        return gson.fromJson("[{\"url\":\"https://api.github" +
                ".com/repos/JakeWharton/butterknife/issues/comments/386957944\"," +
                "\"html_url\":\"https://github" +
                ".com/JakeWharton/butterknife/issues/1269#issuecomment-386957944\"," +
                "\"issue_url\":\"https://api.github" +
                ".com/repos/JakeWharton/butterknife/issues/1269\",\"id\":386957944," +
                "\"node_id\":\"MDEyOklzc3VlQ29tbWVudDM4Njk1Nzk0NA==\"," +
                "\"user\":{\"login\":\"JakeWharton\",\"id\":66577," +
                "\"node_id\":\"MDQ6VXNlcjY2NTc3\"," +
                "\"avatar_url\":\"https://avatars0.githubusercontent.com/u/66577?v=4\"," +
                "\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/JakeWharton\"," +
                "\"html_url\":\"https://github.com/JakeWharton\",\"followers_url\":\"https://api" +
                ".github.com/users/JakeWharton/followers\",\"following_url\":\"https://api.github" +
                ".com/users/JakeWharton/following{/other_user}\",\"gists_url\":\"https://api" +
                ".github.com/users/JakeWharton/gists{/gist_id}\",\"starred_url\":\"https://api" +
                ".github.com/users/JakeWharton/starred{/owner}{/repo}\"," +
                "\"subscriptions_url\":\"https://api.github" +
                ".com/users/JakeWharton/subscriptions\",\"organizations_url\":\"https://api" +
                ".github.com/users/JakeWharton/orgs\",\"repos_url\":\"https://api.github" +
                ".com/users/JakeWharton/repos\",\"events_url\":\"https://api.github" +
                ".com/users/JakeWharton/events{/privacy}\",\"received_events_url\":\"https://api" +
                ".github.com/users/JakeWharton/received_events\",\"type\":\"User\"," +
                "\"site_admin\":false},\"created_at\":\"2018-05-07T05:03:10Z\"," +
                "\"updated_at\":\"2018-05-07T05:03:10Z\",\"author_association\":\"OWNER\"," +
                "\"body\":\"The warning shouldn't occur because a class is generated which " +
                "accesses those fields. I'll take a look at some point...\"}," +
                "{\"url\":\"https://api.github" +
                ".com/repos/JakeWharton/butterknife/issues/comments/387126325\"," +
                "\"html_url\":\"https://github" +
                ".com/JakeWharton/butterknife/issues/1269#issuecomment-387126325\"," +
                "\"issue_url\":\"https://api.github" +
                ".com/repos/JakeWharton/butterknife/issues/1269\",\"id\":387126325," +
                "\"node_id\":\"MDEyOklzc3VlQ29tbWVudDM4NzEyNjMyNQ==\"," +
                "\"user\":{\"login\":\"Madonahs\",\"id\":11560987," +
                "\"node_id\":\"MDQ6VXNlcjExNTYwOTg3\"," +
                "\"avatar_url\":\"https://avatars3.githubusercontent.com/u/11560987?v=4\"," +
                "\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/Madonahs\"," +
                "\"html_url\":\"https://github.com/Madonahs\",\"followers_url\":\"https://api" +
                ".github.com/users/Madonahs/followers\",\"following_url\":\"https://api.github" +
                ".com/users/Madonahs/following{/other_user}\",\"gists_url\":\"https://api.github" +
                ".com/users/Madonahs/gists{/gist_id}\",\"starred_url\":\"https://api.github" +
                ".com/users/Madonahs/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api" +
                ".github.com/users/Madonahs/subscriptions\",\"organizations_url\":\"https://api" +
                ".github.com/users/Madonahs/orgs\",\"repos_url\":\"https://api.github" +
                ".com/users/Madonahs/repos\",\"events_url\":\"https://api.github" +
                ".com/users/Madonahs/events{/privacy}\",\"received_events_url\":\"https://api" +
                ".github.com/users/Madonahs/received_events\",\"type\":\"User\"," +
                "\"site_admin\":false},\"created_at\":\"2018-05-07T16:42:53Z\"," +
                "\"updated_at\":\"2018-05-07T16:42:53Z\",\"author_association\":\"NONE\"," +
                "\"body\":\"Thank you, will appreciate. \"}]", Comment[].class);

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

        switch (item.getItemId()) {

            case R.id.action_close_open:
                if (issue.getState().equals("closed")) {

                    Toast.makeText(this, "Issue Opened", Toast.LENGTH_LONG).show();

                }
                else {

                    Toast.makeText(this, "Issue Closed", Toast.LENGTH_LONG).show();

                }
                return true;

            case R.id.action_lock_unlock:
                if (issue.isLocked()) {

                    Toast.makeText(this, "Issue Unlocked", Toast.LENGTH_LONG).show();

                }
                else {

                    Toast.makeText(this, "Issue locked", Toast.LENGTH_LONG).show();

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

    }

    @Override
    protected void onPause() {

        super.onPause();
        addComment.hide();

    }

}
