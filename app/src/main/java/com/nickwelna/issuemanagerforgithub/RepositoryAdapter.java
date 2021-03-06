package com.nickwelna.issuemanagerforgithub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nickwelna.issuemanagerforgithub.models.Repository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public final class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder> {
    private List<Repository> repositoryList = new ArrayList<>();

    @NonNull
    @Override
    public RepositoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.repository_list_item, parent, false);
        return new RepositoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RepositoryViewHolder holder, int position) {
        holder.bind(repositoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return repositoryList.size();
    }

    void updateRepositories(@NonNull List<Repository> newRepositories) {
        repositoryList = newRepositories;
        notifyDataSetChanged();
    }

    static final class RepositoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.repository_name)
        TextView repositoryName;

        RepositoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(@NonNull Repository repository) {
            String repositoryNameString = repository.getFullName();
            repositoryName.setText(repositoryNameString);
            Bundle bundle = new Bundle();
            bundle.putString(NewMainActivity.REPOSITORY_NAME, repositoryNameString);
            itemView.setOnClickListener(Navigation.createNavigateOnClickListener(
                    R.id.action_pinnedRepositories_to_repositoryDetails, bundle));
        }
    }
}
