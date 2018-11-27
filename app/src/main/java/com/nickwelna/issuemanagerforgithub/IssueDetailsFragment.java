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
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommonMoshi;
import com.nickwelna.issuemanagerforgithub.models.IssueMoshi;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssueDetailsFragment extends Fragment implements OptionsMenuProvider {

    @BindView(R.id.comment_recycler_view)
    RecyclerView commentRecyclerView;
    private CommentAdapterMoshi commentAdapter;

    NewMainActivity activity;
    String repositoryName;
    IssueMoshi issue;

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

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
        issue = arguments.getParcelable(NewMainActivity.CURRENT_ISSUE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logger.atInfo().log("onCreateView() called");
        this.activity.setTitle(issue.getTitle());
        this.activity.setMenuProvider(this);
        activity.invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_issue_details, container, false);
        ButterKnife.bind(this, view);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        commentAdapter = new CommentAdapterMoshi(activity.getUser().getLogin());
        commentRecyclerView.setAdapter(commentAdapter);
        loadComments();
        return view;
    }

    private void loadComments() {
        logger.atInfo().log("loadComments() called");
        String[] repoNameSplit = repositoryName.split("/");
        activity.getService()
                .getCommentsMoshi(repoNameSplit[0], repoNameSplit[1], issue.getNumber())
                .enqueue(new GetCommentsCallback());

    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.issue_menu, menu);

        MenuItem closeOpen = menu.findItem(R.id.action_close_open);
        MenuItem lockUnlock = menu.findItem(R.id.action_lock_unlock);
        MenuItem pinUnpin = menu.findItem(R.id.action_pin_unpin);
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

    private boolean isPinned() {
        return activity.getPinnedIssues().contains(issue.getNumber());
    }

    private class GetCommentsCallback implements Callback<List<IssueCommentCommonMoshi>> {
        @Override
        public void onResponse(@NonNull Call<List<IssueCommentCommonMoshi>> call,
                               @NonNull Response<List<IssueCommentCommonMoshi>> response) {
            logger.atInfo().log("GetCommentsCallback onResponse() called");
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

                List<IssueCommentCommonMoshi> comments = response.body();
                comments.add(0, issue);
                commentAdapter.updateComments(comments);

            }
        }

        @Override
        public void onFailure(@NonNull Call<List<IssueCommentCommonMoshi>> call,
                              @NonNull Throwable t) {
            logger.atInfo().log("GetCommentsCallback onFailure() called");
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }

}
