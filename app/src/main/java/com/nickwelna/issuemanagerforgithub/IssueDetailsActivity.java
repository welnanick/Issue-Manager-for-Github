package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    boolean connected;
    public static final String REPO_NAME_KEY = "repo_name";
    public static final String FROM_PINNED_KEY = "from_pinned";
    public static final String USER_KEY = "user";
    public static final String PINNED_ISSUES_MENU_KEY = "pinned_issues_menu";
    public static final String ALL_COMMENTS_KEY = "all_comments";
    public static final String ISSUE_KEY = "issue";
    public static final String PINNED_ISSUES_KEY = "pinned_issues";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_details);
        ButterKnife.bind(this);

        DatabaseReference connectedRef =
                FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                connected = snapshot.getValue(Boolean.class);

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }

        });

        firstRun = true;

        Bundle extras = getIntent().getExtras();

        repositoryName = extras.getString(REPO_NAME_KEY);
        fromPinned = extras.getBoolean(FROM_PINNED_KEY);
        if (savedInstanceState != null) {

            user = savedInstanceState.getParcelable(USER_KEY);
            pinnedIssueMenuItems =
                    savedInstanceState.getParcelableArrayList(PINNED_ISSUES_MENU_KEY);
            allComments =
                    (IssueCommentCommon[]) savedInstanceState.getParcelableArray(ALL_COMMENTS_KEY);
            issue = savedInstanceState.getParcelable(ISSUE_KEY);
            pinnedIssues = savedInstanceState.getIntegerArrayList(PINNED_ISSUES_KEY);
            rotated = true;

        }
        else {

            user = extras.getParcelable(USER_KEY);
            issue = extras.getParcelable(ISSUE_KEY);

        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(issue.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                refreshIssue();
                loadUser();

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
                extras.putString(EditCommentActivity.ACTION_KEY, EditCommentActivity.ACTION_ADD);
                extras.putString(EditCommentActivity.TYPE_KEY, EditCommentActivity.TYPE_COMMENT);
                extras.putInt(EditCommentActivity.ISSUE_NUMBER_KEY, issue.getNumber());
                extras.putString(EditCommentActivity.REPO_NAME_KEY, repositoryName);
                addCommentIntent.putExtras(extras);
                startActivity(addCommentIntent);

            }

        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String token = preferences.getString(getString(R.string.oauth_token_key), null);

        service = ServiceGenerator.createService(token);
        if (user == null) {
            loadUser();
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

                            if (error.getMessage()
                                    .equals(getString(R.string.bad_credentials_error))) {

                                new AlertDialog.Builder(IssueDetailsActivity.this)
                                        .setTitle(R.string.login_credentials_expired_title)
                                        .setMessage(R.string.expired_credentials_message)
                                        .setPositiveButton(R.string.ok_button_text,
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {

                                                        SharedPreferences preferences =
                                                                PreferenceManager
                                                                        .getDefaultSharedPreferences(
                                                                                IssueDetailsActivity.this);
                                                        Editor editor = preferences.edit();
                                                        editor.putString(
                                                                getString(R.string.oauth_token_key),
                                                                null);
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

                            issue = response.body();
                            invalidateOptionsMenu();
                            loadComments();

                        }

                    }

                    @Override
                    public void onFailure(Call<Issue> call, Throwable t) {

                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(IssueDetailsActivity.this, R.string.network_error_toast,
                                Toast.LENGTH_LONG).show();

                    }
                });

    }

    private void loadPinnedIssues(GithubUser user) {

        if (user != null) {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            menuRecyclerView.setLayoutManager(linearLayoutManager);
            final PinnedIssueAdapter pinnedIssueAdapter = new PinnedIssueAdapter(user);
            menuRecyclerView.setAdapter(pinnedIssueAdapter);

            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

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
                                for (DataSnapshot ownerRepositories : pinnedIssueSnapshot
                                        .getChildren()) {
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
            else {

                swipeRefresh.setRefreshing(false);
                Toast.makeText(IssueDetailsActivity.this, R.string.network_error_toast,
                        Toast.LENGTH_LONG).show();

            }

        }

    }

    private void loadUser() {

        service.getAuthorizedUser().enqueue(new Callback<GithubUser>() {

            @Override
            public void onResponse(Call<GithubUser> call, Response<GithubUser> response) {

                if (response.code() == 401) {

                    Gson gson = new Gson();
                    APIRequestError error = null;
                    try {
                        error = gson.fromJson(response.errorBody().string(), APIRequestError.class);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (error.getMessage().equals(getString(R.string.bad_credentials_error))) {

                        new AlertDialog.Builder(IssueDetailsActivity.this)
                                .setTitle(R.string.login_credentials_expired_title)
                                .setPositiveButton(R.string.ok_button_text,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                SharedPreferences preferences = PreferenceManager
                                                        .getDefaultSharedPreferences(
                                                                IssueDetailsActivity.this);
                                                Editor editor = preferences.edit();
                                                editor.putString(
                                                        getString(R.string.oauth_token_key), null);
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

                    user = response.body();
                    loadPinnedIssues(user);
                    if (firstRun) {
                        loadComments();
                    }

                }

            }

            @Override
            public void onFailure(Call<GithubUser> call, Throwable t) {

                swipeRefresh.setRefreshing(false);
                Toast.makeText(IssueDetailsActivity.this, R.string.network_error_toast,
                        Toast.LENGTH_LONG).show();

            }
        });
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

                                if (error.getMessage()
                                        .equals(getString(R.string.bad_credentials_error))) {

                                    new AlertDialog.Builder(IssueDetailsActivity.this)
                                            .setTitle(R.string.login_credentials_expired_title)
                                            .setMessage(R.string.expired_credentials_message)
                                            .setPositiveButton(R.string.ok_button_text,
                                                    new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {

                                                            SharedPreferences preferences =
                                                                    PreferenceManager
                                                                            .getDefaultSharedPreferences(
                                                                                    IssueDetailsActivity.this);
                                                            Editor editor = preferences.edit();
                                                            editor.putString(getString(
                                                                    R.string.oauth_token_key),
                                                                    null);
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

                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(IssueDetailsActivity.this, R.string.network_error_toast,
                                    Toast.LENGTH_LONG).show();

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
        extras.putString(IssueListActivity.REPOSITORY_KEY, repositoryName);
        extras.putParcelable(IssueListActivity.USER_KEY, user);
        parentIntent.putExtras(extras);

        return parentIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.issue_menu, menu);

        MenuItem closeOpen = menu.findItem(R.id.action_close_open);
        MenuItem lockUnlock = menu.findItem(R.id.action_lock_unlock);
        MenuItem pinUnpin = menu.findItem(R.id.action_pin_unpin);
        if (issue.getState().equals(getString(R.string.issue_state_closed))) {

            closeOpen.setTitle(R.string.open_issue_titile);

        }
        if (issue.isLocked()) {

            lockUnlock.setTitle(R.string.unlock_issue_title);

        }
        if (pinned) {

            pinUnpin.setTitle(R.string.unpin_issue_title);
            pinUnpin.setIcon(R.drawable.ic_thumbtack_off_white_24dp);

        }

        return super.onCreateOptionsMenu(menu);

    }

    private void isPinned() {

        if (pinnedIssues != null) {

            pinned = pinnedIssues.contains(issue.getNumber());
            invalidateOptionsMenu();

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String[] repoNameSplit = repositoryName.split("/");
        switch (item.getItemId()) {

            case R.id.action_close_open:
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    swipeRefresh.setRefreshing(true);
                    IssueCloseOpenRequest closeOpenRequest = new IssueCloseOpenRequest();
                    if (issue.getState().equals(getString(R.string.issue_state_closed))) {

                        closeOpenRequest.setState(getString(R.string.issue_state_open));
                        service.openCloseIssue(repoNameSplit[0], repoNameSplit[1],
                                issue.getNumber(), closeOpenRequest).enqueue(

                                new Callback<Issue>() {

                                    @Override
                                    public void onResponse(Call<Issue> call,
                                                           Response<Issue> response) {

                                        switch (response.code()) {
                                            case 200:

                                                issue = response.body();
                                                invalidateOptionsMenu();
                                                loadComments();
                                                Toast.makeText(IssueDetailsActivity.this,
                                                        R.string.issue_opened_toast,
                                                        Toast.LENGTH_LONG).show();

                                                break;
                                            case 401: {

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

                                                if (error.getMessage().equals(getString(
                                                        R.string.bad_credentials_error))) {

                                                    new AlertDialog.Builder(IssueDetailsActivity
                                                            .this).setTitle(
                                                            R.string.login_credentials_expired_title)
                                                            .setMessage(
                                                                    R.string.expired_credentials_message)
                                                            .setPositiveButton(
                                                                    R.string.ok_button_text,
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
                                                                                    getString(
                                                                                            R.string.oauth_token_key),
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

                                                break;
                                            }
                                            default: {

                                                swipeRefresh.setRefreshing(false);
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

                                                break;
                                            }
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<Issue> call, Throwable t) {

                                        swipeRefresh.setRefreshing(false);
                                        Toast.makeText(IssueDetailsActivity.this,
                                                R.string.network_error_toast, Toast.LENGTH_LONG)
                                                .show();

                                    }
                                });

                    }
                    else {

                        closeOpenRequest.setState(getString(R.string.issue_state_closed));
                        service.openCloseIssue(repoNameSplit[0], repoNameSplit[1],
                                issue.getNumber(), closeOpenRequest).enqueue(

                                new Callback<Issue>() {

                                    @Override
                                    public void onResponse(Call<Issue> call,
                                                           Response<Issue> response) {

                                        switch (response.code()) {
                                            case 200:

                                                issue = response.body();
                                                invalidateOptionsMenu();
                                                loadComments();
                                                Toast.makeText(IssueDetailsActivity.this,
                                                        R.string.issue_closed_toast,
                                                        Toast.LENGTH_LONG).show();

                                                break;
                                            case 401: {

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

                                                if (error.getMessage().equals(getString(
                                                        R.string.bad_credentials_error))) {

                                                    new AlertDialog.Builder(IssueDetailsActivity
                                                            .this).setTitle(
                                                            R.string.login_credentials_expired_title)
                                                            .setMessage(
                                                                    R.string.expired_credentials_message)
                                                            .setPositiveButton(
                                                                    R.string.ok_button_text,
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
                                                                                    getString(
                                                                                            R.string.oauth_token_key),
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

                                                break;
                                            }
                                            default: {

                                                swipeRefresh.setRefreshing(false);
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

                                                break;
                                            }
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<Issue> call, Throwable t) {

                                        swipeRefresh.setRefreshing(false);
                                        Toast.makeText(IssueDetailsActivity.this,
                                                R.string.network_error_toast, Toast.LENGTH_LONG)
                                                .show();

                                    }
                                });

                    }
                    return true;
                }
                return false;

            case R.id.action_lock_unlock:
                connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    swipeRefresh.setRefreshing(true);
                    if (issue.isLocked()) {

                        service.unlockIssue(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                                .enqueue(

                                        new Callback<Issue>() {

                                            @Override
                                            public void onResponse(Call<Issue> call,
                                                                   Response<Issue> response) {

                                                switch (response.code()) {
                                                    case 204:

                                                        refreshIssue();

                                                        Toast.makeText(IssueDetailsActivity.this,
                                                                R.string.issue_unlocked_toast,
                                                                Toast.LENGTH_LONG).show();

                                                        break;
                                                    case 401: {

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

                                                        if (error.getMessage().equals(getString(
                                                                R.string.bad_credentials_error))) {

                                                            new AlertDialog.Builder(
                                                                    IssueDetailsActivity
                                                                            .this).setTitle(
                                                                    R.string.login_credentials_expired_title)
                                                                    .setMessage(
                                                                            R.string.expired_credentials_message)
                                                                    .setPositiveButton(
                                                                            R.string.ok_button_text,
                                                                            new DialogInterface
                                                                                    .OnClickListener() {

                                                                                @Override
                                                                                public void onClick(
                                                                                        DialogInterface dialog,
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
                                                                                            getString(
                                                                                                    R.string.oauth_token_key),
                                                                                            null);
                                                                                    editor.apply();
                                                                                    FirebaseAuth
                                                                                            .getInstance()
                                                                                            .signOut();

                                                                                    Intent
                                                                                            logoutIntent =
                                                                                            new Intent(
                                                                                                    IssueDetailsActivity.this,
                                                                                                    LoginActivity.class);
                                                                                    logoutIntent
                                                                                            .addFlags(
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

                                                        break;
                                                    }
                                                    default: {

                                                        swipeRefresh.setRefreshing(false);
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
                                                                error.getMessage(),
                                                                Toast.LENGTH_LONG).show();

                                                        break;
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onFailure(Call<Issue> call, Throwable t) {

                                                swipeRefresh.setRefreshing(false);
                                                Toast.makeText(IssueDetailsActivity.this,
                                                        R.string.network_error_toast,
                                                        Toast.LENGTH_LONG).show();

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

                                                switch (response.code()) {
                                                    case 204:

                                                        refreshIssue();
                                                        Toast.makeText(IssueDetailsActivity.this,
                                                                R.string.issue_locked_toast,
                                                                Toast.LENGTH_LONG).show();

                                                        break;
                                                    case 401: {

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

                                                        if (error.getMessage().equals(getString(
                                                                R.string.bad_credentials_error))) {

                                                            new AlertDialog.Builder(
                                                                    IssueDetailsActivity
                                                                            .this).setTitle(
                                                                    R.string.login_credentials_expired_title)
                                                                    .setMessage(
                                                                            R.string.expired_credentials_message)
                                                                    .setPositiveButton(
                                                                            R.string.ok_button_text,
                                                                            new DialogInterface
                                                                                    .OnClickListener() {

                                                                                @Override
                                                                                public void onClick(
                                                                                        DialogInterface dialog,
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
                                                                                            getString(
                                                                                                    R.string.oauth_token_key),
                                                                                            null);
                                                                                    editor.apply();
                                                                                    FirebaseAuth
                                                                                            .getInstance()
                                                                                            .signOut();

                                                                                    Intent
                                                                                            logoutIntent =
                                                                                            new Intent(
                                                                                                    IssueDetailsActivity.this,
                                                                                                    LoginActivity.class);
                                                                                    logoutIntent
                                                                                            .addFlags(
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

                                                        break;
                                                    }
                                                    default: {

                                                        swipeRefresh.setRefreshing(false);
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
                                                                error.getMessage(),
                                                                Toast.LENGTH_LONG).show();

                                                        break;
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onFailure(Call<Issue> call, Throwable t) {

                                                swipeRefresh.setRefreshing(false);
                                                Toast.makeText(IssueDetailsActivity.this,
                                                        R.string.network_error_toast,
                                                        Toast.LENGTH_LONG).show();

                                            }
                                        });

                    }
                    return true;
                }
                return false;
            case R.id.action_pin_unpin:
                connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
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
                        Toast.makeText(this, R.string.issue_unpinned_toast, Toast.LENGTH_LONG)
                                .show();

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
                        Toast.makeText(this, R.string.issue_pinned_toast, Toast.LENGTH_LONG).show();

                    }
                    return true;
                }
                return false;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!addComment.isShown() && !firstRun) {

            addComment.show();
            DatabaseReference connectedRef =
                    FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    connected = snapshot.getValue(Boolean.class);

                }

                @Override
                public void onCancelled(DatabaseError error) {

                }

            });

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
        outState.putParcelable(ISSUE_KEY, issue);
        outState.putParcelableArray(ALL_COMMENTS_KEY, allComments);
        outState.putParcelableArrayList(PINNED_ISSUES_MENU_KEY, pinnedIssueMenuItems);
        outState.putIntegerArrayList(PINNED_ISSUES_KEY, pinnedIssues);
        outState.putParcelable(USER_KEY, user);

    }
}
