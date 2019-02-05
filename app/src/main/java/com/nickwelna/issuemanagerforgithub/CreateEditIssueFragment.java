package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.flogger.FluentLogger;
import com.nickwelna.issuemanagerforgithub.models.APIRequestErrorMoshi;
import com.nickwelna.issuemanagerforgithub.models.IssueAddEditRequestMoshi;
import com.nickwelna.issuemanagerforgithub.models.IssueMoshi;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEditIssueFragment extends Fragment implements OptionsMenuProvider {

    @BindView(R.id.submit_button)
    Button submitButton;
    @BindView(R.id.title_input_layout)
    TextInputLayout titleInputLayout;
    @BindView(R.id.title_edit_text)
    TextInputEditText titleEditText;
    @BindView(R.id.body_edit_text)
    TextInputEditText bodyEditText;

    public static final String CREATE_ISSUE = "create_issue";
    public static final String ISSUE_NUMBER = "issue_number";
    public static final String ISSUE_NAME = "issue_name";
    public static final String ISSUE_BODY = "issue_body";
    private boolean createIssue;
    private int issueNumber;
    private String issueName;
    private String issueBody;
    String repositoryName;
    private NewMainActivity activity;

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (NewMainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        createIssue = arguments.getBoolean(CREATE_ISSUE);
        if (!createIssue) {
            issueNumber = arguments.getInt(ISSUE_NUMBER);
            issueName = arguments.getString(ISSUE_NAME);
            issueBody = arguments.getString(ISSUE_BODY);
        }
        repositoryName = arguments.getString(NewMainActivity.REPOSITORY_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (createIssue) {
            this.activity.setTitle(R.string.create_issue_title);
        } else {
            this.activity.setTitle(R.string.edit_issue_title);

        }
        activity.setMenuProvider(this);
        activity.invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_create_edit_issue, container, false);
        ButterKnife.bind(this, view);
        if(!createIssue) {
            titleEditText.setText(issueName);
            bodyEditText.setText(issueBody);
        }

        submitButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(titleEditText.getText().toString())) {
                titleInputLayout.setError(getString(R.string.issue_title_error_text));
            } else {
                submitComment();
            }
        });
        return view;
    }

    private void submitComment() {

        if (createIssue) {
            String[] repoNameSplit = repositoryName.split("/");
            IssueAddEditRequestMoshi issueRequest = new IssueAddEditRequestMoshi();
            issueRequest.setTitle(titleEditText.getText().toString());
            issueRequest.setBody(bodyEditText.getText().toString());
            activity.getService().addIssueMoshi(repoNameSplit[0], repoNameSplit[1], issueRequest)
                    .enqueue(new AddIssueCallback());
        } else {
            String[] repoNameSplit = repositoryName.split("/");
            IssueAddEditRequestMoshi issueRequest = new IssueAddEditRequestMoshi();
            issueRequest.setTitle(titleEditText.getText().toString());
            issueRequest.setBody(bodyEditText.getText().toString());
            activity.getService().editIssueMoshi(repoNameSplit[0], repoNameSplit[1], issueNumber,
                    issueRequest).enqueue(new EditIssueCallback());
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        // Intentionally left blank
    }

    @Override
    public void updateProviderData() {

    }

    private class AddIssueCallback implements Callback<IssueMoshi> {

        @Override
        public void onResponse(@NonNull Call<IssueMoshi> call, @NonNull Response<IssueMoshi> response) {
            logger.atInfo().log("AddIssueCallback onResponse() called");
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
                } catch (IOException e) {
                    logger.atSevere().withCause(e).log("Error Body string() failed");
                }
                if (error != null) {
                    logger.atSevere().log(error.getMessage());
                }
            } else {
                Toast.makeText(activity, R.string.new_issue_submitted_toast, Toast.LENGTH_LONG).show();
                Navigation.findNavController(submitButton).popBackStack();
            }
        }

        @Override
        public void onFailure(@NonNull Call<IssueMoshi> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }

    private class EditIssueCallback implements Callback<IssueMoshi> {
        @Override
        public void onResponse(@NonNull Call<IssueMoshi> call, @NonNull Response<IssueMoshi> response) {
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
        public void onFailure(@NonNull Call<IssueMoshi> call, @NonNull Throwable t) {
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
        }
    }
}
