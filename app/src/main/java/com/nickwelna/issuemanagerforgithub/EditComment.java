package com.nickwelna.issuemanagerforgithub;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.nickwelna.issuemanagerforgithub.models.Issue;
import com.nickwelna.issuemanagerforgithub.models.IssueCommentCommon;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditComment extends AppCompatActivity {

    @BindView(R.id.title_input_layout)
    TextInputLayout titleInputLayout;
    @BindView(R.id.title_edit_text)
    TextInputEditText titleEditText;
    @BindView(R.id.body_edit_text)
    TextInputEditText bodyEditText;
    @BindView(R.id.submit_button)
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        final String action = extras.getString("action");
        String type = extras.getString("type");
        IssueCommentCommon comment = extras.getParcelable("comment");
        StringBuilder titleBuilder = new StringBuilder();
        switch (action) {

            case "add":
                titleBuilder.append("Add ");
                break;

            case "edit":
                titleBuilder.append("Edit ");
                bodyEditText.setText(comment.getBody());
                break;

        }
        switch (type) {

            case "issue":
                titleBuilder.append("Issue");

                if (action.equals("edit")) {

                    titleEditText.setText(((Issue) comment).getTitle());

                }

                break;

            case "comment":
                titleBuilder.append("Comment");
                titleInputLayout.setVisibility(View.GONE);
                break;

        }

        getSupportActionBar().setTitle(titleBuilder.toString());

        submitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                submitComment(action);

            }

        });

    }

    private void submitComment(String action) {

        switch (action) {

            case "add":
                Toast.makeText(this, "New comment submitted", Toast.LENGTH_LONG).show();
                finish();
                break;

            case "edit":
                Toast.makeText(this, "Comment edit submitted", Toast.LENGTH_LONG).show();
                finish();
                break;

        }

    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this).setTitle("Discard Changes?").setPositiveButton("Discard",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        finish();

                    }

                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        }).create().show();

    }

}
