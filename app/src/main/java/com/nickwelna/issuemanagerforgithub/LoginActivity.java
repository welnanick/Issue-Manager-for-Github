package com.nickwelna.issuemanagerforgithub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.AuthorizationRequest;
import com.nickwelna.issuemanagerforgithub.models.AuthorizationResponse;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username_edit_text)
    TextInputEditText usernameEditText;
    @BindView(R.id.password_edit_text)
    TextInputEditText passwordEditText;
    @BindView(R.id.two_factor_edit_text)
    TextInputEditText twoFactorEditText;
    @BindView(R.id.two_factor_input_layout)
    TextInputLayout twoFactorInputLayout;
    @BindView(R.id.login_button)
    Button loginButton;
    @BindView(R.id.group)
    Group group;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString("OAuth_token", null) != null) {

            Intent pinnedRepositoryIntent = new Intent(this, MainActivity.class);
            startActivity(pinnedRepositoryIntent);
            finish();

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                authorizeUser();

            }

        });
        mAuth = FirebaseAuth.getInstance();

    }

    private void authorizeUser() {

        if (twoFactorInputLayout.getVisibility() == View.GONE) {

            group.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            GitHubService service = ServiceGenerator
                    .createService(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
            Map<String, String> headers = new HashMap<>();
            service.authorizeUser(headers, new AuthorizationRequest())
                    .enqueue(new Callback<AuthorizationResponse>() {

                        @Override
                        public void onResponse(Call<AuthorizationResponse> call,
                                               Response<AuthorizationResponse> response) {

                            if (response.code() == 201) {

                                AuthorizationResponse body = response.body();
                                login(body);

                            }
                            else if (response.code() == 401) {

                                group.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);

                                Gson gson = new Gson();
                                APIRequestError responseUnsuccessful = null;
                                try {
                                    responseUnsuccessful =
                                            gson.fromJson(response.errorBody().string(),
                                                    APIRequestError.class);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (responseUnsuccessful.getMessage().equals("Bad credentials")) {

                                    Toast.makeText(LoginActivity.this,
                                            "Bad Credentials, please try again", Toast.LENGTH_LONG)
                                            .show();

                                }
                                else {

                                    Toast.makeText(LoginActivity.this,
                                            "Two Factor Authentication required", Toast.LENGTH_LONG)
                                            .show();
                                    twoFactorInputLayout.setVisibility(View.VISIBLE);

                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<AuthorizationResponse> call, Throwable t) {

                        }

                    });

        }
        else {

            group.setVisibility(View.GONE);
            twoFactorInputLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            GitHubService service = ServiceGenerator
                    .createService(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Github-OTP", twoFactorEditText.getText().toString());
            service.authorizeUser(headers, new AuthorizationRequest())
                    .enqueue(new Callback<AuthorizationResponse>() {

                        @Override
                        public void onResponse(Call<AuthorizationResponse> call,
                                               Response<AuthorizationResponse> response) {

                            if (response.code() == 201) {

                                AuthorizationResponse body = response.body();
                                login(body);

                            }
                            else if (response.code() == 401) {

                                group.setVisibility(View.VISIBLE);
                                twoFactorInputLayout.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                Gson gson = new Gson();
                                APIRequestError responseUnsuccessful = null;
                                try {
                                    responseUnsuccessful =
                                            gson.fromJson(response.errorBody().string(),
                                                    APIRequestError.class);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (responseUnsuccessful.getMessage().equals("Bad Credentials")) {

                                    Toast.makeText(LoginActivity.this,
                                            "Bad Credentials, please try again", Toast.LENGTH_LONG)
                                            .show();

                                }
                                else {

                                    Toast.makeText(LoginActivity.this,
                                            "Two Factor Authentication code incorrect",
                                            Toast.LENGTH_LONG).show();
                                    twoFactorInputLayout.setVisibility(View.VISIBLE);

                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<AuthorizationResponse> call, Throwable t) {

                        }
                    });

        }

    }

    private void login(AuthorizationResponse body) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = preferences.edit();
        editor.putString("OAuth_token", body.getToken());
        editor.apply();

        AuthCredential credential = GithubAuthProvider.getCredential(body.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Firebase Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {

                            Intent pinnedRepositoryIntent =
                                    new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(pinnedRepositoryIntent);
                            finish();

                        }
                    }
                });

    }

}
