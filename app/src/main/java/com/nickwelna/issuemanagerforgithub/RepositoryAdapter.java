package com.nickwelna.issuemanagerforgithub;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nickwelna.issuemanagerforgithub.RepositoryAdapter.RepositoryViewHolder;
import com.nickwelna.issuemanagerforgithub.models.Repository;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryViewHolder> {

    Repository[] repositories;

    @NonNull
    @Override
    public RepositoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.repository_list_item, parent, false);
        return new RepositoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RepositoryViewHolder holder, int position) {

        if (repositories != null && repositories.length > 0) {

            holder.bind(repositories[position]);

        }

    }

    @Override
    public int getItemCount() {

        if (repositories != null) {

            return repositories.length;

        }
        return 0;

    }

    public void updateSearchResults(Repository[] repositories) {

        this.repositories = repositories;
        notifyDataSetChanged();

    }

    static class RepositoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.repository_name)
        TextView repositoryName;

        RepositoryViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        void bind(Repository repository) {

            repositoryName.setText(repository.getFull_name());

        }

    }

}
