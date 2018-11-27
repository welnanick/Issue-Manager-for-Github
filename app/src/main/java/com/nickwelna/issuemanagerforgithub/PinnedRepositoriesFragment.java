package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.nickwelna.issuemanagerforgithub.models.APIRequestErrorMoshi;
import com.nickwelna.issuemanagerforgithub.models.SearchResultMoshi;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class PinnedRepositoriesFragment extends Fragment implements OptionsMenuProvider {

    @BindView(R.id.repository_recycler_view)
    RecyclerView repositoryRecyclerView;

    private RepositoryAdapterMoshi repositoryAdapter;
    private NewMainActivity activity;

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logger.atInfo().log("onCreateView() called");
        activity.setTitle(R.string.pinned_repositories_title);
        activity.setMenuProvider(this);
        activity.invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_pinned_repositories, container, false);
        ButterKnife.bind(this, view);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String token = preferences.getString(getString(R.string.oauth_token_key), null);
        if (token == null) {
            logger.atInfo().log("User not logged in, navigating to login fragment");
            Navigation.findNavController(activity, R.id.nav_host_fragment)
                      .navigate(R.id.action_pinnedRepositories_to_githubLogin);
        }
        else {
            logger.atInfo().log("User logged in, staying here");
            activity.updateUserDataReference();
            activity.setService(ServiceGenerator.createService(token));
            activity.loadUser();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            repositoryRecyclerView.setLayoutManager(linearLayoutManager);
            repositoryAdapter = new RepositoryAdapterMoshi();
            repositoryRecyclerView.setAdapter(repositoryAdapter);
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
        //Inflate the menu
        activity.getMenuInflater().inflate(R.menu.options_menu, menu);

        //Find the menu search item
        MenuItem searchItem = menu.findItem(R.id.action_search);

        //get the searchView from the search item
        SearchView searchView = (SearchView) searchItem.getActionView();

        //Set the query hint for the search view
        searchView.setQueryHint(getString(R.string.search_query_hint));

        //Access the Edit Text in the search view
        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        //make it so it performs our github search call when the user hits the search button on
        // the keyboard
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void searchRepositories(String searchQuery) {
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }

        activity.getService().searchRepositoriesMoshi(searchQuery)
                .enqueue(new SearchRepositoryCallback());
    }

    private class SearchRepositoryCallback implements Callback<SearchResultMoshi> {
        @Override
        public void onResponse(@NonNull Call<SearchResultMoshi> call,
                               @NonNull Response<SearchResultMoshi> response) {

            if (response.code() == 401) {
                ResponseBody errorBody = response.errorBody();
                APIRequestErrorMoshi error = null;
                try {
                    String errorBodyJson = "";
                    if (errorBody != null) {
                        errorBodyJson = errorBody.string();
                    }
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<APIRequestErrorMoshi> jsonAdapter = moshi
                            .adapter(APIRequestErrorMoshi.class);
                    error = jsonAdapter.fromJson(errorBodyJson);
                }
                catch (IOException e) {
                    logger.atSevere().withCause(e).log("Error Body string() failed");
                }
                if (error != null) {
                    logger.atSevere().log(error.getMessage());
                }
            }
            else {
                SearchResultMoshi results = response.body();
                if (results != null) {
                    repositoryAdapter.updateRepositories(results.getItems());
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<SearchResultMoshi> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
