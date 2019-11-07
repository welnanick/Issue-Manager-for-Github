package com.nickwelna.issuemanagerforgithub.networking;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public final class ServiceGenerator {

    private static final String API_BASE_URL = "https://api.github.com";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder = new Retrofit.Builder().baseUrl(API_BASE_URL)
                                                                    .addConverterFactory(
                                                                            MoshiConverterFactory
                                                                                    .create());
    //            .addConverterFactory(GsonConverterFactory.create());
    private static Retrofit retrofit = builder.build();

    public static GitHubService createService() {
        return createService(null, null);
    }

    public static GitHubService createService(@Nullable String username, @Nullable String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(username, password);
            return createService(authToken);
        }
        return createService(null);
    }

    public static GitHubService createService(@Nullable final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(authToken);
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);
                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }
        return retrofit.create(GitHubService.class);
    }
}
