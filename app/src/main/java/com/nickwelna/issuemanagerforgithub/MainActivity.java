package com.nickwelna.issuemanagerforgithub;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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
import com.nickwelna.issuemanagerforgithub.models.Repository;
import com.nickwelna.issuemanagerforgithub.models.SearchResult;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.repository_recycler_view)
    RecyclerView repositoryRecyclerView;
    @BindView(R.id.menu_recycler_view)
    RecyclerView menuRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    RepositoryAdapter repositoryAdapter;
    ArrayList<Repository> visibleRepositories;
    String currentList = CURRENT_LIST_PINNED;
    boolean refreshRequested = false;
    SharedPreferences preferences;
    GitHubService service;
    EditText searchText;
    GithubUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference userDataReference =
            database.getReference("users").child(auth.getCurrentUser().getUid());
    ArrayList<PinnedIssueMenuItem> pinnedIssueMenuItems;
    boolean firstRun;
    MenuItem searchItem;
    boolean loadSearch = false;
    String searchTextString;
    boolean rotated;
    boolean connected;
    public static final String CURRENT_LIST_PINNED = "pinned";
    public static final String CURRENT_LIST_SEARCH = "search";
    public static final String USER_KEY = "user";
    public static final String PINNED_ISSUES_KEY = "pinned_issues";
    public static final String VISIBLE_REPOSITORIES_KEY = "visible_repositories";
    public static final String LOAD_SEARCH_KEY = "load_search";
    public static final String SEARCH_TEXT_KEY = "search_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        firstRun = true;

        if (savedInstanceState != null) {

            user = savedInstanceState.getParcelable(USER_KEY);
            pinnedIssueMenuItems = savedInstanceState.getParcelableArrayList(PINNED_ISSUES_KEY);
            visibleRepositories =
                    savedInstanceState.getParcelableArrayList(VISIBLE_REPOSITORIES_KEY);
            loadSearch = savedInstanceState.getBoolean(LOAD_SEARCH_KEY);
            searchTextString = savedInstanceState.getString(SEARCH_TEXT_KEY);
            rotated = true;

        }

        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {

            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        }
        swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                refresh();

            }

        });
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setRefreshing(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        repositoryRecyclerView.setLayoutManager(linearLayoutManager);
        repositoryAdapter = new RepositoryAdapter();
        repositoryRecyclerView.setAdapter(repositoryAdapter);

        String token = preferences.getString(getString(R.string.oauth_token_key), null);

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

                        if (error.getMessage().equals(getString(R.string.bad_credentials_error))) {

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.login_credentials_expired_title)
                                    .setPositiveButton(R.string.ok_button_text,
                                            new OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {

                                                    SharedPreferences preferences =
                                                            PreferenceManager
                                                                    .getDefaultSharedPreferences(
                                                                            MainActivity.this);
                                                    Editor editor = preferences.edit();
                                                    editor.putString(
                                                            getString(R.string.oauth_token_key),
                                                            null);
                                                    editor.apply();
                                                    FirebaseAuth.getInstance().signOut();

                                                    Intent logoutIntent =
                                                            new Intent(MainActivity.this,
                                                                    LoginActivity.class);
                                                    logoutIntent.addFlags(
                                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    dialog.dismiss();
                                                    MainActivity.this.startActivity(logoutIntent);

                                                }

                                            }).create().show();

                        }

                    }
                    else {

                        user = response.body();
                        repositoryAdapter.updateUser(user);
                        loadPinnedIssues(user);
                        loadPinnedRepositories();

                    }

                }

                @Override
                public void onFailure(Call<GithubUser> call, Throwable t) {

                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MainActivity.this, R.string.network_error_toast,
                            Toast.LENGTH_LONG).show();

                }
            });
        }
        else {

            repositoryAdapter.updateUser(user);
            loadPinnedIssues(user);
            loadPinnedRepositories();

        }

    }

    private void refresh() {

        if (currentList.equals(CURRENT_LIST_PINNED)) {

            refreshRequested = true;
            loadPinnedRepositories();

        }
        else {

            searchRepositories();

        }

    }

    private void loadPinnedRepositories() {

        firebaseConnected();

        if (connected && (visibleRepositories == null || refreshRequested || !rotated)) {

            DatabaseReference pinnedRepos = userDataReference.child("pinned_repos");
            ValueEventListener pinnedRepositoryListener = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    visibleRepositories = new ArrayList<>();
                    for (DataSnapshot pinnedRepoSnapshot : dataSnapshot.getChildren()) {

                        Repository temp = new Repository();
                        temp.setFullName(pinnedRepoSnapshot.getValue(String.class));
                        visibleRepositories.add(temp);

                    }
                    swipeRefresh.setRefreshing(false);
                    repositoryAdapter.updateContents(visibleRepositories);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            pinnedRepos.addListenerForSingleValueEvent(pinnedRepositoryListener);
            refreshRequested = false;

        }
        else if (!connected) {

            swipeRefresh.setRefreshing(false);

        }
        else {

            swipeRefresh.setRefreshing(false);
            repositoryAdapter.updateContents(visibleRepositories);

        }

    }

    private void loadPinnedIssues(GithubUser user) {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        menuRecyclerView.setLayoutManager(linearLayoutManager);
        final PinnedIssueAdapter pinnedIssueAdapter = new PinnedIssueAdapter(user);
        menuRecyclerView.setAdapter(pinnedIssueAdapter);

        firebaseConnected();

        if (connected && (pinnedIssueMenuItems == null || !rotated)) {

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
        else if (!connected) {

            swipeRefresh.setRefreshing(false);
            Toast.makeText(MainActivity.this, R.string.network_error_toast, Toast.LENGTH_LONG)
                    .show();

        }
        else {

            pinnedIssueAdapter.updatePinnedRepositories(pinnedIssueMenuItems);

        }

    }

    @Override
    protected void onResume() {

        super.onResume();
        if (user != null && !firstRun) {

            loadPinnedIssues(user);
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
        if (firstRun) {

            firstRun = false;

        }
        if (!rotated) {

            refresh();

        }
        if (rotated) {

            rotated = false;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate the menu
        getMenuInflater().inflate(R.menu.options_menu, menu);

        //Find the menu search item
        searchItem = menu.findItem(R.id.action_search);

        //get the searchView from the search item
        SearchView searchView = (SearchView) searchItem.getActionView();

        //Set the query hint for the search view
        searchView.setQueryHint(getString(R.string.search_query_hint));

        //Access the Edit Text in the searchview
        searchText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        //make it so it performs our github search call when the user hits the search button on
        // the keyboard
        searchText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    visibleRepositories = null;
                    searchRepositories();

                }
                return false;

            }

        });

        searchItem.setOnActionExpandListener(new OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                currentList = CURRENT_LIST_PINNED;
                visibleRepositories = null;
                loadPinnedRepositories();
                return true;

            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                currentList = CURRENT_LIST_SEARCH;
                return true;

            }

        });

        if (loadSearch) {

            searchItem.expandActionView();
            searchText.setText(searchTextString);

        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * This method performs our Github Search. It dismisses the keyboard, shows and hides the
     * progress bar, and performs the search
     */
    private void searchRepositories() {

        if (visibleRepositories == null || !rotated) {
            swipeRefresh.setRefreshing(true);

            repositoryAdapter.updateContents(null);

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (imm != null && imm.isAcceptingText()) {

                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            service.searchRepositories(searchText.getText().toString())
                    .enqueue(new Callback<SearchResult>() {

                        @Override
                        public void onResponse(Call<SearchResult> call,
                                               Response<SearchResult> response) {

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

                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.login_credentials_expired_title)
                                            .setPositiveButton(R.string.ok_button_text,
                                                    new OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {

                                                            SharedPreferences preferences =
                                                                    PreferenceManager
                                                                            .getDefaultSharedPreferences(
                                                                                    MainActivity
                                                                                            .this);
                                                            Editor editor = preferences.edit();
                                                            editor.putString(getString(
                                                                    R.string.oauth_token_key),
                                                                    null);
                                                            editor.apply();
                                                            FirebaseAuth.getInstance().signOut();

                                                            Intent logoutIntent =
                                                                    new Intent(MainActivity.this,
                                                                            LoginActivity.class);
                                                            logoutIntent.addFlags(
                                                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            dialog.dismiss();
                                                            MainActivity.this
                                                                    .startActivity(logoutIntent);

                                                        }

                                                    }).create().show();

                                }

                            }
                            else {

                                SearchResult results = response.body();

                                if (results != null) {

                                    visibleRepositories = results.getItems();
                                    repositoryAdapter.updateContents(visibleRepositories);

                                }
                                swipeRefresh.setRefreshing(false);

                            }

                        }

                        @Override
                        public void onFailure(Call<SearchResult> call, Throwable t) {

                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(MainActivity.this, R.string.network_error_toast,
                                    Toast.LENGTH_LONG).show();

                        }

                    });
        }
        else {

            repositoryAdapter.updateContents(visibleRepositories);

        }

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
        outState.putParcelable(USER_KEY, user);
        outState.putParcelableArrayList(PINNED_ISSUES_KEY, pinnedIssueMenuItems);
        outState.putParcelableArrayList(VISIBLE_REPOSITORIES_KEY, visibleRepositories);
        if (searchText != null && currentList.equals(CURRENT_LIST_SEARCH)) {
            outState.putBoolean(LOAD_SEARCH_KEY, true);
            outState.putString(SEARCH_TEXT_KEY, searchText.getText().toString());
        }
        else {
            outState.putBoolean(LOAD_SEARCH_KEY, false);
        }

    }

    private void firebaseConnected() {

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

}
