package com.nickwelna.issuemanagerforgithub.networking;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthenticationInterceptor implements Interceptor {

    private String authToken;

    public AuthenticationInterceptor(String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder;
        if (!authToken.contains("Basic")) {
            builder = original.newBuilder().header("Authorization", "token " + authToken);
        } else {
            builder = original.newBuilder().header("Authorization", authToken);
        }
        Request request = builder.build();
        return chain.proceed(request);
    }

}
