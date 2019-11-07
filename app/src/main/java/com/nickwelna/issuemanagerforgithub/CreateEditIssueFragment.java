package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Splitter;
import com.google.common.flogger.FluentLogger;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueAddEditRequest;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CreateEditIssueFragment extends Fragment implements NavigationHelper {

    public static final String CREATE_ISSUE = "create_issue";
    public static final String ISSUE_NUMBER = "issue_number";
    public static final String ISSUE_NAME = "issue_name";
    public static final String ISSUE_BODY = "issue_body";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    @BindView(R.id.submit_button)
    Button submitButton;
    @BindView(R.id.title_input_layout)
    TextInputLayout titleInputLayout;
    @BindView(R.id.title_edit_text)
    TextInputEditText titleEditText;
    @BindView(R.id.body_edit_text)
    TextInputEditText bodyEditText;
    private String repositoryName;
    private boolean createIssue;
    private int issueNumber;
    @Nullable
    private String issueName;
    @Nullable
    private String issueBody;
    private NewMainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (NewMainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        createIssue = arguments.getBoolean(CREATE_ISSUE);
        if (!createIssue) {
            issueNumber = arguments.getInt(ISSUE_NUMBER);
            issueName = arguments.getString(ISSUE_NAME);
            issueBody = arguments.getString(ISSUE_BODY);
        }
        repositoryName = arguments.getString(NewMainActivity.REPOSITORY_NAME);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (createIssue) {
            this.activity.setTitle(R.string.create_issue_title);
        } else {
            this.activity.setTitle(R.string.edit_issue_title);

        }
        activity.setNavigationHelper(this);
        activity.invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_create_edit_issue, container, false);
        ButterKnife.bind(this, view);
        if (!createIssue) {
            titleEditText.setText(issueName);
            bodyEditText.setText(issueBody);
        }

        submitButton.setOnClickListener(v -> {
            Editable titleText = titleEditText.getText();
            if (titleText == null) {
                return;
            }
            if (TextUtils.isEmpty(titleText.toString())) {
                titleInputLayout.setError(getString(R.string.issue_title_error_text));
            } else {
                submitComment();
            }
        });
        return view;
    }

    private void submitComment() {

        Editable titleText = titleEditText.getText();
        Editable bodyText = bodyEditText.getText();
        if (titleText == null || bodyText == null) {
            return;
        }
        if (createIssue) {
            List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
            IssueAddEditRequest issueRequest = new IssueAddEditRequest();
            issueRequest.setTitle(titleText.toString());
            issueRequest.setBody(bodyText.toString());
            GitHubService service = activity.getService();
            if (service == null) {
                return;
            }
            service.addIssue(repoNameSplit.get(0), repoNameSplit.get(1), issueRequest)
                   .enqueue(new AddIssueCallback());
        } else {
            List<String> repoNameSplit = Splitter.on('/').splitToList(repositoryName);
            IssueAddEditRequest issueRequest = new IssueAddEditRequest();
            issueRequest.setTitle(titleText.toString());
            issueRequest.setBody(bodyText.toString());
            GitHubService service = activity.getService();
            if (service == null) {
                return;
            }
            service.editIssue(repoNameSplit.get(0), repoNameSplit.get(1), issueNumber,
                    issueRequest).enqueue(new EditIssueCallback());
        }

    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        // Intentionally left blank
    }

    @Override
    public void updateProviderData() {

    }

    private class AddIssueCallback implements Callback<Issue> {

        @Override
        public void onResponse(@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
            logger.atInfo().log("AddIssueCallback onResponse() called");
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
                Toast.makeText(activity, R.string.new_issue_submitted_toast, Toast.LENGTH_LONG)
                     .show();
                Navigation.findNavController(submitButton).popBackStack();
            }
        }

        @Override
        public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }

    private class EditIssueCallback implements Callback<Issue> {
        @Override
        public void onResponse(@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
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
                Toast.makeText(activity, R.string.issue_change_toast, Toast.LENGTH_LONG).show();
                Navigation.findNavController(submitButton).popBackStack();
            }
        }

        @Override
        public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
