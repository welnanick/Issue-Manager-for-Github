package com.nickwelna.issuemanagerforgithub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.Repository;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    String repositoryName;
    GitHubService service;
    SharedPreferences preferences;
    GithubUser user;
    ArrayList<Repository> pinnedRepositories;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference userDataReference =
            database.getReference("users").child(auth.getCurrentUser().getUid());
    ArrayList<PinnedIssueMenuItem> pinnedIssueMenuItems;
    ArrayList<Issue> issues;
    boolean firstRun;
    boolean rotated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_list);
        Bundle extras = getIntent().getExtras();
        firstRun = true;
        repositoryName = extras.getString("repository");

        if (savedInstanceState != null) {

            user = savedInstanceState.getParcelable("user");
            pinnedIssueMenuItems = savedInstanceState.getParcelableArrayList("pinned_issues");
            issues = savedInstanceState.getParcelableArrayList("issues");
            pinnedRepositories = savedInstanceState.getParcelableArrayList("pinned_repositories");
            rotated = true;

        }
        else {

            user = extras.getParcelable("user");

        }
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(repositoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                issues = null;
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
        if (user == null) {
            service.getAuthorizedUser().enqueue(new Callback<GithubUser>() {

                @Override
                public void onResponse(Call<GithubUser> call, Response<GithubUser> response) {

                    if (response.code() == 401) {

                        Gson gson = new Gson();
                        APIRequestError error = null;
                        try {
                            error = gson.fromJson(response.errorBody().string(),
                                    APIRequestError.class);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (error.getMessage().equals("Bad credentials")) {

                            new AlertDialog.Builder(IssueListActivity.this)
                                    .setTitle("Login Credentials Expired").setMessage(
                                    "Your login credentials have expired, please log in again")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            SharedPreferences preferences = PreferenceManager
                                                    .getDefaultSharedPreferences(
                                                            IssueListActivity.this);
                                            Editor editor = preferences.edit();
                                            editor.putString("OAuth_token", null);
                                            editor.apply();
                                            FirebaseAuth.getInstance().signOut();

                                            Intent logoutIntent = new Intent(IssueListActivity.this,
                                                    LoginActivity.class);
                                            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            dialog.dismiss();
                                            IssueListActivity.this.startActivity(logoutIntent);

                                        }

                                    }).create().show();

                        }

                    }
                    else {

                        user = response.body();
                        loadPinnedIssues(user);

                    }

                }

                @Override
                public void onFailure(Call<GithubUser> call, Throwable t) {

                }
            });
        }
        else {
            loadPinnedIssues(user);
        }

        refreshPinnedRepositories();

    }

    private void refreshPinnedRepositories() {

        if (pinnedRepositories == null || !rotated) {

            DatabaseReference pinnedRepos = userDataReference.child("pinned_repos");
            ValueEventListener pinnedRepositoryListener = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    pinnedRepositories = new ArrayList<>();
                    for (DataSnapshot pinnedRepoSnapshot : dataSnapshot.getChildren()) {

                        Repository temp = new Repository();
                        temp.setFullName(pinnedRepoSnapshot.getValue(String.class));
                        pinnedRepositories.add(temp);

                    }
                    invalidateOptionsMenu();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            pinnedRepos.addListenerForSingleValueEvent(pinnedRepositoryListener);

        }

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

        if (pinnedIssueMenuItems == null || !rotated) {

            pinnedIssueMenuItems = new ArrayList<>();
            pinnedIssueMenuItems.add(new PinnedIssueMenuItem(0));

            DatabaseReference pinnedIssuesRef = userDataReference.child("pinned_issues");
            ValueEventListener pinnedIssueListener = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //owners of all repos with pinned issues
                    for (DataSnapshot pinnedIssueSnapshot : dataSnapshot.getChildren()) {
                        String owner = pinnedIssueSnapshot.getKey();

                        //repositories that have pinned issues
                        for (DataSnapshot ownerRepositories : pinnedIssueSnapshot.getChildren()) {
                            String repositoryName = ownerRepositories.getKey();
                            String fullName = owner + "/" + repositoryName;
                            pinnedIssueMenuItems.add(new PinnedIssueMenuItem(fullName, 1));

                            //issues pinned
                            for (DataSnapshot issue : ownerRepositories.getChildren()) {

                                pinnedIssueMenuItems.add(new PinnedIssueMenuItem(fullName,
                                        issue.getValue(Integer.class), 2));

                            }

                        }

                    }
                    pinnedIssueAdapter.updatePinnedRepositories(pinnedIssueMenuItems);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            pinnedIssuesRef.addListenerForSingleValueEvent(pinnedIssueListener);

        }
        else {

            pinnedIssueAdapter.updatePinnedRepositories(pinnedIssueMenuItems);

        }

    }

    private void loadIssues() {

        if (issues == null || !rotated) {
            String[] repoNameSplit = repositoryName.split("/");

            service.getIssues(repoNameSplit[0], repoNameSplit[1], "all")
                    .enqueue(new Callback<ArrayList<Issue>>() {

                        @Override
                        public void onResponse(Call<ArrayList<Issue>> call,
                                               Response<ArrayList<Issue>> response) {

                            if (response.code() == 401) {

                                Gson gson = new Gson();
                                APIRequestError error = null;
                                try {
                                    error = gson.fromJson(response.errorBody().string(),
                                            APIRequestError.class);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (error.getMessage().equals("Bad credentials")) {

                                    new AlertDialog.Builder(IssueListActivity.this)
                                            .setTitle("Login Credentials Expired").setMessage(
                                            "Your login credentials have expired, please log in " +
                                                    "again").setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {

                                                    SharedPreferences preferences =
                                                            PreferenceManager
                                                                    .getDefaultSharedPreferences(
                                                                            IssueListActivity.this);
                                                    Editor editor = preferences.edit();
                                                    editor.putString("OAuth_token", null);
                                                    editor.apply();
                                                    FirebaseAuth.getInstance().signOut();

                                                    Intent logoutIntent =
                                                            new Intent(IssueListActivity.this,
                                                                    LoginActivity.class);
                                                    logoutIntent.addFlags(
                                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    dialog.dismiss();
                                                    IssueListActivity.this
                                                            .startActivity(logoutIntent);

                                                }

                                            }).create().show();

                                }

                            }
                            else {

                                issues = response.body();
                                issueAdapter.updateIssues(issues);
                                addIssue.show();
                                swipeRefresh.setRefreshing(false);

                            }

                        }

                        @Override
                        public void onFailure(Call<ArrayList<Issue>> call, Throwable t) {

                        }
                    });
        }
        else {

            issueAdapter.updateIssues(issues);
            addIssue.show();
            swipeRefresh.setRefreshing(false);

        }

    }

    @Override
    protected void onResume() {

        super.onResume();
        swipeRefresh.setRefreshing(true);
        loadIssues();
        if (user != null && !firstRun) {

            loadPinnedIssues(user);

        }
        if (firstRun) {

            firstRun = false;

        }
        rotated = false;

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
            pinUnpin.setIcon(R.drawable.ic_thumbtack_off_white_24dp);

        }

        return super.onCreateOptionsMenu(menu);

    }

    private boolean isPinned() {

        Repository repository = new Repository();
        repository.setFullName(repositoryName);

        return pinnedRepositories != null && pinnedRepositories.contains(repository);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_pin_unpin:
                if (isPinned()) {

                    for (int i = 0; i < pinnedRepositories.size(); i++) {

                        if (pinnedRepositories.get(i).getFull_name().equals(repositoryName)) {

                            pinnedRepositories.remove(i);

                        }

                    }
                    userDataReference.child("pinned_repos")
                            .setValue(convertToString(pinnedRepositories));
                    invalidateOptionsMenu();
                    Toast.makeText(this, "Repository Unpinned", Toast.LENGTH_LONG).show();

                }
                else {

                    Repository repository = new Repository();
                    repository.setFullName(repositoryName);
                    pinnedRepositories.add(repository);
                    userDataReference.child("pinned_repos")
                            .setValue(convertToString(pinnedRepositories));
                    invalidateOptionsMenu();
                    Toast.makeText(this, "Repository pinned", Toast.LENGTH_LONG).show();

                }
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    private List<String> convertToString(List<Repository> pinnedRepositories) {

        List<String> repoStrings = new ArrayList<>();

        for (Repository repo : pinnedRepositories) {

            repoStrings.add(repo.getFull_name());

        }
        return repoStrings;
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putParcelable("user", user);
        outState.putParcelableArrayList("pinned_issues", pinnedIssueMenuItems);
        outState.putParcelableArrayList("issues", issues);
        outState.putParcelableArrayList("pinned_repositories", pinnedRepositories);

    }

}
