package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nickwelna.issuemanagerforgithub.IssueAdapter.IssueViewHolder;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IssueAdapter extends RecyclerView.Adapter<IssueViewHolder> {

    Issue[] issues;
    String repositoryName;
    GithubUser user;

    public IssueAdapter(String repositoryName, GithubUser user) {

        this.repositoryName = repositoryName;
        this.user = user;

    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.issue_list_item, parent, false);
        return new IssueViewHolder(itemView, parent.getContext(), repositoryName, user);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {

        if (issues != null && issues.length > 0) {

            holder.bind(issues[position]);

        }

    }

    @Override
    public int getItemCount() {

        if (issues != null) {

            return issues.length;

        }
        return 0;

    }

    public void updateIssues(Issue[] issues) {

        this.issues = issues;
        notifyDataSetChanged();

    }

    static class IssueViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.issue_title)
        TextView issueTitle;
        @BindView(R.id.issue_number)
        TextView issueNumber;
        @BindView(R.id.issue_comments)
        TextView issueComments;
        @BindView(R.id.issue_status)
        TextView issueStatus;
        View itemView;
        Context context;
        String repositoryName;
        GithubUser user;

        IssueViewHolder(View itemView, Context context, String repositoryName, GithubUser user) {

            super(itemView);
            this.itemView = itemView;
            this.context = context;
            this.repositoryName = repositoryName;
            this.user = user;
            ButterKnife.bind(this, itemView);

        }

        void bind(final Issue issue) {

            issueTitle.setText(issue.getTitle());
            issueNumber.setText('#' + String.valueOf(issue.getNumber()));
            if (issue.getComments() != 1) {

                issueComments.setText(issue.getComments() + " comments");

            }
            else {

                issueComments.setText("1 comment");

            }
            String issueState = issue.getState();
            String issueStateCap =
                    issueState.substring(0, 1).toUpperCase() + issueState.substring(1);
            issueStatus.setText(issueStateCap);
            if (issueState.equals("open")) {

                issueStatus.setTextColor(context.getResources().getColor(R.color.material_green));

            }
            else {

                issueStatus.setTextColor(context.getResources().getColor(R.color.material_red));

            }

            itemView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent viewIssueDetailsIntent = new Intent(context, IssueDetailsActivity.class);
                    Bundle extras = new Bundle();
                    extras.putParcelable("Issue", issue);
                    extras.putString("repo-name", repositoryName);
                    extras.putParcelable("user", user);
                    viewIssueDetailsIntent.putExtras(extras);
                    context.startActivity(viewIssueDetailsIntent);

                }

            });

        }

    }

}
