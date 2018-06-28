package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.CommentAdapter.CommentViewHolder;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.Comment;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.nickwelna.issuemanagerforgithub.networking.ServiceGenerator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;

class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

    IssueCommentCommon[] comments;
    GithubUser user;
    String repositoryName;

    public CommentAdapter(String repositoryName, GithubUser user) {

        this.repositoryName = repositoryName;
        this.user = user;

    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.comment_list_item, parent, false);
        return new CommentViewHolder(itemView, parent.getContext(), user.getLogin(),
                repositoryName);
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
        @BindView(R.id.how_long)
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
        String repositoryName;

        CommentViewHolder(View itemView, Context context, String username, String repositoryName) {

            super(itemView);
            this.context = context;
            this.itemView = itemView;
            this.usernameString = username;
            this.repositoryName = repositoryName;
            ButterKnife.bind(this, itemView);

        }

        void bind(final IssueCommentCommon comment) {

            username.setText(comment.getUser().getLogin());
            howLong.setText(getHowLong(comment));
            body.setText(comment.getBody());
            Glide.with(itemView).load(comment.getUser().getAvatar_url())
                    .apply(RequestOptions.circleCropTransform()).into(avatar);
            avatar.setContentDescription(
                    context.getString(R.string.avatar_image_content_description,
                            comment.getUser().getLogin()));

            if (!comment.getUser().getLogin().equals(usernameString)) {

                edit.setVisibility(View.GONE);

            }
            else {

                edit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent addCommentIntent = new Intent(context, EditCommentActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(EditCommentActivity.ACTION_KEY,
                                EditCommentActivity.ACTION_EDIT);
                        extras.putString(EditCommentActivity.REPO_NAME_KEY, repositoryName);
                        if (comment instanceof Issue) {

                            extras.putString(EditCommentActivity.TYPE_KEY,
                                    EditCommentActivity.TYPE_ISSUE);
                            extras.putInt(EditCommentActivity.ISSUE_NUMBER_KEY,
                                    ((Issue) comment).getNumber());

                        }
                        else {

                            extras.putString(EditCommentActivity.TYPE_KEY,
                                    EditCommentActivity.TYPE_COMMENT);
                            extras.putInt(EditCommentActivity.COMMENT_ID_KEY, comment.getId());

                        }
                        extras.putParcelable(EditCommentActivity.COMMENT_KEY, comment);
                        addCommentIntent.putExtras(extras);
                        context.startActivity(addCommentIntent);

                    }

                });
                if (comment instanceof Comment) {
                    itemView.setLongClickable(true);
                    itemView.setOnLongClickListener(new OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {

                            new Builder(context).setTitle(R.string.delete_comment_title)
                                    .setPositiveButton(

                                            R.string.yes_button_text,
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(final DialogInterface dialog,
                                                                    int which) {

                                                    SharedPreferences preferences =
                                                            PreferenceManager
                                                                    .getDefaultSharedPreferences(
                                                                            context);
                                                    String token = preferences.getString(
                                                            context.getString(
                                                                    R.string.oauth_token_key),
                                                            null);

                                                    GitHubService service =
                                                            ServiceGenerator.createService(token);
                                                    String[] repoNameSplit =
                                                            repositoryName.split("/");
                                                    service.deleteComment(repoNameSplit[0],
                                                            repoNameSplit[1], comment.getId())
                                                            .enqueue(

                                                                    new Callback<Issue>() {

                                                                        @Override
                                                                        public void onResponse(
                                                                                Call<Issue> call,
                                                                                Response<Issue>
                                                                                        response) {

                                                                            if (response.code() ==
                                                                                    401) {

                                                                                Gson gson =
                                                                                        new Gson();
                                                                                APIRequestError
                                                                                        error =
                                                                                        null;
                                                                                try {
                                                                                    error =
                                                                                            gson.fromJson(
                                                                                                    response.errorBody()
                                                                                                            .string(),
                                                                                                    APIRequestError.class);
                                                                                }
                                                                                catch (IOException e) {
                                                                                    e.printStackTrace();
                                                                                }

                                                                                if (error
                                                                                        .getMessage()
                                                                                        .equals(context
                                                                                                .getString(
                                                                                                        R.string.bad_credentials_error))) {

                                                                                    new Builder(
                                                                                            context)
                                                                                            .setTitle(
                                                                                                    R.string.login_credentials_expired_title)
                                                                                            .setMessage(
                                                                                                    R.string.expired_credentials_message)
                                                                                            .setPositiveButton(
                                                                                                    context.getString(
                                                                                                            R.string.ok_button_text),
                                                                                                    new DialogInterface.OnClickListener() {

                                                                                                        @Override
                                                                                                        public void onClick(
                                                                                                                DialogInterface dialog,
                                                                                                                int which) {

                                                                                                            SharedPreferences
                                                                                                                    preferences =
                                                                                                                    PreferenceManager
                                                                                                                            .getDefaultSharedPreferences(
                                                                                                                                    context);
                                                                                                            Editor
                                                                                                                    editor =
                                                                                                                    preferences
                                                                                                                            .edit();
                                                                                                            editor.putString(
                                                                                                                    context.getString(
                                                                                                                            R.string.oauth_token_key),
                                                                                                                    null);
                                                                                                            editor.apply();
                                                                                                            FirebaseAuth
                                                                                                                    .getInstance()
                                                                                                                    .signOut();

                                                                                                            Intent
                                                                                                                    logoutIntent =
                                                                                                                    new Intent(
                                                                                                                            context,
                                                                                                                            LoginActivity.class);
                                                                                                            logoutIntent
                                                                                                                    .addFlags(
                                                                                                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                            dialog.dismiss();
                                                                                                            context.startActivity(
                                                                                                                    logoutIntent);

                                                                                                        }

                                                                                                    })
                                                                                            .create()
                                                                                            .show();

                                                                                }

                                                                            }
                                                                            else {

                                                                                Toast.makeText(
                                                                                        context,
                                                                                        R.string.comment_deleted_toast,

                                                                                        Toast.LENGTH_LONG)
                                                                                        .show();
                                                                                dialog.dismiss();
                                                                                ((IssueDetailsActivity) context)
                                                                                        .refreshIssue();

                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(
                                                                                Call<Issue> call,
                                                                                Throwable t) {

                                                                            Toast.makeText(context,
                                                                                    R.string.network_error_toast,
                                                                                    Toast.LENGTH_LONG)
                                                                                    .show();

                                                                        }
                                                                    });

                                                }

                                            }).setNegativeButton(R.string.no_button_text,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();

                                        }
                                    }).create().show();
                            return true;
                        }

                    });
                }

            }

        }

        private String getHowLong(IssueCommentCommon comment) {

            String datePosted = comment.getCreatedAt();
            SimpleDateFormat format = new SimpleDateFormat(context.getString(R.string.date_format));
            format.setTimeZone(TimeZone.getTimeZone(context.getString(R.string.utc_timezonr)));
            Date date = null;
            try {
                date = format.parse(datePosted);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
            String result = (String) DateUtils
                    .getRelativeTimeSpanString(date.getTime(), new Date().getTime(), 0,
                            FORMAT_ABBREV_MONTH);

            if (!result.contains(context.getString(R.string.ago_string)) &&
                    !result.contains(context.getString(

                            R.string.in_string)) &&
                    !result.equals(context.getString(R.string.yesterday_string))) {

                return context.getString(R.string.how_long_format, result);

            }
            else {

                if (result.equals(context.getString(R.string.yesterday_string))) {

                    result = result.toLowerCase();

                }

                return result;

            }

        }

    }

}
