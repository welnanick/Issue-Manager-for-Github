package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.flogger.FluentLogger;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.SearchResult;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public final class PinnedRepositoriesFragment extends Fragment implements NavigationHelper {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    @BindView(R.id.repository_recycler_view)
    RecyclerView repositoryRecyclerView;
    @BindView(R.id.pinned_repository_swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    private RepositoryAdapterMoshi repositoryAdapter;
    private NewMainActivity activity;
    private EditText searchText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logger.atInfo().log("onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_pinned_repositories, container, false);
        ButterKnife.bind(this, view);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String token = preferences.getString(getString(R.string.oauth_token_key), null);
        if (token == null) {
            logger.atInfo().log("User not logged in, launching login fragment");
            Navigation.findNavController(activity, R.id.nav_host_fragment)
                      .navigate(R.id.githubLogin);
        } else {
            logger.atInfo().log("User logged in, staying here");
            activity.setNavigationHelper(this);
            activity.invalidateOptionsMenu();
            activity.setTitle(R.string.pinned_repositories_title);
            activity.updateUserDataReference();
            activity.setService(ServiceGenerator.createService(token));
            activity.loadUser();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            repositoryRecyclerView.setLayoutManager(linearLayoutManager);
            repositoryAdapter = new RepositoryAdapterMoshi();
            repositoryRecyclerView.setAdapter(repositoryAdapter);
            swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
            swipeRefresh.setOnRefreshListener(() -> {
                if (!TextUtils.isEmpty(searchText.getText())) {
                    searchRepositories(searchText.getText().toString());
                } else {
                    activity.loadPinnedRepositories();
                }
            });
            swipeRefresh.setRefreshing(true);
            activity.loadPinnedRepositories();
            activity.loadPinnedIssues();
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger.atInfo().log("onAttach() called");
        this.activity = (NewMainActivity) context;
    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_query_hint));
        searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchRepositories(searchText.getText().toString());
            }
            return false;
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                updateProviderData();
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                repositoryAdapter.updateRepositories(new ArrayList<>());
                return true;
            }
        });
    }

    @Override
    public void updateProviderData() {
        repositoryAdapter.updateRepositories(activity.getPinnedRepositories());
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void searchRepositories(String searchQuery) {
        swipeRefresh.setRefreshing(true);
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }

        activity.getService().searchRepositories(searchQuery)
                .enqueue(new SearchRepositoryCallback());
    }

    private class SearchRepositoryCallback implements Callback<SearchResult> {
        @Override
        public void onResponse(@NonNull Call<SearchResult> call,
                               @NonNull Response<SearchResult> response) {

            if (response.code() == 401) {
                ResponseBody errorBody = response.errorBody();
                APIRequestError error = null;
                try {
                    String errorBodyJson = "";
                    if (errorBody != null) {
                        errorBodyJson = errorBody.string();
                    }
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<APIRequestError> jsonAdapter = moshi
                            .adapter(APIRequestError.class);
                    error = jsonAdapter.fromJson(errorBodyJson);
                } catch (IOException e) {
                    logger.atSevere().withCause(e).log("Error Body string() failed");
                }
                if (error != null) {
                    logger.atSevere().log(error.getMessage());
                }
            } else {
                SearchResult results = response.body();
                if (results != null) {
                    repositoryAdapter.updateRepositories(results.getItems());
                    swipeRefresh.setRefreshing(false);
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<SearchResult> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
