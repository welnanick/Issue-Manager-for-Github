package com.nickwelna.issuemanagerforgithub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.CommentAddEditRequest;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueAddEditRequest;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCommentActivity extends AppCompatActivity {

    @BindView(R.id.title_input_layout)
    TextInputLayout titleInputLayout;
    @BindView(R.id.title_edit_text)
    TextInputEditText titleEditText;
    @BindView(R.id.body_edit_text)
    TextInputEditText bodyEditText;
    @BindView(R.id.submit_button)
    Button submitButton;
    GitHubService service;
    SharedPreferences preferences;
    String repositoryName;
    int commentId;
    int issueNumber;
    public static final String ACTION_KEY = "action";
    public static final String TYPE_KEY = "type";
    public static final String COMMENT_KEY = "comment";
    public static final String REPO_NAME_KEY = "repo_name";
    public static final String ISSUE_NUMBER_KEY = "issue_number";
    public static final String COMMENT_ID_KEY = "action";
    public static final String ACTION_ADD = "add";
    public static final String ACTION_EDIT = "edit";
    public static final String TYPE_ISSUE = "issue";
    public static final String TYPE_COMMENT = "comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        final String action = extras.getString(ACTION_KEY);
        final String type = extras.getString(TYPE_KEY);
        IssueCommentCommon comment = extras.getParcelable(COMMENT_KEY);
        repositoryName = extras.getString(REPO_NAME_KEY);
        issueNumber = extras.getInt(ISSUE_NUMBER_KEY);
        commentId = extras.getInt(COMMENT_ID_KEY);
        StringBuilder titleBuilder = new StringBuilder();
        switch (action) {

            case ACTION_ADD:
                titleBuilder.append(getString(R.string.add_title_append));
                break;

            case ACTION_EDIT:
                titleBuilder.append(getString(R.string.edit_title_append));
                bodyEditText.setText(comment.getBody());
                break;

        }
        switch (type) {

            case TYPE_ISSUE:
                titleBuilder.append(getString(R.string.issue_title_append));

                if (action.equals(ACTION_EDIT)) {

                    titleEditText.setText(((Issue) comment).getTitle());

                }

                break;

            case TYPE_COMMENT:
                titleBuilder.append(getString(R.string.comment_title_append));
                titleInputLayout.setVisibility(View.GONE);
                break;

        }

        getSupportActionBar().setTitle(titleBuilder.toString());

        submitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (titleInputLayout.getVisibility() == View.VISIBLE) {

                    if (TextUtils.isEmpty(titleEditText.getText().toString())) {

                        titleInputLayout.setError(getString(R.string.issue_title_error_text));

                    }
                    else {

                        submitComment(action, type);

                    }

                }
                else {

                    submitComment(action, type);

                }

            }

        });

        titleEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() != 0) {

                    titleInputLayout.setError(null);

                }

            }

        });
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String token = preferences.getString(getString(R.string.oauth_token_key), null);

        service = ServiceGenerator.createService(token);

    }

    private void submitComment(String action, String type) {

        switch (action) {

            case ACTION_ADD:

                switch (type) {

                    case TYPE_ISSUE:

                        String[] repoNameSplit = repositoryName.split("/");
                        IssueAddEditRequest issueRequest = new IssueAddEditRequest();
                        issueRequest.setTitle(titleEditText.getText().toString());
                        issueRequest.setBody(bodyEditText.getText().toString());
                        service.addIssue(repoNameSplit[0], repoNameSplit[1], issueRequest)
                                .enqueue(new Callback<Issue>() {

                                    @Override
                                    public void onResponse(Call<Issue> call,
                                                           Response<Issue> response) {

                                        if (response.code() == 401) {

                                            Gson gson = new Gson();
                                            APIRequestError error = null;
                                            try {
                                                error = gson.fromJson(response.errorBody().string(),
                                                        APIRequestError.class);
                                            }
                                            catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            if (error.getMessage().equals(getString(
                                                    R.string.bad_credentials_error))) {

                                                new Builder(EditCommentActivity.this).setTitle(
                                                        R.string.login_credentials_expired_title)
                                                        .setMessage(
                                                                R.string.expired_credentials_message)
                                                        .setPositiveButton(R.string.ok_button_text,
                                                                new DialogInterface
                                                                        .OnClickListener() {

                                                                    @Override
                                                                    public void onClick(
                                                                            DialogInterface dialog,
                                                                            int which) {

                                                                        SharedPreferences
                                                                                preferences =
                                                                                PreferenceManager
                                                                                        .getDefaultSharedPreferences(
                                                                                                EditCommentActivity.this);
                                                                        Editor editor =
                                                                                preferences.edit();
                                                                        editor.putString(getString(
                                                                                R.string.oauth_token_key),
                                                                                null);
                                                                        editor.apply();
                                                                        FirebaseAuth.getInstance()
                                                                                .signOut();

                                                                        Intent logoutIntent =
                                                                                new Intent(
                                                                                        EditCommentActivity.this,
                                                                                        LoginActivity.class);
                                                                        logoutIntent.addFlags(
                                                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        dialog.dismiss();
                                                                        EditCommentActivity.this
                                                                                .startActivity(
                                                                                        logoutIntent);

                                                                    }

                                                                }).create().show();

                                            }

                                        }
                                        else {

                                            Toast.makeText(EditCommentActivity.this,
                                                    R.string.new_issue_submitted_toast,
                                                    Toast.LENGTH_LONG).show();
                                            finish();

                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<Issue> call, Throwable t) {

                                    }

                                });
                        break;

                    case TYPE_COMMENT:

                        repoNameSplit = repositoryName.split("/");
                        CommentAddEditRequest commentRequest = new CommentAddEditRequest();
                        commentRequest.setBody(bodyEditText.getText().toString());
                        service.addComment(repoNameSplit[0], repoNameSplit[1], issueNumber,
                                commentRequest).enqueue(new Callback<Issue>() {

                            @Override
                            public void onResponse(Call<Issue> call, Response<Issue> response) {

                                if (response.code() == 401) {

                                    Gson gson = new Gson();
                                    APIRequestError error = null;
                                    try {
                                        error = gson.fromJson(response.errorBody().string(),
                                                APIRequestError.class);
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if (error.getMessage()
                                            .equals(getString(R.string.bad_credentials_error))) {

                                        new Builder(EditCommentActivity.this)
                                                .setTitle(R.string.login_credentials_expired_title)
                                                .setMessage(R.string.expired_credentials_message)
                                                .setPositiveButton(R.string.ok_button_text,
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {

                                                                SharedPreferences preferences =
                                                                        PreferenceManager
                                                                                .getDefaultSharedPreferences(
                                                                                        EditCommentActivity.this);
                                                                Editor editor = preferences.edit();
                                                                editor.putString(getString(
                                                                        R.string.oauth_token_key),
                                                                        null);
                                                                editor.apply();
                                                                FirebaseAuth.getInstance()
                                                                        .signOut();

                                                                Intent logoutIntent = new Intent(
                                                                        EditCommentActivity.this,
                                                                        LoginActivity.class);
                                                                logoutIntent.addFlags(
                                                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                dialog.dismiss();
                                                                EditCommentActivity.this
                                                                        .startActivity(
                                                                                logoutIntent);

                                                            }

                                                        }).create().show();

                                    }

                                }
                                else {

                                    Toast.makeText(EditCommentActivity.this,
                                            R.string.new_comment_toast, Toast.LENGTH_LONG).show();
                                    finish();

                                }

                            }

                            @Override
                            public void onFailure(Call<Issue> call, Throwable t) {

                            }

                        });
                        break;

                }

            case ACTION_EDIT:
                switch (type) {

                    case TYPE_ISSUE:

                        String[] repoNameSplit = repositoryName.split("/");
                        IssueAddEditRequest issueRequest = new IssueAddEditRequest();
                        issueRequest.setTitle(titleEditText.getText().toString());
                        issueRequest.setBody(bodyEditText.getText().toString());
                        service.editIssue(repoNameSplit[0], repoNameSplit[1], issueNumber,
                                issueRequest).enqueue(new Callback<Issue>() {

                            @Override
                            public void onResponse(Call<Issue> call, Response<Issue> response) {

                                if (response.code() == 401) {

                                    Gson gson = new Gson();
                                    APIRequestError error = null;
                                    try {
                                        error = gson.fromJson(response.errorBody().string(),
                                                APIRequestError.class);
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if (error.getMessage()
                                            .equals(getString(R.string.bad_credentials_error))) {

                                        new Builder(EditCommentActivity.this)
                                                .setTitle(R.string.login_credentials_expired_title)
                                                .setMessage(R.string.expired_credentials_message)
                                                .setPositiveButton(R.string.ok_button_text,
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {

                                                                SharedPreferences preferences =
                                                                        PreferenceManager
                                                                                .getDefaultSharedPreferences(
                                                                                        EditCommentActivity.this);
                                                                Editor editor = preferences.edit();
                                                                editor.putString(getString(
                                                                        R.string.oauth_token_key),
                                                                        null);
                                                                editor.apply();
                                                                FirebaseAuth.getInstance()
                                                                        .signOut();

                                                                Intent logoutIntent = new Intent(
                                                                        EditCommentActivity.this,
                                                                        LoginActivity.class);
                                                                logoutIntent.addFlags(
                                                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                dialog.dismiss();
                                                                EditCommentActivity.this
                                                                        .startActivity(
                                                                                logoutIntent);

                                                            }

                                                        }).create().show();

                                    }

                                }
                                else {

                                    Toast.makeText(EditCommentActivity.this,
                                            R.string.issue_change_toast, Toast.LENGTH_LONG).show();
                                    finish();

                                }

                            }

                            @Override
                            public void onFailure(Call<Issue> call, Throwable t) {

                            }
                        });

                        break;

                    case TYPE_COMMENT:

                        repoNameSplit = repositoryName.split("/");
                        CommentAddEditRequest commentRequest = new CommentAddEditRequest();
                        commentRequest.setBody(bodyEditText.getText().toString());
                        service.editComment(repoNameSplit[0], repoNameSplit[1], commentId,
                                commentRequest).enqueue(new Callback<Issue>() {

                            @Override
                            public void onResponse(Call<Issue> call, Response<Issue> response) {

                                if (response.code() == 401) {

                                    Gson gson = new Gson();
                                    APIRequestError error = null;
                                    try {
                                        error = gson.fromJson(response.errorBody().string(),
                                                APIRequestError.class);
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if (error.getMessage()
                                            .equals(getString(R.string.bad_credentials_error))) {

                                        new Builder(EditCommentActivity.this)
                                                .setTitle(R.string.login_credentials_expired_title)
                                                .setMessage(R.string.expired_credentials_message)
                                                .setPositiveButton(R.string.ok_button_text,
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {

                                                                SharedPreferences preferences =
                                                                        PreferenceManager
                                                                                .getDefaultSharedPreferences(
                                                                                        EditCommentActivity.this);
                                                                Editor editor = preferences.edit();
                                                                editor.putString(getString(
                                                                        R.string.oauth_token_key),
                                                                        null);
                                                                editor.apply();
                                                                FirebaseAuth.getInstance()
                                                                        .signOut();

                                                                Intent logoutIntent = new Intent(
                                                                        EditCommentActivity.this,
                                                                        LoginActivity.class);
                                                                logoutIntent.addFlags(
                                                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                dialog.dismiss();
                                                                EditCommentActivity.this
                                                                        .startActivity(
                                                                                logoutIntent);

                                                            }

                                                        }).create().show();

                                    }

                                }
                                else {

                                    Toast.makeText(EditCommentActivity.this,
                                            R.string.comment_change_toast, Toast.LENGTH_LONG)
                                            .show();
                                    finish();

                                }

                            }

                            @Override
                            public void onFailure(Call<Issue> call, Throwable t) {

                            }
                        });

                        break;
                }

        }

    }

    @Override
    public void onBackPressed() {

        new Builder(this).setTitle(R.string.discard_changes_title)
                .setPositiveButton(R.string.discard_button_text,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                finish();

                            }

                        }).setNegativeButton(R.string.cancel_button_text,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();

                    }
                }).create().show();

    }

}
