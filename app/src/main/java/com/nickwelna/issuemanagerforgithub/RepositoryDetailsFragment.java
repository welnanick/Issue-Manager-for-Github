package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.flogger.FluentLogger;
import com.nickwelna.issuemanagerforgithub.models.APIRequestErrorMoshi;
import com.nickwelna.issuemanagerforgithub.models.IssueMoshi;
import com.nickwelna.issuemanagerforgithub.models.RepositoryMoshi;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
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

public class RepositoryDetailsFragment extends Fragment implements OptionsMenuProvider {

    @BindView(R.id.issue_recycler_view)
    RecyclerView issueRecyclerView;
    @BindView(R.id.repository_issues_swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    private IssueAdapterMoshi issueAdapter;
    private String repositoryName;

    private NewMainActivity activity;

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.atInfo().log("onCreate() called");
        Bundle arguments = getArguments();
        repositoryName = arguments.getString(NewMainActivity.REPOSITORY_NAME);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger.atInfo().log("onAttach() called");
        this.activity = (NewMainActivity) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logger.atInfo().log("onCreateView() called");
        activity.showFab();
        this.activity.setTitle(repositoryName);
        this.activity.setMenuProvider(this);
        activity.invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_repository_details, container, false);
        activity.setFabClick(v -> {
            NavController controller = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putBoolean(CreateEditIssueFragment.CREATE_ISSUE, true);
            args.putString(NewMainActivity.REPOSITORY_NAME, repositoryName);
            controller.navigate(R.id.action_repositoryDetails_to_createEditIssue, args);
        });
        ButterKnife.bind(this, view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        issueRecyclerView.setLayoutManager(linearLayoutManager);
        issueAdapter = new IssueAdapterMoshi(repositoryName);
        issueRecyclerView.setAdapter(issueAdapter);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(this::loadIssues);
        swipeRefresh.setRefreshing(true);
        loadIssues();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        logger.atInfo().log("onDestroyView() called");
        activity.hideFab();
    }

    private void loadIssues() {
        logger.atInfo().log("loadIssues() called");
        String[] repoNameSplit = repositoryName.split("/");
        activity.getService().getIssuesMoshi(repoNameSplit[0], repoNameSplit[1], "all")
                .enqueue(new GetIssueCallback());
    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.repository_menu, menu);
        MenuItem pinUnpin = menu.findItem(R.id.action_pin_unpin);
        if (isPinned()) {
            pinUnpin.setTitle(R.string.unpin_repository_title);
            pinUnpin.setIcon(R.drawable.ic_thumbtack_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pin_unpin:
                if (isPinned()) {
                    activity.removePinnedRepository(repositoryName);
                }
                else {
                    activity.addPinnedRepository(repositoryName);
                }
                activity.loadPinnedRepositories();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isPinned() {
        List<RepositoryMoshi> pinnedRepositories = activity.getPinnedRepositories();
        for (RepositoryMoshi repository : pinnedRepositories) {
            if (repository.getFullName().equals(repositoryName)) {
                return true;
            }
        }
        return false;
    }

    private class GetIssueCallback implements Callback<List<IssueMoshi>> {
        @Override
        public void onResponse(@NonNull Call<List<IssueMoshi>> call,
                               @NonNull Response<List<IssueMoshi>> response) {
            logger.atInfo().log("GetIssueCallback onResponse() called");
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
                issueAdapter.updateIssues(response.body());
                swipeRefresh.setRefreshing(false);
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<IssueMoshi>> call, @NonNull Throwable t) {
            logger.atInfo().log("GetIssueCallback onFailure() called");
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
