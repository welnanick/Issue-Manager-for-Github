package com.nickwelna.issuemanagerforgithub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.nickwelna.issuemanagerforgithub.models.Comment;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueCloseOpenRequest;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;
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
    boolean firstRun;
    GitHubService service;
    SharedPreferences preferences;
    GithubUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference userDataReference =
            database.getReference("users").child(auth.getCurrentUser().getUid());
    ArrayList<Integer> pinnedIssues;
    ArrayList<PinnedIssueMenuItem> pinnedIssueMenuItems;
    IssueCommentCommon[] allComments;
    boolean pinned;
    boolean rotated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_details);
        ButterKnife.bind(this);
        firstRun = true;

        Bundle extras = getIntent().getExtras();

        repositoryName = extras.getString("repo-name");
        fromPinned = extras.getBoolean("from-pinned");
        if (savedInstanceState != null) {

            user = savedInstanceState.getParcelable("user");
            pinnedIssueMenuItems = savedInstanceState.getParcelableArrayList("pinned_issues_menu");
            allComments =
                    (IssueCommentCommon[]) savedInstanceState.getParcelableArray("all_comments");
            issue = savedInstanceState.getParcelable("issue");
            pinnedIssues = savedInstanceState.getIntegerArrayList("pinned_issues");
            rotated = true;

        }
        else {

            user = extras.getParcelable("user");
            issue = extras.getParcelable("Issue");

        }
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

                            new AlertDialog.Builder(IssueDetailsActivity.this)
                                    .setTitle("Login Credentials Expired").setMessage(
                                    "Your login credentials have " + "expired, please log in " +
                                            "again")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            SharedPreferences preferences = PreferenceManager
                                                    .getDefaultSharedPreferences(
                                                            IssueDetailsActivity.this);
                                            Editor editor = preferences.edit();
                                            editor.putString("OAuth_token", null);
                                            editor.apply();
                                            FirebaseAuth.getInstance().signOut();

                                            Intent logoutIntent =
                                                    new Intent(IssueDetailsActivity.this,
                                                            LoginActivity.class);
                                            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            dialog.dismiss();
                                            IssueDetailsActivity.this.startActivity(logoutIntent);

                                        }

                                    }).create().show();

                        }

                    }
                    else {

                        user = response.body();
                        commentAdapter.user = user;
                        loadPinnedIssues(user);
                        loadComments();

                    }

                }

                @Override
                public void onFailure(Call<GithubUser> call, Throwable t) {

                }
            });
        }
        else {
            loadPinnedIssues(user);
            loadComments();
        }
        isPinned();

    }

    public void refreshIssue() {

        allComments = null;
        String[] repoNameSplit = repositoryName.split("/");
        service.getIssue(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                .enqueue(new Callback<Issue>() {

                    @Override
                    public void onResponse(Call<Issue> call, Response<Issue> response) {

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

                                new AlertDialog.Builder(IssueDetailsActivity.this)
                                        .setTitle("Login Credentials Expired").setMessage(
                                        "Your login credentials have " + "expired, please log in " +
                                                "again").setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                SharedPreferences preferences = PreferenceManager
                                                        .getDefaultSharedPreferences(
                                                                IssueDetailsActivity.this);
                                                Editor editor = preferences.edit();
                                                editor.putString("OAuth_token", null);
                                                editor.apply();
                                                FirebaseAuth.getInstance().signOut();

                                                Intent logoutIntent =
                                                        new Intent(IssueDetailsActivity.this,
                                                                LoginActivity.class);
                                                logoutIntent.addFlags(
                                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                dialog.dismiss();
                                                IssueDetailsActivity.this
                                                        .startActivity(logoutIntent);

                                            }

                                        }).create().show();

                            }

                        }
                        else {

                            issue = response.body();
                            invalidateOptionsMenu();
                            loadComments();

                        }

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

        if (pinnedIssueMenuItems == null || !rotated) {

            pinnedIssueMenuItems = new ArrayList<>();
            pinnedIssues = new ArrayList<>();
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
                                pinnedIssues.add(issue.getValue(Integer.class));

                            }

                        }

                    }
                    pinnedIssueAdapter.updatePinnedRepositories(pinnedIssueMenuItems);
                    isPinned();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            pinnedIssuesRef.addListenerForSingleValueEvent(pinnedIssueListener);

        }
        else {

            pinnedIssueAdapter.updatePinnedRepositories(pinnedIssueMenuItems);
            isPinned();

        }

    }

    private void loadComments() {

        if (allComments == null || !rotated) {
            String[] repoNameSplit = repositoryName.split("/");

            service.getComments(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                    .enqueue(new Callback<Comment[]>() {

                        @Override
                        public void onResponse(Call<Comment[]> call, Response<Comment[]> response) {

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

                                    new AlertDialog.Builder(IssueDetailsActivity.this)
                                            .setTitle("Login Credentials Expired").setMessage(
                                            "Your login credentials have " +
                                                    "expired, please log in " + "again")
                                            .setPositiveButton("Ok",
                                                    new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {

                                                            SharedPreferences preferences =
                                                                    PreferenceManager
                                                                            .getDefaultSharedPreferences(
                                                                                    IssueDetailsActivity.this);
                                                            Editor editor = preferences.edit();
                                                            editor.putString("OAuth_token", null);
                                                            editor.apply();
                                                            FirebaseAuth.getInstance().signOut();

                                                            Intent logoutIntent = new Intent(
                                                                    IssueDetailsActivity.this,
                                                                    LoginActivity.class);
                                                            logoutIntent.addFlags(
                                                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            dialog.dismiss();
                                                            IssueDetailsActivity.this
                                                                    .startActivity(logoutIntent);

                                                        }

                                                    }).create().show();

                                }

                            }
                            else {

                                Comment[] comments = response.body();

                                allComments = new IssueCommentCommon[comments.length + 1];
                                allComments[0] = issue;
                                System.arraycopy(comments, 0, allComments, 1, comments.length);
                                commentAdapter.updateComments(allComments);
                                addComment.show();
                                swipeRefresh.setRefreshing(false);

                            }

                        }

                        @Override
                        public void onFailure(Call<Comment[]> call, Throwable t) {

                        }
                    });
        }
        else {

            commentAdapter.updateComments(allComments);
            addComment.show();
            swipeRefresh.setRefreshing(false);

        }

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
        if (pinned) {

            pinUnpin.setTitle("Unpin issue");
            pinUnpin.setIcon(R.drawable.ic_thumbtack_off_white_24dp);

        }

        return super.onCreateOptionsMenu(menu);

    }

    private void isPinned() {

        pinned = pinnedIssues.contains(issue.getNumber());
        invalidateOptionsMenu();

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
                                    else if (response.code() == 401) {

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

                                            new AlertDialog.Builder(IssueDetailsActivity.this)
                                                    .setTitle("Login Credentials Expired")
                                                    .setMessage("Your login credentials have " +
                                                            "expired, please log in " + "again")
                                                    .setPositiveButton("Ok",
                                                            new DialogInterface.OnClickListener() {

                                                                @Override
                                                                public void onClick(
                                                                        DialogInterface dialog,
                                                                        int which) {

                                                                    SharedPreferences preferences =
                                                                            PreferenceManager
                                                                                    .getDefaultSharedPreferences(
                                                                                            IssueDetailsActivity.this);
                                                                    Editor editor =
                                                                            preferences.edit();
                                                                    editor.putString("OAuth_token",
                                                                            null);
                                                                    editor.apply();
                                                                    FirebaseAuth.getInstance()
                                                                            .signOut();

                                                                    Intent logoutIntent =
                                                                            new Intent(
                                                                                    IssueDetailsActivity.this,
                                                                                    LoginActivity
                                                                                            .class);
                                                                    logoutIntent.addFlags(
                                                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    dialog.dismiss();
                                                                    IssueDetailsActivity.this
                                                                            .startActivity(
                                                                                    logoutIntent);

                                                                }

                                                            }).create().show();

                                        }

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
                                    else if (response.code() == 401) {

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

                                            new AlertDialog.Builder(IssueDetailsActivity.this)
                                                    .setTitle("Login Credentials Expired")
                                                    .setMessage("Your login credentials have " +
                                                            "expired, please log in " + "again")
                                                    .setPositiveButton("Ok",
                                                            new DialogInterface.OnClickListener() {

                                                                @Override
                                                                public void onClick(
                                                                        DialogInterface dialog,
                                                                        int which) {

                                                                    SharedPreferences preferences =
                                                                            PreferenceManager
                                                                                    .getDefaultSharedPreferences(
                                                                                            IssueDetailsActivity.this);
                                                                    Editor editor =
                                                                            preferences.edit();
                                                                    editor.putString("OAuth_token",
                                                                            null);
                                                                    editor.apply();
                                                                    FirebaseAuth.getInstance()
                                                                            .signOut();

                                                                    Intent logoutIntent =
                                                                            new Intent(
                                                                                    IssueDetailsActivity.this,
                                                                                    LoginActivity
                                                                                            .class);
                                                                    logoutIntent.addFlags(
                                                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    dialog.dismiss();
                                                                    IssueDetailsActivity.this
                                                                            .startActivity(
                                                                                    logoutIntent);

                                                                }

                                                            }).create().show();

                                        }

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
                                            else if (response.code() == 401) {

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

                                                if (error.getMessage().equals("Bad credentials")) {

                                                    new AlertDialog.Builder(
                                                            IssueDetailsActivity.this)
                                                            .setTitle("Login Credentials Expired")
                                                            .setMessage(
                                                                    "Your login credentials have " +
                                                                            "expired, please log " +
                                                                            "in " + "again")
                                                            .setPositiveButton("Ok",
                                                                    new DialogInterface
                                                                            .OnClickListener() {

                                                                        @Override
                                                                        public void onClick(
                                                                                DialogInterface
                                                                                        dialog,
                                                                                int which) {

                                                                            SharedPreferences
                                                                                    preferences =
                                                                                    PreferenceManager
                                                                                            .getDefaultSharedPreferences(
                                                                                                    IssueDetailsActivity.this);
                                                                            Editor editor =
                                                                                    preferences
                                                                                            .edit();
                                                                            editor.putString(
                                                                                    "OAuth_token",
                                                                                    null);
                                                                            editor.apply();
                                                                            FirebaseAuth
                                                                                    .getInstance()
                                                                                    .signOut();

                                                                            Intent logoutIntent =
                                                                                    new Intent(
                                                                                            IssueDetailsActivity.this,
                                                                                            LoginActivity.class);
                                                                            logoutIntent.addFlags(
                                                                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            dialog.dismiss();
                                                                            IssueDetailsActivity
                                                                                    .this
                                                                                    .startActivity(
                                                                                            logoutIntent);

                                                                        }

                                                                    }).create().show();

                                                }

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
                                            else if (response.code() == 401) {

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

                                                if (error.getMessage().equals("Bad credentials")) {

                                                    new AlertDialog.Builder(
                                                            IssueDetailsActivity.this)
                                                            .setTitle("Login Credentials Expired")
                                                            .setMessage(
                                                                    "Your login credentials have " +
                                                                            "expired, please log " +
                                                                            "in " + "again")
                                                            .setPositiveButton("Ok",
                                                                    new DialogInterface
                                                                            .OnClickListener() {

                                                                        @Override
                                                                        public void onClick(
                                                                                DialogInterface
                                                                                        dialog,
                                                                                int which) {

                                                                            SharedPreferences
                                                                                    preferences =
                                                                                    PreferenceManager
                                                                                            .getDefaultSharedPreferences(
                                                                                                    IssueDetailsActivity.this);
                                                                            Editor editor =
                                                                                    preferences
                                                                                            .edit();
                                                                            editor.putString(
                                                                                    "OAuth_token",
                                                                                    null);
                                                                            editor.apply();
                                                                            FirebaseAuth
                                                                                    .getInstance()
                                                                                    .signOut();

                                                                            Intent logoutIntent =
                                                                                    new Intent(
                                                                                            IssueDetailsActivity.this,
                                                                                            LoginActivity.class);
                                                                            logoutIntent.addFlags(
                                                                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            dialog.dismiss();
                                                                            IssueDetailsActivity
                                                                                    .this
                                                                                    .startActivity(
                                                                                            logoutIntent);

                                                                        }

                                                                    }).create().show();

                                                }

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
                if (pinned) {

                    final DatabaseReference reference =
                            userDataReference.child("pinned_issues").child(repositoryName);
                    ValueEventListener issueCheckListener = new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<Integer> thisRepoPinnedIssues = new ArrayList<>();

                            for (DataSnapshot issues : dataSnapshot.getChildren()) {

                                thisRepoPinnedIssues.add(issues.getValue(Integer.class));

                            }
                            pinned = true;
                            thisRepoPinnedIssues.remove((Integer) issue.getNumber());
                            reference.setValue(thisRepoPinnedIssues);
                            invalidateOptionsMenu();
                            pinnedIssueMenuItems = null;
                            loadPinnedIssues(user);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    reference.addListenerForSingleValueEvent(issueCheckListener);
                    Toast.makeText(this, "Issue Unpinned", Toast.LENGTH_LONG).show();

                }
                else {

                    final DatabaseReference reference =
                            userDataReference.child("pinned_issues").child(repositoryName);

                    ValueEventListener issueCheckListener = new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<Integer> thisRepoPinnedIssues = new ArrayList<>();

                            for (DataSnapshot issues : dataSnapshot.getChildren()) {

                                thisRepoPinnedIssues.add(issues.getValue(Integer.class));

                            }
                            pinned = true;
                            thisRepoPinnedIssues.add(issue.getNumber());
                            reference.setValue(thisRepoPinnedIssues);
                            invalidateOptionsMenu();
                            pinnedIssueMenuItems = null;
                            loadPinnedIssues(user);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    reference.addListenerForSingleValueEvent(issueCheckListener);
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
        else {

            firstRun = false;

        }
        if (rotated) {

            rotated = false;

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putParcelable("issue", issue);
        outState.putParcelableArray("all_comments", allComments);
        outState.putParcelableArrayList("pinned_issues_menu", pinnedIssueMenuItems);
        outState.putIntegerArrayList("pinned_issues", pinnedIssues);
        outState.putParcelable("user", user);

    }
}
