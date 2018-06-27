package com.nickwelna.issuemanagerforgithub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        final String action = extras.getString("action");
        final String type = extras.getString("type");
        IssueCommentCommon comment = extras.getParcelable("comment");
        repositoryName = extras.getString("repo_name");
        issueNumber = extras.getInt("issue_number");
        commentId = extras.getInt("comment_id");
        StringBuilder titleBuilder = new StringBuilder();
        switch (action) {

            case "add":
                titleBuilder.append("Add ");
                break;

            case "edit":
                titleBuilder.append("Edit ");
                bodyEditText.setText(comment.getBody());
                break;

        }
        switch (type) {

            case "issue":
                titleBuilder.append("Issue");

                if (action.equals("edit")) {

                    titleEditText.setText(((Issue) comment).getTitle());

                }

                break;

            case "comment":
                titleBuilder.append("Comment");
                titleInputLayout.setVisibility(View.GONE);
                break;

        }

        getSupportActionBar().setTitle(titleBuilder.toString());

        submitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (titleInputLayout.getVisibility() == View.VISIBLE) {

                    if (TextUtils.isEmpty(titleEditText.getText().toString())) {

                        titleInputLayout.setError("You must enter a title");

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

        String token = preferences.getString("OAuth_token", null);

        service = ServiceGenerator.createService(token);

    }

    private void submitComment(String action, String type) {

        switch (action) {

            case "add":

                switch (type) {

                    case "issue":

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

                                            if (error.getMessage().equals("Bad credentials")) {

                                                new AlertDialog.Builder(EditCommentActivity.this)
                                                        .setTitle("Login Credentials Expired")
                                                        .setMessage("Your login credentials have " +
                                                                "expired, please log in " + "again")
                                                        .setPositiveButton("Ok",
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
                                                                        editor.putString(
                                                                                "OAuth_token",
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
                                                    "New Issue submitted", Toast.LENGTH_LONG)
                                                    .show();
                                            finish();

                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<Issue> call, Throwable t) {

                                    }

                                });
                        break;

                    case "comment":

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

                                    if (error.getMessage().equals("Bad credentials")) {

                                        new AlertDialog.Builder(EditCommentActivity.this)
                                                .setTitle("Login Credentials Expired").setMessage(
                                                "Your login credentials have " +
                                                        "expired, please log in " + "again")
                                                .setPositiveButton("Ok",
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
                                                                editor.putString("OAuth_token",
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
                                            "New Comment " + "submitted", Toast.LENGTH_LONG).show();
                                    finish();

                                }

                            }

                            @Override
                            public void onFailure(Call<Issue> call, Throwable t) {

                            }

                        });
                        break;

                }

            case "edit":
                switch (type) {

                    case "issue":

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

                                    if (error.getMessage().equals("Bad credentials")) {

                                        new AlertDialog.Builder(EditCommentActivity.this)
                                                .setTitle("Login Credentials Expired").setMessage(
                                                "Your login credentials have " +
                                                        "expired, please log in " + "again")
                                                .setPositiveButton("Ok",
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
                                                                editor.putString("OAuth_token",
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
                                            "Issue changes submitted", Toast.LENGTH_LONG).show();
                                    finish();

                                }

                            }

                            @Override
                            public void onFailure(Call<Issue> call, Throwable t) {

                            }
                        });

                        break;

                    case "comment":

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

                                    if (error.getMessage().equals("Bad credentials")) {

                                        new AlertDialog.Builder(EditCommentActivity.this)
                                                .setTitle("Login Credentials Expired").setMessage(
                                                "Your login credentials have " +
                                                        "expired, please log in " + "again")
                                                .setPositiveButton("Ok",
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
                                                                editor.putString("OAuth_token",
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
                                            "Comment changes submitted", Toast.LENGTH_LONG).show();
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

        new AlertDialog.Builder(this).setTitle("Discard Changes?")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        finish();

                    }

                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        }).create().show();

    }

}
