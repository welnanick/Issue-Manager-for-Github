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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nickwelna.issuemanagerforgithub.CommentAdapter.CommentViewHolder;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;

import butterknife.BindView;
import butterknife.ButterKnife;

class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

    IssueCommentCommon[] comments;

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.comment_list_item, parent, false);
        return new CommentViewHolder(itemView, parent.getContext(), "JakeWharton");
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        if (comments != null && comments.length > 0) {

            holder.bind(comments[position]);

        }

    }

    @Override
    public int getItemCount() {

        if (comments != null) {

            return comments.length;

        }
        return 0;
    }

    public void updateComments(IssueCommentCommon[] comments) {

        this.comments = comments;
        notifyDataSetChanged();

    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.howlong)
        TextView howLong;
        @BindView(R.id.body)
        TextView body;
        @BindView(R.id.avatar)
        ImageView avatar;
        @BindView(R.id.edit)
        ImageView edit;
        View itemView;
        Context context;
        String usernameString;

        CommentViewHolder(View itemView, Context context, String username) {

            super(itemView);
            this.context = context;
            this.itemView = itemView;
            this.usernameString = username;
            ButterKnife.bind(this, itemView);

        }

        void bind(final IssueCommentCommon comment) {

            username.setText(comment.getUser().getLogin());
            howLong.setText(getHowLong(comment));
            body.setText(comment.getBody());
            Glide.with(itemView).load(comment.getUser().getAvatar_url())
                 .apply(RequestOptions.circleCropTransform()).into(avatar);

            if (!comment.getUser().getLogin().equals(usernameString)) {

                edit.setVisibility(View.GONE);

            }
            else {

                edit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent addCommentIntent = new Intent(context, EditComment.class);
                        Bundle extras = new Bundle();
                        extras.putString("action", "edit");
                        if (comment instanceof Issue) {

                            extras.putString("type", "issue");

                        }
                        else {

                            extras.putString("type", "comment");

                        }
                        extras.putParcelable("comment", comment);
                        addCommentIntent.putExtras(extras);
                        context.startActivity(addCommentIntent);

                    }

                });

            }

        }

        private String getHowLong(IssueCommentCommon comment) {

            return "12 days";

        }

    }

}
