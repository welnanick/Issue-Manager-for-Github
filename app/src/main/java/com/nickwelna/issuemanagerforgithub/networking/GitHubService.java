package com.nickwelna.issuemanagerforgithub.networking;

import com.nickwelna.issuemanagerforgithub.models.AuthorizationRequest;
import com.nickwelna.issuemanagerforgithub.models.AuthorizationResponse;
import com.nickwelna.issuemanagerforgithub.models.Comment;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueAddEditRequest;
import com.nickwelna.issuemanagerforgithub.models.SearchResult;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GitHubService {

    @POST("/authorizations")
    Call<AuthorizationResponse> authorizeUser(@HeaderMap Map<String, String> headers,
                                              @Body AuthorizationRequest request);

    @GET("/user")
    Call<GithubUser> getAuthorizedUser();

    @GET("/search/repositories")
    Call<SearchResult> searchRepositories(@Query("q") String search);

    @GET("/repos/{owner}/{repository}/issues")
    Call<Issue[]> getIssues(@Path("owner") String owner, @Path("repository") String repository,
                            @Query("state") String state);

    @GET("/repos/{owner}/{repository}/issues/{issue}/comments")
    Call<Comment[]> getComments(@Path("owner") String owner, @Path("repository") String repository,
                                @Path("issue") int issue);

    @POST("/repos/{owner}/{repository}/issues")
    Call<Issue> addIssue(@Path("owner") String owner, @Path("repository") String repository,
                         @Body IssueAddEditRequest request);

}
