package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.common.base.Splitter;
import com.google.common.flogger.FluentLogger;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.CommentAddEditRequest;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CreateEditCommentFragment extends Fragment implements NavigationHelper {

    public static final String CREATE_COMMENT = "create_comment";
    public static final String ISSUE_NUMBER = "issue_number";
    public static final String COMMENT_ID = "comment_id";
    public static final String COMMENT_BODY = "comment_body";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    @BindView(R.id.body_edit_text)
    TextInputEditText bodyEditText;
    @BindView(R.id.submit_button)
    Button submitButton;
    String repositoryName;
    int commentId;
    int issueNumber;
    private boolean createComment;
    private String commentBody;
    private NewMainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (NewMainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        createComment = arguments.getBoolean(CREATE_COMMENT);
        if (!createComment) {
            commentBody = arguments.getString(COMMENT_BODY);
            commentId = arguments.getInt(COMMENT_ID);
        } else {
            issueNumber = arguments.getInt(ISSUE_NUMBER);
        }
        repositoryName = arguments.getString(NewMainActivity.REPOSITORY_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (createComment) {
            this.activity.setTitle(R.string.create_comment_title);
        } else {
            this.activity.setTitle(R.string.edit_comment_title);

        }
        activity.setNavigationHelper(this);
        activity.invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_create_edit_comment, container, false);
        ButterKnife.bind(this, view);
        if (!createComment) {
            bodyEditText.setText(commentBody);
        }

        submitButton.setOnClickListener(v -> {
            submitComment();
        });
        return view;
    }

    private void submitComment() {

        if (createComment) {
            List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
            CommentAddEditRequest commentRequest = new CommentAddEditRequest();
            commentRequest.setBody(bodyEditText.getText().toString());
            activity.getService().addComment(repoNameSplit.get(0), repoNameSplit
                    .get(1), issueNumber, commentRequest)
                    .enqueue(new AddCommentCallback());
        } else {
            List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
            CommentAddEditRequest commentRequest = new CommentAddEditRequest();
            commentRequest.setBody(bodyEditText.getText().toString());
            activity.getService().editComment(repoNameSplit.get(0), repoNameSplit.get(1), commentId,
                    commentRequest).enqueue(new EditCommentCallback());
        }

    }

    @Override
    public void inflateOptionsMenu(Menu menu) {

    }

    @Override
    public void updateProviderData() {

    }

    private class AddCommentCallback implements Callback<Issue> {

        @Override
        public void onResponse(@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
            logger.atInfo().log("AddCommentCallback onResponse() called");
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
                Toast.makeText(activity, R.string.new_comment_toast, Toast.LENGTH_LONG).show();
                Navigation.findNavController(submitButton).popBackStack();
            }
        }

        @Override
        public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }

    private class EditCommentCallback implements Callback<Issue> {
        @Override
        public void onResponse(@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
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
                Toast.makeText(activity, R.string.comment_change_toast, Toast.LENGTH_LONG).show();
                Navigation.findNavController(submitButton).popBackStack();
            }
        }

        @Override
        public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
