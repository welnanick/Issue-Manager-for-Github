package com.nickwelna.issuemanagerforgithub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssueListActivity extends AppCompatActivity {

    @BindView(R.id.issue_recycler_view)
    RecyclerView issue_recycler_view;
    @BindView(R.id.add_issue)
    FloatingActionButton addIssue;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.menu_recycler_view)
    RecyclerView menuRecyclerView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    IssueAdapter issueAdapter;
    boolean firstRun = true;
    String repositoryName;
    GitHubService service;
    SharedPreferences preferences;
    GithubUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_list);
        Bundle extras = getIntent().getExtras();
        repositoryName = extras.getString("repository");
        user = extras.getParcelable("user");
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(repositoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                loadIssues();

            }

        });
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        issue_recycler_view.setLayoutManager(linearLayoutManager);
        issueAdapter = new IssueAdapter(repositoryName, user);
        issue_recycler_view.setAdapter(issueAdapter);

        addIssue.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent addCommentIntent =
                        new Intent(IssueListActivity.this, EditCommentActivity.class);
                Bundle extras = new Bundle();
                extras.putString("action", "add");
                extras.putString("type", "issue");
                extras.putString("repo_name", repositoryName);
                addCommentIntent.putExtras(extras);
                startActivity(addCommentIntent);

            }

        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String token = preferences.getString("OAuth_token", null);

        service = ServiceGenerator.createService(token);
        loadPinnedIssues(user);

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

    private void loadIssues() {

        String[] repoNameSplit = repositoryName.split("/");

        service.getIssues(repoNameSplit[0], repoNameSplit[1], "all")
                .enqueue(new Callback<Issue[]>() {

                    @Override
                    public void onResponse(Call<Issue[]> call, Response<Issue[]> response) {

                        Issue[] issues = response.body();
                        issueAdapter.updateIssues(issues);
                        addIssue.show();
                        firstRun = false;
                        swipeRefresh.setRefreshing(false);

                    }

                    @Override
                    public void onFailure(Call<Issue[]> call, Throwable t) {

                    }
                });

    }

    @Override
    protected void onResume() {

        super.onResume();
        swipeRefresh.setRefreshing(true);
        loadIssues();

    }

    @Override
    protected void onPause() {

        super.onPause();
        addIssue.hide();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.repository_menu, menu);

        MenuItem pinUnpin = menu.findItem(R.id.action_pin_unpin);

        if (isPinned()) {

            pinUnpin.setTitle("Unpin issue");
            pinUnpin.setIcon(R.drawable.ic_thumbtack_white_24dp);

        }

        return super.onCreateOptionsMenu(menu);

    }

    private boolean isPinned() {

        return repositoryName.length() % 2 == 0;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_pin_unpin:
                if (isPinned()) {

                    Toast.makeText(this, "Repository Unpinned", Toast.LENGTH_LONG).show();

                }
                else {

                    Toast.makeText(this, "Repository pinned", Toast.LENGTH_LONG).show();

                }
                return true;

        }

        return super.onOptionsItemSelected(item);

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
