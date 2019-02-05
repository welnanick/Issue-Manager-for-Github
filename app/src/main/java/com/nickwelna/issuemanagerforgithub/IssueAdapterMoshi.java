package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nickwelna.issuemanagerforgithub.IssueAdapterMoshi.IssueViewHolderMoshi;
import com.nickwelna.issuemanagerforgithub.models.IssueMoshi;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class IssueAdapterMoshi extends RecyclerView.Adapter<IssueViewHolderMoshi> {
    private List<IssueMoshi> issues = new ArrayList<>();
    private String repositoryName;

    IssueAdapterMoshi(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    @NonNull
    @Override
    public IssueViewHolderMoshi onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.issue_list_item, parent, false);
        return new IssueViewHolderMoshi(itemView, parent.getContext(), repositoryName);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolderMoshi holder, int position) {
        holder.bind(issues.get(position));
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }

    void updateIssues(List<IssueMoshi> issues) {
        this.issues = issues;
        notifyDataSetChanged();
    }

    static final class IssueViewHolderMoshi extends RecyclerView.ViewHolder {
        @BindView(R.id.issue_title)
        TextView issueTitle;
        @BindView(R.id.issue_number)
        TextView issueNumber;
        @BindView(R.id.issue_comments)
        TextView issueComments;
        @BindView(R.id.issue_status)
        TextView issueStatus;

        Context context;
        String repositoryName;

        IssueViewHolderMoshi(@NonNull View itemView, Context context, String repositoryName) {
            super(itemView);
            this.context = context;
            this.repositoryName = repositoryName;
            ButterKnife.bind(this, itemView);
        }

        public void bind(IssueMoshi issue) {
            issueTitle.setText(issue.getTitle());
            issueNumber.setText(context.getString(R.string.issue_number_format, issue.getNumber()));
            issueComments.setText(context.getResources()
                                         .getQuantityString(R.plurals.comments_format,
                                                            issue.getComments(),
                                                            issue.getComments()));
            String issueState = issue.getState();
            String issueStateCap = issueState.substring(0, 1).toUpperCase() + issueState
                    .substring(1);
            issueStatus.setText(issueStateCap);
            if (issueState.equals(context.getString(R.string.open_state_text))) {
                issueStatus.setTextColor(ContextCompat.getColor(context, R.color.material_green));
            }
            else {
                issueStatus.setTextColor(ContextCompat.getColor(context, R.color.material_red));
            }
            Bundle bundle = new Bundle();
            bundle.putString(NewMainActivity.REPOSITORY_NAME, repositoryName);
            bundle.putInt(NewMainActivity.CURRENT_ISSUE, issue.getNumber());
            itemView.setOnClickListener(Navigation.createNavigateOnClickListener(
                    R.id.action_repositoryDetails_to_issueDetails, bundle));
        }
    }
}
