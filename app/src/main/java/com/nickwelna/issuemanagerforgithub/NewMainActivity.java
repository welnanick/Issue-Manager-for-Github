package com.nickwelna.issuemanagerforgithub;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.common.flogger.FluentLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.Repository;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NewMainActivity extends AppCompatActivity {

    public static final String REPOSITORY_NAME = "repository_name";
    public static final String CURRENT_ISSUE = "current_issue";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    AppBarConfiguration appBarConfiguration;
    NavigationView navView;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    @Nullable private DatabaseReference userDataReference;
    @Nullable private GitHubService service;
    @Nullable private GithubUser user;
    @Nullable private NavigationHelper navigationHelper;
    private List<Repository> pinnedRepositories = new ArrayList<>();
    private Map<String, List<Integer>> pinnedIssueMenuItems = new HashMap<>();
    private NavController navController;
    private View headerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.pinnedRepositories, R.id.githubLogin)
                        .setDrawerLayout(drawerLayout)
                        .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
        headerLayout = navView.getHeaderView(0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration);
    }

    public void updateUserDataReference() {
        userDataReference = database.getReference("users").child(auth.getCurrentUser().getUid());
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    @Nullable
    public GitHubService getService() {
        return service;
    }

    public void setService(GitHubService service) {
        this.service = service;
    }

    @Nullable
    public GithubUser getUser() {
        return user;
    }

    public void loadUser() {
        if (service == null) {
            logger.atWarning().log("Service is null");
            return;
        }
        service.getAuthorizedUser().enqueue(new GetUserCallback());
    }

    public void removePinnedIssue(String repositoryName, int issue) {
        int removeIndex = -1;
        @Nullable List<Integer> pinnedIssues = pinnedIssueMenuItems.get(repositoryName);
        if (pinnedIssues == null) {
            return;
        }
        for (int i = 0; i < pinnedIssues.size(); i++) {
            if (pinnedIssues.get(i).equals(issue)) {
                removeIndex = i;
            }
        }
        if (removeIndex == -1) {
            return;
        }
        pinnedIssues.remove(removeIndex);
        if (userDataReference == null) {
            logger.atWarning().log("userDataReference is null");
            return;
        }
        userDataReference.child("pinned_issues").child(repositoryName).setValue(pinnedIssues);
        Toast.makeText(this, R.string.issue_unpinned_toast, Toast.LENGTH_LONG).show();
        loadPinnedIssues();

    }

    public void addPinnedIssue(String repositoryName, int number) {
        @Nullable List<Integer> pinnedIssues = pinnedIssueMenuItems.get(repositoryName);
        if (pinnedIssues == null) {
            pinnedIssues = new ArrayList<>();
        }
        pinnedIssues.add(number);
        Collections.sort(pinnedIssues);
        if (userDataReference == null) {
            logger.atWarning().log("userDataReference is null");
            return;
        }
        userDataReference.child("pinned_issues").child(repositoryName).setValue(pinnedIssues);
        Toast.makeText(this, R.string.issue_pinned_toast, Toast.LENGTH_LONG).show();
        loadPinnedIssues();

    }

    private void logout(DialogInterface dialog) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(this.getString(R.string.oauth_token_key),
                null);
        editor.apply();
        FirebaseAuth.getInstance().signOut();
        dialog.dismiss();
        navController.navigate(R.id.githubLogin);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.atInfo().log("onCreateOptionsMenu called");
        if (navigationHelper != null) {
            navigationHelper.inflateOptionsMenu(menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navigationHelper == null) {
            return false;
        }
        return navigationHelper.onOptionsItemSelected(item);
    }

    public void setNavigationHelper(NavigationHelper navigationHelper) {
        this.navigationHelper = navigationHelper;
    }

    public void removePinnedRepository(String repositoryName) {
        int removeIndex = -1;
        for (int i = 0; i < pinnedRepositories.size(); i++) {
            if (pinnedRepositories.get(i).getFullName().equals(repositoryName)) {
                removeIndex = i;
            }
        }
        if (removeIndex == -1) {
            return;
        }
        pinnedRepositories.remove(removeIndex);
        if (userDataReference == null) {
            logger.atWarning().log("userDataReference is null");
            return;
        }
        userDataReference.child("pinned_repos").setValue(convertToStringList(pinnedRepositories));
        invalidateOptionsMenu();
        Toast.makeText(this, R.string.repository_unpinned_toast, Toast.LENGTH_LONG).show();
    }

    public void addPinnedRepository(String repositoryName) {
        Repository repository = new Repository();
        repository.setFullName(repositoryName);
        pinnedRepositories.add(repository);
        List<String> repositoryStrings = convertToStringList(pinnedRepositories);
        if (userDataReference == null) {
            logger.atWarning().log("userDataReference is null");
            return;
        }
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

    private List<String> convertToStringList(List<Repository> pinnedRepositories) {
        List<String> repoStrings = new ArrayList<>();
        for (Repository repo : pinnedRepositories) {
            repoStrings.add(repo.getFullName());
        }
        return repoStrings;
    }

    public void loadPinnedRepositories() {
        logger.atInfo().log("loadPinnedRepositories() called");
        if (userDataReference == null) {
            logger.atWarning().log("userDataReference is null");
            return;
        }
        DatabaseReference pinnedRepos = userDataReference.child("pinned_repos");
        pinnedRepos.addListenerForSingleValueEvent(new RepositoryValueEventListener());
    }

    public void loadPinnedIssues() {

        pinnedIssueMenuItems = new HashMap<>();

        if (userDataReference == null) {
            logger.atWarning().log("userDataReference is null");
            return;
        }
        DatabaseReference pinnedIssuesRef = userDataReference.child("pinned_issues");
        ValueEventListener pinnedIssueListener = new IssueValueEventListener();
        pinnedIssuesRef.addListenerForSingleValueEvent(pinnedIssueListener);

    }

    public List<Repository> getPinnedRepositories() {
        return pinnedRepositories;
    }

    public Map<String, List<Integer>> getPinnedIssues() {
        return pinnedIssueMenuItems;
    }

    private static class GetIssueCallback implements Callback<Issue> {

        View issueView;

        GetIssueCallback(View issueView) {
            this.issueView = issueView;
        }

        @Override
        public void onResponse(Call<Issue> call, Response<Issue> response) {
            logger.atInfo().log("LoadIssueCallback onResponse() called");
            if (response.code() == 401) {
                ResponseBody errorBody = response.errorBody();
                @Nullable APIRequestError error = null;
                try {
                    String errorBodyJson = "";
                    if (errorBody != null) {
                        errorBodyJson = errorBody.string();
                    }
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<APIRequestError> jsonAdapter = moshi
                            .adapter(APIRequestError.class);
                    error = jsonAdapter.fromJson(errorBodyJson);
                } catch (IOException e) {
                    logger.atSevere().withCause(e).log("Error Body string() failed");
                }
                if (error != null) {
                    logger.atSevere().log(error.getMessage());
                }
            } else {
                Issue issue = response.body();
                ((TextView) issueView.findViewById(R.id.issue_title)).setText(issue.getTitle());
            }
        }

        @Override
        public void onFailure(Call<Issue> call, Throwable t) {

        }
    }

    private class GetUserCallback implements Callback<GithubUser> {
        @Override
        public void onResponse(@NonNull Call<GithubUser> call,
                               @NonNull Response<GithubUser> response) {
            if (response.code() == 401) {
                ResponseBody errorBody = response.errorBody();
                @Nullable APIRequestError error = null;
                try {
                    String errorBodyJson = "";
                    if (errorBody != null) {
                        errorBodyJson = errorBody.string();
                    }
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<APIRequestError> jsonAdapter = moshi
                            .adapter(APIRequestError.class);
                    error = jsonAdapter.fromJson(errorBodyJson);
                } catch (IOException e) {
                    logger.atSevere().withCause(e).log("Error Body string() failed");
                }
                if (error != null) {
                    logger.atSevere().log(error.getMessage());
                }
            } else {
                user = response.body();
                TextView username = headerLayout.findViewById(R.id.username);
                ImageView avatar = headerLayout.findViewById(R.id.avatar);
                Button logoutButton = headerLayout.findViewById(R.id.logout_text);
                if (user == null) {
                    return;
                }
                username.setText(user.getLogin());
                Glide.with(headerLayout).load(user.getAvatarUrl())
                     .apply(RequestOptions.circleCropTransform()).into(avatar);
                logoutButton.setOnClickListener(v -> new AlertDialog.Builder(NewMainActivity.this)
                        .setTitle(R.string.Logout_title)
                        .setNegativeButton(R.string.no_button_text,
                                (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(R.string.yes_button_text,
                                (dialog, which) -> logout(dialog))
                        .create()
                        .show());
            }
        }

        @Override
        public void onFailure(@NonNull Call<GithubUser> call, @NonNull Throwable t) {
            Toast.makeText(NewMainActivity.this, R.string.network_error_toast, Toast.LENGTH_LONG)
                 .show();
        }
    }

    private class RepositoryValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            logger.atInfo().log("RepositoryValueEventListener onDataChange() called");
            pinnedRepositories = new ArrayList<>();
            for (DataSnapshot pinnedRepoSnapshot : dataSnapshot.getChildren()) {
                Repository temp = new Repository();
                temp.setFullName(pinnedRepoSnapshot.getValue(String.class));
                pinnedRepositories.add(temp);
            }
            if (navigationHelper == null) {
                return;
            }
            navigationHelper.updateProviderData();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logger.atInfo().log("RepositoryValueEventListener onCancelled() called");
            Toast.makeText(NewMainActivity.this, R.string.network_error_toast, Toast.LENGTH_LONG)
                 .show();
        }

    }

    private class IssueValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Menu navMenu = navView.getMenu();
            navMenu.clear();
            //owners of all repos with pinned issues
            for (DataSnapshot pinnedIssueSnapshot : dataSnapshot.getChildren()) {
                String owner = pinnedIssueSnapshot.getKey();
                //repositories that have pinned issues
                for (DataSnapshot ownerRepositories : pinnedIssueSnapshot
                        .getChildren()) {
                    String repositoryName = ownerRepositories.getKey();
                    String fullName = owner + "/" + repositoryName;
                    MenuItem repoMenuItem = navView.getMenu().add(null);
                    View repoView = getLayoutInflater()
                            .inflate(R.layout.issue_repo_list_item, navView, false);
                    ((TextView) repoView.findViewById(R.id.repository_name)).setText(fullName);
                    repoMenuItem.setActionView(repoView);
                    repoMenuItem.setEnabled(false);
                    List<Integer> pinnedIssues = new ArrayList<>();
                    pinnedIssueMenuItems.put(fullName, pinnedIssues);
                    //issues pinned
                    for (DataSnapshot issue : ownerRepositories.getChildren()) {
                        Integer issueNumber = issue.getValue(Integer.class);
                        pinnedIssues.add(issueNumber);
                        MenuItem issueMenuItem = navView.getMenu().add(null);
                        View issueView = getLayoutInflater()
                                .inflate(R.layout.pinned_issue_list_item, navView, false);
                        if(service == null) {
                            continue;
                        }
                        service.getIssue(owner, repositoryName, issueNumber)
                               .enqueue(new GetIssueCallback(issueView));
                        ((TextView) issueView.findViewById(R.id.issue_number))
                                .setText(getString(R.string.issue_number_format, issueNumber));
                        issueMenuItem.setActionView(issueView);
                        issueMenuItem.setOnMenuItemClickListener(item -> {
                            Bundle bundle = new Bundle();
                            bundle.putString(NewMainActivity.REPOSITORY_NAME, fullName);
                            bundle.putInt(NewMainActivity.CURRENT_ISSUE, issueNumber);
                            navController.navigate(R.id.issueDetails, bundle);
                            drawerLayout.closeDrawer(GravityCompat.START);
                            return true;
                        });
                    }
                }
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }
}
