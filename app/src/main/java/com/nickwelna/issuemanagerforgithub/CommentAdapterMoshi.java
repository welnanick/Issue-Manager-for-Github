package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nickwelna.issuemanagerforgithub.CommentAdapterMoshi.CommentViewHolderMoshi;
import com.nickwelna.issuemanagerforgithub.models.GithubUserMoshi;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommonMoshi;
import com.nickwelna.issuemanagerforgithub.models.IssueMoshi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;

public class CommentAdapterMoshi extends RecyclerView.Adapter<CommentViewHolderMoshi> {

    private List<IssueCommentCommonMoshi> comments = new ArrayList<>();
    private String username;
    private String repositoryName;

    CommentAdapterMoshi(String username, String repositoryName) {
        this.username = username;
        this.repositoryName = repositoryName;
    }

    @NonNull
    @Override
    public CommentViewHolderMoshi onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.comment_list_item, parent, false);
        return new CommentViewHolderMoshi(itemView, parent.getContext(), username, repositoryName);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolderMoshi holder, int position) {
        holder.bind(comments.get(position));
    }

    void updateComments(@NonNull List<IssueCommentCommonMoshi> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static final class CommentViewHolderMoshi extends RecyclerView.ViewHolder {

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

        Context context;
        String usernameString;
        String repositoryName;

        CommentViewHolderMoshi(@NonNull View itemView, Context context, String username, String repositoryName) {
            super(itemView);
            this.context = context;
            this.usernameString = username;
            this.repositoryName = repositoryName;
            ButterKnife.bind(this, itemView);
        }

        public void bind(IssueCommentCommonMoshi comment) {
            GithubUserMoshi user = comment.getUser();
            String login = user.getLogin();
            username.setText(login);
            howLong.setText(getHowLong(comment));
            body.setText(comment.getBody());
            Glide.with(itemView).load(user.getAvatarUrl())
                 .apply(RequestOptions.circleCropTransform()).into(avatar);
            avatar.setContentDescription(
                    context.getString(R.string.avatar_image_content_description, login));
            if (!login.equals(usernameString)) {
                edit.setVisibility(View.GONE);
            }
            else {
                if (comment instanceof IssueMoshi) {
                    Bundle args = new Bundle();
                    args.putBoolean(CreateEditIssueFragment.CREATE_ISSUE, false);
                    args.putString(NewMainActivity.REPOSITORY_NAME, repositoryName);
                    args.putInt(CreateEditIssueFragment.ISSUE_NUMBER, ((IssueMoshi)comment).getNumber());
                    args.putString(CreateEditIssueFragment.ISSUE_NAME, ((IssueMoshi)comment).getTitle());
                    args.putString(CreateEditIssueFragment.ISSUE_BODY, comment.getBody());
                    edit.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_issueDetails_to_createEditIssue, args));
                }
            }
        }

        private String getHowLong(IssueCommentCommonMoshi comment) {
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
            if (!result.contains(context.getString(R.string.ago_string)) && !result
                    .contains(context.getString(R.string.in_string)) && !result
                    .equals(context.getString(R.string.yesterday_string))) {
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
