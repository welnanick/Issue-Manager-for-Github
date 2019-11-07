package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.google.common.flogger.FluentLogger;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.Repository;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
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

public final class RepositoryDetailsFragment extends Fragment implements NavigationHelper {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    @BindView(R.id.issue_recycler_view)
    RecyclerView issueRecyclerView;
    @BindView(R.id.repository_issues_swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    private IssueAdapterMoshi issueAdapter;
    private String repositoryName;
    private NewMainActivity activity;

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
        this.activity.setTitle(repositoryName);
        this.activity.setNavigationHelper(this);
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
        List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
        GitHubService service = activity.getService();
        if (service == null) {
            return;
        }
        service.getIssues(repoNameSplit.get(0), repoNameSplit.get(1), "all")
                .enqueue(new GetIssueCallback());
    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.repository_menu, menu);
        MenuItem pinUnpin = menu.findItem(R.id.action_pin_unpin);
        if (isPinned()) {
            pinUnpin.setTitle(R.string.unpin_repository_title);
            pinUnpin.setIcon(R.drawable.ic_thumbtack_off_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pin_unpin) {
            if (isPinned()) {
                activity.removePinnedRepository(repositoryName);
            } else {
                activity.addPinnedRepository(repositoryName);
            }
            activity.loadPinnedRepositories();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isPinned() {
        List<Repository> pinnedRepositories = activity.getPinnedRepositories();
        for (Repository repository : pinnedRepositories) {
            if (repository.getFullName().equals(repositoryName)) {
                return true;
            }
        }
        return false;
    }

    private class GetIssueCallback implements Callback<List<Issue>> {
        @Override
        public void onResponse(@NonNull Call<List<Issue>> call,
                               @NonNull Response<List<Issue>> response) {
            logger.atInfo().log("GetIssueCallback onResponse() called");
            if (response.code() == 401) {
                ResponseBody errorBody = response.errorBody();
                @Nullable APIRequestError error = null;
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
                issueAdapter.updateIssues(response.body());
                swipeRefresh.setRefreshing(false);
                activity.showFab();
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
            logger.atInfo().log("GetIssueCallback onFailure() called");
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
