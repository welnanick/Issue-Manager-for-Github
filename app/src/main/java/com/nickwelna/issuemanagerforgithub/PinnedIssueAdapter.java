package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.PinnedIssueAdapter.PinnedIssueViewHolder;
import com.nickwelna.issuemanagerforgithub.models.Issue;

import butterknife.BindView;
import butterknife.ButterKnife;

class PinnedIssueAdapter extends RecyclerView.Adapter<PinnedIssueViewHolder> {

    PinnedIssueMenuItem[] pinnedIssues;

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

            holder.bind(pinnedIssues[position]);

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
        @BindView(R.id.avatar)
        ImageView avatar;
        View itemView;
        int viewType;
        Context context;

        PinnedIssueViewHolder(View itemView, int viewType, Context context) {

            super(itemView);
            this.itemView = itemView;
            this.viewType = viewType;
            this.context = context;
            ButterKnife.bind(this, itemView);

        }

        void bind(PinnedIssueMenuItem item) {

            switch (viewType) {

                case 0:
                    headerView.setText(item.text);
                    Glide.with(itemView)
                         .load("https://avatars3.githubusercontent.com/u/10217506?v=4")
                         .apply(RequestOptions.circleCropTransform()).into(avatar);
                    break;

                case 1:
                    text.setText(item.text);
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
