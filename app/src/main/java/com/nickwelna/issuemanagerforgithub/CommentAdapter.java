package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.base.Splitter;
import com.google.common.flogger.FluentLogger;
import com.nickwelna.issuemanagerforgithub.CommentAdapter.CommentViewHolder;
import com.nickwelna.issuemanagerforgithub.models.APIRequestError;
import com.nickwelna.issuemanagerforgithub.models.GithubUser;
import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;
import com.nickwelna.issuemanagerforgithub.networking.GitHubService;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;

public final class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

    private final NewMainActivity activity;
    private final String repositoryName;
    private final IssueDetailsFragment issueDetailsFragment;
    private List<IssueCommentCommon> comments = new ArrayList<>();

    CommentAdapter(NewMainActivity activity, String repositoryName,
                   IssueDetailsFragment issueDetailsFragment) {
        this.activity = activity;
        this.repositoryName = repositoryName;
        this.issueDetailsFragment = issueDetailsFragment;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.comment_list_item, parent, false);
        return new CommentViewHolder(itemView, parent
                .getContext(), repositoryName, activity, issueDetailsFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    void updateComments(@NonNull List<IssueCommentCommon> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static final class CommentViewHolder extends RecyclerView.ViewHolder {

        private static final FluentLogger logger = FluentLogger.forEnclosingClass();
        final Context context;
        final String repositoryName;
        final NewMainActivity activity;
        final IssueDetailsFragment issueDetailsFragment;
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
        @Nullable
        String usernameString;

        CommentViewHolder(@NonNull View itemView, Context context,
                          String repositoryName, NewMainActivity activity,
                          IssueDetailsFragment issueDetailsFragment) {
            super(itemView);
            this.context = context;
            GithubUser user = activity.getUser();
            if (user != null) {
                this.usernameString = user.getLogin();
            }
            this.repositoryName = repositoryName;
            this.activity = activity;
            this.issueDetailsFragment = issueDetailsFragment;
            ButterKnife.bind(this, itemView);
        }

        void bind(IssueCommentCommon comment) {
            GithubUser user = comment.getUser();
            String login = user.getLogin();
            username.setText(login);
            howLong.setText(getHowLong(comment));
            body.setText(comment.getBody());
            Glide.with(itemView).load(user.getAvatarUrl())
                 .apply(RequestOptions.circleCropTransform()).into(avatar);
            avatar.setContentDescription(context
                    .getString(R.string.avatar_image_content_description, login));
            if (!login.equals(usernameString)) {
                edit.setVisibility(View.GONE);
            } else {
                if (comment instanceof Issue) {
                    Bundle args = new Bundle();
                    args.putBoolean(CreateEditIssueFragment.CREATE_ISSUE, false);
                    args.putString(NewMainActivity.REPOSITORY_NAME, repositoryName);
                    args.putInt(CreateEditIssueFragment.ISSUE_NUMBER, ((Issue) comment)
                            .getNumber());
                    args.putString(CreateEditIssueFragment.ISSUE_NAME, ((Issue) comment)
                            .getTitle());
                    args.putString(CreateEditIssueFragment.ISSUE_BODY, comment.getBody());
                    edit.setOnClickListener(Navigation
                            .createNavigateOnClickListener(R.id.action_issueDetails_to_createEditIssue, args));
                } else {
                    Bundle args = new Bundle();
                    args.putBoolean(CreateEditCommentFragment.CREATE_COMMENT, false);
                    args.putString(NewMainActivity.REPOSITORY_NAME, repositoryName);
                    args.putInt(CreateEditCommentFragment.COMMENT_ID, comment.getId());
                    args.putString(CreateEditCommentFragment.COMMENT_BODY, comment.getBody());
                    edit.setOnClickListener(Navigation
                            .createNavigateOnClickListener(R.id.action_issueDetails_to_createEditComment, args));
                    itemView.setOnLongClickListener(v -> {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.delete_comment_title)
                                .setPositiveButton(R.string.yes_button_text, (dialog, which) -> {
                                    List<String> repoNameSplit = Splitter.on('/')
                                                                         .splitToList(repositoryName);
                                    GitHubService service = activity.getService();
                                    if (service == null) {
                                        return;
                                    }
                                    service.deleteComment(
                                            repoNameSplit.get(0),
                                            repoNameSplit.get(1),
                                            comment.getId())
                                           .enqueue(new DeleteIssueCallback(dialog, issueDetailsFragment));
                                })
                                .setNegativeButton(
                                        R.string.no_button_text,
                                        (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                        return true;
                    });

                }
            }
        }

        private String getHowLong(IssueCommentCommon comment) {
            String datePosted = comment.getCreatedAt();
            SimpleDateFormat format = new SimpleDateFormat(context
                    .getString(R.string.date_format), Locale.US);
            format.setTimeZone(TimeZone.getTimeZone(context.getString(R.string.utc_timezonr)));
            @Nullable Date date = null;
            try {
                date = format.parse(datePosted);
            } catch (ParseException e) {
                logger.atSevere().withCause(e).log("Failed to parse date");
            }
            if (date == null) {
                return "";
            }
            String result = (String) DateUtils.getRelativeTimeSpanString(date.getTime(), new Date()
                    .getTime(), 0, FORMAT_ABBREV_MONTH);
            if (!result.contains(context.getString(R.string.ago_string)) && !result
                    .contains(context.getString(R.string.in_string)) && !result
                    .equals(context.getString(R.string.yesterday_string))) {
                return context.getString(R.string.how_long_format, result);
            } else {
                if (result.equals(context.getString(R.string.yesterday_string))) {
                    result = result.toLowerCase();
                }
                return result;
            }
        }

        final class DeleteIssueCallback implements Callback<Issue> {

            final DialogInterface dialog;
            final IssueDetailsFragment issueDetailsFragment;

            DeleteIssueCallback(DialogInterface dialog,
                                IssueDetailsFragment issueDetailsFragment) {
                this.dialog = dialog;
                this.issueDetailsFragment = issueDetailsFragment;
            }

            @Override
            public void onResponse(@NotNull Call<Issue> call, Response<Issue> response) {

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

                    Toast.makeText(activity, R.string.comment_deleted_toast, Toast.LENGTH_LONG)
                         .show();
                    dialog.dismiss();
                    issueDetailsFragment.loadComments();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Issue> call, @NotNull Throwable t) {

                Toast.makeText(activity, R.string.network_error_toast, Toast.LENGTH_LONG).show();

            }
        }
    }
}
