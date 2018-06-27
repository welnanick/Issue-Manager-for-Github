package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.PinnedIssueAdapter.PinnedIssueViewHolder;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class PinnedIssueAdapter extends RecyclerView.Adapter<PinnedIssueViewHolder> {

    List<PinnedIssueMenuItem> pinnedIssues;
    GithubUser user;

    public PinnedIssueAdapter(GithubUser user) {

        this.user = user;

    }

    @NonNull
    @Override
    public PinnedIssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case 0:
                itemView = inflater.inflate(R.layout.menu_header_layout, parent, false);
                break;

            case 1:
                itemView = inflater.inflate(R.layout.issue_repo_list_item, parent, false);
                break;

            default:
                itemView = inflater.inflate(R.layout.pinned_issue_list_item, parent, false);
                break;
        }
        return new PinnedIssueViewHolder(itemView, viewType, parent.getContext());

    }

    @Override
    public int getItemViewType(int position) {

        return pinnedIssues.get(position).viewType;

    }

    @Override
    public void onBindViewHolder(@NonNull PinnedIssueViewHolder holder, int position) {

        if (pinnedIssues != null && pinnedIssues.size() > 0) {

            holder.bind(pinnedIssues.get(position), user);

        }

    }

    @Override
    public int getItemCount() {

        if (pinnedIssues != null) {

            return pinnedIssues.size();

        }
        return 0;

    }

    public void updatePinnedRepositories(List<PinnedIssueMenuItem> items) {

        pinnedIssues = items;
        notifyDataSetChanged();

    }

    static class PinnedIssueViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.text)
        TextView text;
        @Nullable
        @BindView(R.id.sub_text)
        TextView subText;
        @Nullable
        @BindView(R.id.header_view)
        TextView headerView;
        @Nullable
        @BindView(R.id.logout_text)
        TextView logoutText;
        @Nullable
        @BindView(R.id.avatar)
        ImageView avatar;
        View itemView;
        int viewType;
        Context context;
        AlertDialog logoutAlert;

        PinnedIssueViewHolder(View itemView, int viewType, final Context context) {

            super(itemView);
            this.itemView = itemView;
            this.viewType = viewType;
            this.context = context;
            logoutAlert = new AlertDialog.Builder(context).setTitle("Logout?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }

                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            SharedPreferences preferences =
                                    PreferenceManager.getDefaultSharedPreferences(context);
                            Editor editor = preferences.edit();
                            editor.putString("OAuth_token", null);
                            editor.apply();
                            FirebaseAuth.getInstance().signOut();

                            Intent logoutIntent = new Intent(context, LoginActivity.class);
                            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            dialog.dismiss();
                            context.startActivity(logoutIntent);
                        }
                    }).create();
            ButterKnife.bind(this, itemView);

        }

        void bind(final PinnedIssueMenuItem item, final GithubUser user) {

            switch (viewType) {

                case 0:
                    headerView.setText(user.getLogin());
                    Glide.with(itemView).load(user.getAvatar_url())
                            .apply(RequestOptions.circleCropTransform()).into(avatar);
                    logoutText.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            logoutAlert.show();

                        }

                    });

                    break;

                case 1:
                    text.setText(item.text);
                    itemView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent viewRepoIntent = new Intent(context, IssueListActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("repository", item.text);
                            extras.putParcelable("user", user);
                            viewRepoIntent.putExtras(extras);
                            context.startActivity(viewRepoIntent);

                        }

                    });
                    break;

                default:

                    SharedPreferences preferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    String token = preferences.getString("OAuth_token", null);
                    GitHubService service = ServiceGenerator.createService(token);
                    String[] repoNameSplit = item.text.split("/");
                    service.getIssue(repoNameSplit[0], repoNameSplit[1], item.number)
                            .enqueue(new Callback<Issue>() {

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

                                            new Builder(context)
                                                    .setTitle("Login Credentials Expired")
                                                    .setMessage("Your login credentials have " +
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
                                                                                            context);
                                                                    Editor editor =
                                                                            preferences.edit();
                                                                    editor.putString("OAuth_token",
                                                                            null);
                                                                    editor.apply();
                                                                    FirebaseAuth.getInstance()
                                                                            .signOut();

                                                                    Intent logoutIntent =
                                                                            new Intent(context,
                                                                                    LoginActivity
                                                                                            .class);
                                                                    logoutIntent.addFlags(
                                                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    dialog.dismiss();
                                                                    context.startActivity(
                                                                            logoutIntent);

                                                                }

                                                            }).create().show();

                                        }

                                    }
                                    else {

                                        final Issue issue = response.body();
                                        text.setText(issue.getTitle());
                                        subText.setText("# " + issue.getNumber());
                                        itemView.setOnClickListener(new OnClickListener() {

                                            @Override
                                            public void onClick(View v) {

                                                Intent viewIssueDetailsIntent = new Intent(context,
                                                        IssueDetailsActivity.class);

                                                Bundle extras = new Bundle();
                                                extras.putParcelable("Issue", issue);
                                                extras.putBoolean("from-pinned", true);
                                                extras.putString("repo-name", item.text);
                                                extras.putParcelable("user", user);
                                                viewIssueDetailsIntent.putExtras(extras);
                                                context.startActivity(viewIssueDetailsIntent);

                                            }

                                        });

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

}
