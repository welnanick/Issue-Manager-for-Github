package com.nickwelna.issuemanagerforgithub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
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

import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Repository;
import com.nickwelna.issuemanagerforgithub.models.SearchResult;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

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
    Repository[] pinnedRepositories;
    String currentList = "pinned";
    boolean refreshRequested = false;
    SharedPreferences preferences;
    GitHubService service;
    EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        String token = preferences.getString("OAuth_token", null);

        service = ServiceGenerator.createService(token);
        service.getAuthorizedUser().enqueue(new Callback<GithubUser>() {

            @Override
            public void onResponse(Call<GithubUser> call, Response<GithubUser> response) {

                loadPinnedIssues(response.body());

            }

            @Override
            public void onFailure(Call<GithubUser> call, Throwable t) {

            }
        });

        loadPinnedRepositories();

    }

    private void refresh() {

        if (currentList.equals("pinned")) {

            refreshRequested = true;
            loadPinnedRepositories();

        }
        else {

            searchRepositories();

        }

    }

    private void loadPinnedRepositories() {

        if (pinnedRepositories == null || refreshRequested) {
            final Handler handler = new Handler();

            final Runnable r = new Runnable() {

                public void run() {

                    Gson gson = new Gson();
                    String test = getString(R.string.dummy_pinned_repos);
                    pinnedRepositories = gson.fromJson(test, Repository[].class);
                    repositoryAdapter.updateContents(pinnedRepositories);
                    swipeRefresh.setRefreshing(false);
                }

            };

            handler.postDelayed(r, 5000);
        }
        else {

            repositoryAdapter.updateContents(pinnedRepositories);

        }
        refreshRequested = false;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate the menu
        getMenuInflater().inflate(R.menu.options_menu, menu);

        //Find the menu search item
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        //get the searchView from the search item
        SearchView searchView = (SearchView) searchItem.getActionView();

        //Set the query hint for the search view
        searchView.setQueryHint("Search");

        //Access the Edit Text in the searchview
        searchText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        //make it so it performs our github search call when the user hits the search button on
        // the keyboard
        searchText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    searchRepositories();

                }
                return false;

            }

        });

        searchItem.setOnActionExpandListener(new OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                currentList = "pinned";
                loadPinnedRepositories();
                return true;

            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                currentList = "search";
                return true;

            }

        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        repositoryRecyclerView.setLayoutManager(linearLayoutManager);
        repositoryAdapter = new RepositoryAdapter();
        repositoryRecyclerView.setAdapter(repositoryAdapter);

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

                        SearchResult results = response.body();

                        if (results != null) {

                            repositoryAdapter.updateContents(results.getItems());

                        }
                        swipeRefresh.setRefreshing(false);

                    }

                    @Override
                    public void onFailure(Call<SearchResult> call, Throwable t) {

                    }

                });

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
