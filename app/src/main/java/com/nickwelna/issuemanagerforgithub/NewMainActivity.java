package com.nickwelna.issuemanagerforgithub;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.flogger.FluentLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nickwelna.issuemanagerforgithub.models.APIRequestErrorMoshi;
import com.nickwelna.issuemanagerforgithub.models.GithubUserMoshi;
import com.nickwelna.issuemanagerforgithub.models.RepositoryMoshi;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewMainActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference userDataReference;
    private GitHubService service;
    private GithubUserMoshi user;
    private OptionsMenuProvider menuProvider;
    private List<RepositoryMoshi> pinnedRepositories = new ArrayList<>();
    private List<Integer> pinnedIssues = new ArrayList<>();

    public static final String REPOSITORY_NAME = "repository_name";
    public static final String CURRENT_ISSUE = "current_issue";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        ButterKnife.bind(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }

    public void updateUserDataReference() {
        userDataReference = database.getReference("users").child(auth.getCurrentUser().getUid());
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public void setService(GitHubService service) {
        this.service = service;
    }

    public GitHubService getService() {
        return service;
    }

    public GithubUserMoshi getUser() {
        return user;
    }

    public void loadUser() {
        service.getAuthorizedUserMoshi().enqueue(new GetUserCallback());
    }

    private class GetUserCallback implements Callback<GithubUserMoshi> {
        @Override
        public void onResponse(@NonNull Call<GithubUserMoshi> call,
                               @NonNull Response<GithubUserMoshi> response) {
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
                }
                catch (IOException e) {
                    logger.atSevere().withCause(e).log("Error Body string() failed");
                }
                if (error != null) {
                    logger.atSevere().log(error.getMessage());
                }
            }
            else {
                user = response.body();
            }
        }

        @Override
        public void onFailure(@NonNull Call<GithubUserMoshi> call, @NonNull Throwable t) {
            Toast.makeText(NewMainActivity.this, R.string.network_error_toast, Toast.LENGTH_LONG)
                 .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.atInfo().log("onCreateOptionsMenu called");
        menuProvider.inflateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return menuProvider.onOptionsItemSelected(item);
    }

    public void setMenuProvider(OptionsMenuProvider menuProvider) {
        this.menuProvider = menuProvider;
    }

    public void removePinnedRepository(String repositoryName) {
        RepositoryMoshi toRemove = null;
        for (RepositoryMoshi repository : pinnedRepositories) {
            if (repository.getFullName().equals(repositoryName)) {
                toRemove = repository;
            }
        }
        if (toRemove == null) {
            return;
        }
        pinnedRepositories.remove(toRemove);
        userDataReference.child("pinned_repos").setValue(convertToStringList(pinnedRepositories));
        invalidateOptionsMenu();
        Toast.makeText(this, R.string.repository_unpinned_toast, Toast.LENGTH_LONG).show();
    }

    public void addPinnedRepository(String repositoryName) {
        RepositoryMoshi repository = new RepositoryMoshi();
        repository.setFullName(repositoryName);
        pinnedRepositories.add(repository);
        List<String> repositoryStrings = convertToStringList(pinnedRepositories);
        userDataReference.child("pinned_repos").setValue(repositoryStrings);
        invalidateOptionsMenu();
        Toast.makeText(this, R.string.repository_pinned_toast, Toast.LENGTH_LONG).show();
    }

    public void showFab() {
        fab.show();
    }

    public void hideFab() {
        fab.hide();
    }

    public void setFabClick(View.OnClickListener listener) {
        fab.setOnClickListener(listener);
    }

    private List<String> convertToStringList(List<RepositoryMoshi> pinnedRepositories) {
        List<String> repoStrings = new ArrayList<>();
        for (RepositoryMoshi repo : pinnedRepositories) {
            repoStrings.add(repo.getFullName());
        }
        return repoStrings;
    }

    public void loadPinnedRepositories() {
        logger.atInfo().log("loadPinnedRepositories() called");
        DatabaseReference pinnedRepos = userDataReference.child("pinned_repos");
        pinnedRepos.addListenerForSingleValueEvent(new RepositoryValueEventListener());
    }

    public void loadPinnedIssues() {

        pinnedIssues = new ArrayList<>();

        DatabaseReference pinnedIssuesRef = userDataReference.child("pinned_issues");
        ValueEventListener pinnedIssueListener = new IssueValueEventListener();
        pinnedIssuesRef.addListenerForSingleValueEvent(pinnedIssueListener);

    }

    private class RepositoryValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            logger.atInfo().log("RepositoryValueEventListener onDataChange() called");
            pinnedRepositories = new ArrayList<>();
            for (DataSnapshot pinnedRepoSnapshot : dataSnapshot.getChildren()) {
                RepositoryMoshi temp = new RepositoryMoshi();
                temp.setFullName(pinnedRepoSnapshot.getValue(String.class));
                pinnedRepositories.add(temp);
            }
            menuProvider.updateProviderData();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logger.atInfo().log("RepositoryValueEventListener onCancelled() called");
            Toast.makeText(NewMainActivity.this, R.string.network_error_toast, Toast.LENGTH_LONG)
                 .show();
        }

    }

    public List<RepositoryMoshi> getPinnedRepositories() {
        return pinnedRepositories;
    }

    public List<Integer> getPinnedIssues() {
        return pinnedIssues;
    }

    private class IssueValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //owners of all repos with pinned issues
            for (DataSnapshot pinnedIssueSnapshot : dataSnapshot.getChildren()) {
                //repositories that have pinned issues
                for (DataSnapshot ownerRepositories : pinnedIssueSnapshot.getChildren()) {
                    //issues pinned
                    for (DataSnapshot issue : ownerRepositories.getChildren()) {
                        pinnedIssues.add(issue.getValue(Integer.class));
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }
}
