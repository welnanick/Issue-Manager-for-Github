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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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
    public static final String TWO_FACTOR_VISIBLE_KEY = "two_factor_visible";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString(getString(R.string.oauth_token_key), null) != null) {

            Intent pinnedRepositoryIntent = new Intent(this, MainActivity.class);
            startActivity(pinnedRepositoryIntent);
            finish();

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {

            if (savedInstanceState.getBoolean(TWO_FACTOR_VISIBLE_KEY)) {

                twoFactorInputLayout.setVisibility(View.VISIBLE);

            }

        }

        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                authorizeUser();

            }

        });
        passwordEditText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO) {

                    authorizeUser();
                    return true;

                }
                return false;

            }

        });
        twoFactorEditText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO) {

                    authorizeUser();
                    return true;

                }
                return false;

            }

        });
        mAuth = FirebaseAuth.getInstance();

    }

    private void authorizeUser() {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (imm != null && imm.isAcceptingText()) {

            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }

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

                                if (responseUnsuccessful.getMessage()
                                        .equals(getString(R.string.bad_credentials_error))) {

                                    Toast.makeText(LoginActivity.this,
                                            R.string.bad_credentials_toast, Toast.LENGTH_LONG)
                                            .show();

                                }
                                else {

                                    Toast.makeText(LoginActivity.this, R.string.two_factor_toast,
                                            Toast.LENGTH_LONG).show();
                                    twoFactorInputLayout.setVisibility(View.VISIBLE);
                                    passwordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<AuthorizationResponse> call, Throwable t) {

                            group.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, R.string.network_error_toast,
                                    Toast.LENGTH_LONG).show();

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
            headers.put(getString(R.string.two_factor_header),
                    twoFactorEditText.getText().toString());
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

                                if (responseUnsuccessful.getMessage()
                                        .equals(getString(R.string.bad_credentials_error))) {

                                    Toast.makeText(LoginActivity.this,
                                            R.string.bad_credentials_toast, Toast.LENGTH_LONG)
                                            .show();

                                }
                                else {

                                    Toast.makeText(LoginActivity.this,
                                            R.string.two_factor_code_incorrect_toast,
                                            Toast.LENGTH_LONG).show();
                                    twoFactorInputLayout.setVisibility(View.VISIBLE);

                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<AuthorizationResponse> call, Throwable t) {

                            group.setVisibility(View.VISIBLE);
                            twoFactorInputLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, R.string.network_error_toast,
                                    Toast.LENGTH_LONG).show();

                        }
                    });

        }

    }

    private void login(AuthorizationResponse body) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = preferences.edit();
        editor.putString(getString(R.string.oauth_token_key), body.getToken());
        editor.apply();

        AuthCredential credential = GithubAuthProvider.getCredential(body.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    R.string.firebase_authentication_failed_toast,
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putBoolean(TWO_FACTOR_VISIBLE_KEY,
                twoFactorInputLayout.getVisibility() == View.VISIBLE);

    }
}
