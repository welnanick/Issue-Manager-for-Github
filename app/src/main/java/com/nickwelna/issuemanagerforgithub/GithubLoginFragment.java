package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.flogger.FluentLogger;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GithubAuthProvider;
import com.nickwelna.issuemanagerforgithub.models.APIRequestErrorMoshi;
import com.nickwelna.issuemanagerforgithub.models.AuthorizationRequestMoshi;
import com.nickwelna.issuemanagerforgithub.models.AuthorizationResponseMoshi;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class GithubLoginFragment extends Fragment {

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

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private NewMainActivity activity;
    private final OnEditorActionListener authorizeAction = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            authorizeUser();
            return true;
        }
        return false;
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logger.atInfo().log("onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_github_login, container, false);
        ButterKnife.bind(this, view);
        loginButton.setOnClickListener((button) -> authorizeUser());
        passwordEditText.setOnEditorActionListener(authorizeAction);
        twoFactorEditText.setOnEditorActionListener(authorizeAction);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        logger.atInfo().log("onAttach() called");
        super.onAttach(context);
        this.activity = (NewMainActivity) context;
    }

    private void authorizeUser() {
        logger.atInfo().log("authorizeUser() called");
        String userNameText = usernameEditText.getText().toString();
        String passwordText = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(userNameText) || TextUtils.isEmpty(passwordText)) {
            Toast.makeText(activity, "Blank Username/Password", Toast.LENGTH_LONG).show();
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
        activity.setService(ServiceGenerator.createService(userNameText, passwordText));
        Map<String, String> headers = new HashMap<>();
        boolean twoFactorVisible = twoFactorInputLayout.getVisibility() == View.VISIBLE;
        if (twoFactorVisible) {
            twoFactorInputLayout.setVisibility(View.GONE);
            headers.put(getString(R.string.two_factor_header),
                        twoFactorEditText.getText().toString());
        }
        group.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<AuthorizationRequestMoshi> jsonAdapter = moshi
                .adapter(AuthorizationRequestMoshi.class);
        String test = jsonAdapter.toJson(new AuthorizationRequestMoshi());
        logger.atInfo().log(test);
        activity.getService().authorizeUserMoshi(headers, new AuthorizationRequestMoshi())
                .enqueue(new LoginCallback(twoFactorVisible));

    }

    private void login(@NonNull AuthorizationResponseMoshi body) {
        logger.atInfo().log("login() called");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.oauth_token_key), body.getToken());
        editor.apply();

        AuthCredential credential = GithubAuthProvider.getCredential(body.getToken());
        activity.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(activity, R.string.firebase_authentication_failed_toast,
                                       Toast.LENGTH_SHORT).show();
                    }
                    else {
                        activity.updateUserDataReference();
                        activity.loadUser();
                        Navigation.findNavController(activity, R.id.nav_host_fragment)
                                  .popBackStack();
                    }
                });
    }

    private class LoginCallback implements Callback<AuthorizationResponseMoshi> {

        private boolean twoFactorVisible;

        LoginCallback(boolean twoFactorVisible) {
            this.twoFactorVisible = twoFactorVisible;
        }

        @Override
        public void onResponse(@NonNull Call<AuthorizationResponseMoshi> call,
                               @NonNull Response<AuthorizationResponseMoshi> response) {
            logger.atInfo().log("LoginCallback onResponse");
            switch (response.code()) {
                case 201:
                    logger.atInfo().log("201 Received");
                    AuthorizationResponseMoshi body = response.body();
                    if (body != null) {
                        login(body);
                    }
                    else {
                        logger.atWarning().log("Response was 201, but response.body() was null");
                    }
                    break;
                case 401:
                    logger.atInfo().log("401 Received");
                    group.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    if (twoFactorVisible) {
                        twoFactorInputLayout.setVisibility(View.VISIBLE);
                    }
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
                        if (error.getMessage().equals(getString(R.string.bad_credentials_error))) {
                            Toast.makeText(activity, R.string.bad_credentials_toast,
                                           Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getContext(), R.string.two_factor_toast,
                                           Toast.LENGTH_LONG).show();
                            twoFactorInputLayout.setVisibility(View.VISIBLE);
                            passwordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            logger.atInfo().log("Github says this user needs 2fa to login");
                        }
                    }
                    else {
                        Toast.makeText(activity, "Something went very wrong", Toast.LENGTH_LONG)
                             .show();
                    }
                    break;
                default:
                    logger.atInfo().log("Unknown Response code received: %d", response.code());
                    break;
            }
        }

        @Override
        public void onFailure(@NonNull Call<AuthorizationResponseMoshi> call,
                              @NonNull Throwable t) {
            group.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();
            logger.atInfo().withCause(t).log("Login Request Failed");
        }
    }
}
