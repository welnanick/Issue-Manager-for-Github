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
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;

import butterknife.BindView;
import butterknife.ButterKnife;

class PinnedIssueAdapter extends RecyclerView.Adapter<PinnedIssueViewHolder> {

    PinnedIssueMenuItem[] pinnedIssues;
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

        return pinnedIssues[position].viewType;

    }

    @Override
    public void onBindViewHolder(@NonNull PinnedIssueViewHolder holder, int position) {

        if (pinnedIssues != null && pinnedIssues.length > 0) {

            holder.bind(pinnedIssues[position], user);

        }

    }

    @Override
    public int getItemCount() {

        if (pinnedIssues != null) {

            return pinnedIssues.length;

        }
        return 0;

    }

    public void updatePinnedRepositories(PinnedIssueMenuItem[] items) {

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

        void bind(final PinnedIssueMenuItem item, GithubUser user) {

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
                            viewRepoIntent.putExtras(extras);
                            context.startActivity(viewRepoIntent);

                        }

                    });
                    break;

                default:
                    text.setText(item.text);
                    subText.setText(item.subText);

                    itemView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent viewIssueDetailsIntent =
                                    new Intent(context, IssueDetailsActivity.class);

                            Gson gson = new Gson();
                            Issue issue = gson.fromJson("{\"url\":\"https://api.github" +
                                            ".com/repos/JakeWharton/butterknife/issues/1294\"," +
                                            "\"repository_url\":\"https://api.github" +
                                            ".com/repos/JakeWharton/butterknife\"," +
                                            "\"labels_url\":\"https://api.github" +
                                            ".com/repos/JakeWharton/butterknife/issues/1294/labels{/name}\",\"comments_url\":\"https://api.github.com/repos/JakeWharton/butterknife/issues/1294/comments\",\"events_url\":\"https://api.github.com/repos/JakeWharton/butterknife/issues/1294/events\",\"html_url\":\"https://github.com/JakeWharton/butterknife/pull/1294\",\"id\":334163428,\"node_id\":\"MDExOlB1bGxSZXF1ZXN0MTk2MjA2MDUw\",\"number\":1294,\"title\":\"Enable task caching for R2Generator.\",\"user\":{\"login\":\"runningcode\",\"id\":332597,\"node_id\":\"MDQ6VXNlcjMzMjU5Nw==\",\"avatar_url\":\"https://avatars3.githubusercontent.com/u/332597?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/runningcode\",\"html_url\":\"https://github.com/runningcode\",\"followers_url\":\"https://api.github.com/users/runningcode/followers\",\"following_url\":\"https://api.github.com/users/runningcode/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/runningcode/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/runningcode/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/runningcode/subscriptions\",\"organizations_url\":\"https://api.github.com/users/runningcode/orgs\",\"repos_url\":\"https://api.github.com/users/runningcode/repos\",\"events_url\":\"https://api.github.com/users/runningcode/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/runningcode/received_events\",\"type\":\"User\",\"site_admin\":false},\"labels\":[],\"state\":\"open\",\"locked\":false,\"assignee\":null,\"assignees\":[],\"milestone\":null,\"comments\":3,\"created_at\":\"2018-06-20T16:43:51Z\",\"updated_at\":\"2018-06-21T03:27:07Z\",\"closed_at\":null,\"author_association\":\"CONTRIBUTOR\",\"pull_request\":{\"url\":\"https://api.github.com/repos/JakeWharton/butterknife/pulls/1294\",\"html_url\":\"https://github.com/JakeWharton/butterknife/pull/1294\",\"diff_url\":\"https://github.com/JakeWharton/butterknife/pull/1294.diff\",\"patch_url\":\"https://github.com/JakeWharton/butterknife/pull/1294.patch\"},\"body\":\"This enables gradle task caching for the R2 Generating task.\\r\\n\\r\\nSome more info on task caching:\\r\\nhttps://github.com/gradle/task-output-cache-demos/blob/master/docs/making-custom-tasks-cacheable.md\"}",
                                    Issue.class);
                            Bundle extras = new Bundle();
                            extras.putParcelable("Issue", issue);
                            extras.putBoolean("from-pinned", true);
                            extras.putString("repo-name", "JakeWharton/butterknife");
                            viewIssueDetailsIntent.putExtras(extras);
                            context.startActivity(viewIssueDetailsIntent);

                        }

                    });

                    break;
            }

        }

    }

}
