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
import com.nickwelna.issuemanagerforgithub.models.IssueCloseOpenRequest;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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

public final class IssueDetailsFragment extends Fragment implements NavigationHelper {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    @BindView(R.id.comment_recycler_view)
    RecyclerView commentRecyclerView;
    @BindView(R.id.issue_details_swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    NewMainActivity activity;
    String repositoryName;
    int issueNumber;

    @Nullable
    Issue issue;
    private CommentAdapterMoshi commentAdapter;

    @Override
    public void onAttach(Context context) {
        logger.atInfo().log("onAttach() called");
        super.onAttach(context);
        activity = (NewMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.atInfo().log("onCreate() called");
        Bundle arguments = getArguments();
        repositoryName = arguments.getString(NewMainActivity.REPOSITORY_NAME);
        issueNumber = arguments.getInt(NewMainActivity.CURRENT_ISSUE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logger.atInfo().log("onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_issue_details, container, false);
        activity.setFabClick(v -> {
            NavController controller = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putBoolean(CreateEditCommentFragment.CREATE_COMMENT, true);
            args.putString(NewMainActivity.REPOSITORY_NAME, repositoryName);
            args.putInt(CreateEditCommentFragment.ISSUE_NUMBER, issueNumber);
            controller.navigate(R.id.action_issueDetails_to_createEditComment, args);
        });
        ButterKnife.bind(this, view);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        commentAdapter = new CommentAdapterMoshi(activity, repositoryName, this);
        commentRecyclerView.setAdapter(commentAdapter);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(this::loadIssue);
        swipeRefresh.setRefreshing(true);
        loadIssue();
        return view;
    }

    private void loadIssue() {
        logger.atInfo().log("loadIssue() called");
        List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
        GitHubService service = activity.getService();
        if (service == null) {
            return;
        }
        service.getIssue(repoNameSplit.get(0), repoNameSplit.get(1), issueNumber)
               .enqueue(new LoadIssueCallback());
    }

    void loadComments() {
        swipeRefresh.setRefreshing(true);
        logger.atInfo().log("loadComments() called");
        List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
        GitHubService service = activity.getService();
        if (service == null) {
            return;
        }
        service.getComments(repoNameSplit.get(0), repoNameSplit.get(1), issueNumber)
               .enqueue(new GetCommentsCallback());

    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.issue_menu, menu);

        MenuItem closeOpen = menu.findItem(R.id.action_close_open);
        MenuItem lockUnlock = menu.findItem(R.id.action_lock_unlock);
        MenuItem pinUnpin = menu.findItem(R.id.action_pin_unpin);
        if (issue == null) {
            logger.atWarning().log("issue is null");
            return;
        }
        if (issue.getState().equals(getString(R.string.issue_state_closed))) {
            closeOpen.setTitle(R.string.open_issue_titile);
        }
        if (issue.isLocked()) {
            lockUnlock.setTitle(R.string.unlock_issue_title);
        }
        if (isPinned()) {
            pinUnpin.setTitle(R.string.unpin_issue_title);
            pinUnpin.setIcon(R.drawable.ic_thumbtack_off_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
        if (issue == null) {
            logger.atWarning().log("issue is null");
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_pin_unpin:
                if (isPinned()) {
                    activity.removePinnedIssue(repositoryName, issue.getNumber());
                } else {
                    activity.addPinnedIssue(repositoryName, issue.getNumber());
                }
                activity.loadPinnedRepositories();
                return true;
            case R.id.action_lock_unlock:
                swipeRefresh.setRefreshing(true);
                if (issue.isLocked()) {
                    GitHubService service = activity.getService();
                    if (service == null) {
                        return false;
                    }
                    service.unlockIssue(
                            repoNameSplit.get(0),
                            repoNameSplit.get(1),
                            issue.getNumber())
                           .enqueue(new UnlockIssueCallback());
                } else {
                    GitHubService service = activity.getService();
                    if (service == null) {
                        return false;
                    }
                    service.lockIssue(
                            repoNameSplit.get(0),
                            repoNameSplit.get(1),
                            issue.getNumber())
                           .enqueue(new LockIssueCallback());

                }
                return true;
            case R.id.action_close_open:
                swipeRefresh.setRefreshing(true);
                IssueCloseOpenRequest closeOpenRequest = new IssueCloseOpenRequest();
                if (issue.getState().equals(getString(R.string.issue_state_closed))) {
                    closeOpenRequest.setState(getString(R.string.issue_state_open));
                    GitHubService service = activity.getService();
                    if (service == null) {
                        return false;
                    }
                    service.openCloseIssue(repoNameSplit.get(0), repoNameSplit.get(1),
                            issue.getNumber(), closeOpenRequest)
                           .enqueue(new IssueOpenCloseCallback(R.string.issue_opened_toast));
                } else {
                    closeOpenRequest.setState(getString(R.string.issue_state_closed));
                    GitHubService service = activity.getService();
                    if (service == null) {
                        return false;
                    }
                    service.openCloseIssue(repoNameSplit.get(0), repoNameSplit.get(1),
                            issue.getNumber(), closeOpenRequest)
                           .enqueue(new IssueOpenCloseCallback(R.string.issue_closed_toast));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        logger.atInfo().log("onDestroyView() called");
        activity.hideFab();
    }

    private boolean isPinned() {
        @Nullable List<Integer> pinnedIssues = activity.getPinnedIssues().get(repositoryName);
        if (pinnedIssues == null) {
            return false;
        }
        if (issue == null) {
            logger.atWarning().log("issue is null");
            return false;
        }
        return pinnedIssues.contains(issue.getNumber());
    }

    private class GetCommentsCallback implements Callback<List<IssueCommentCommon>> {
        @Override
        public void onResponse(@NonNull Call<List<IssueCommentCommon>> call,
                               @NonNull Response<List<IssueCommentCommon>> response) {
            logger.atInfo().log("GetCommentsCallback onResponse() called");
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

                List<IssueCommentCommon> comments = response.body();
                comments.add(0, issue);
                commentAdapter.updateComments(comments);
                swipeRefresh.setRefreshing(false);
                activity.showFab();

            }
        }

        @Override
        public void onFailure(@NonNull Call<List<IssueCommentCommon>> call,
                              @NonNull Throwable t) {
            logger.atInfo().log("GetCommentsCallback onFailure() called");
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }

    private class LoadIssueCallback implements Callback<Issue> {
        @Override
        public void onResponse(Call<Issue> call, Response<Issue> response) {
            logger.atInfo().log("LoadIssueCallback onResponse() called");
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
                issue = response.body();
                activity.setTitle(issue.getTitle());
                activity.setNavigationHelper(IssueDetailsFragment.this);
                activity.invalidateOptionsMenu();
                loadComments();
            }
        }

        @Override
        public void onFailure(Call<Issue> call, Throwable t) {

        }
    }

    private class UnlockIssueCallback implements Callback<Issue> {

        @Override
        public void onResponse(Call<Issue> call, Response<Issue> response) {
            switch (response.code()) {
                case 204:
                    loadIssue();
                    Toast.makeText(activity, R.string.issue_unlocked_toast, Toast.LENGTH_LONG)
                         .show();
                    break;
                case 401:
                default:
                    swipeRefresh.setRefreshing(false);
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
                        Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

        @Override
        public void onFailure(Call<Issue> call, Throwable t) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }

    private class LockIssueCallback implements Callback<Issue> {

        @Override
        public void onResponse(Call<Issue> call, Response<Issue> response) {

            switch (response.code()) {
                case 204:
                    loadIssue();
                    Toast.makeText(activity, R.string.issue_locked_toast, Toast.LENGTH_LONG).show();
                    break;
                case 401:
                default:
                    swipeRefresh.setRefreshing(false);
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
                        Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

        @Override
        public void onFailure(Call<Issue> call, Throwable t) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }

    private class IssueOpenCloseCallback implements Callback<Issue> {

        @StringRes
        int toastText;

        IssueOpenCloseCallback(@StringRes int toastText) {
            this.toastText = toastText;
        }

        @Override
        public void onResponse(Call<Issue> call, Response<Issue> response) {
            switch (response.code()) {
                case 200:
                    loadIssue();
                    Toast.makeText(activity, toastText, Toast.LENGTH_LONG).show();
                    break;
                case 401:
                default:
                    swipeRefresh.setRefreshing(false);
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
                        Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

        @Override
        public void onFailure(Call<Issue> call, Throwable t) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
