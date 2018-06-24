package com.nickwelna.issuemanagerforgithub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.nickwelna.issuemanagerforgithub.models.Issue;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IssueListActivity extends AppCompatActivity {

    @BindView(R.id.issue_recycler_view)
    RecyclerView issue_recycler_view;
    @BindView(R.id.add_issue)
    FloatingActionButton addIssue;
    @BindView(R.id.loading_progress)
    ProgressBar loadingProgress;
    IssueAdapter issueAdapter;
    boolean firstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_list);
        Bundle extras = getIntent().getExtras();
        String repositoryName = extras.getString("repository");
        getSupportActionBar().setTitle(repositoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        issue_recycler_view.setLayoutManager(linearLayoutManager);
        issueAdapter = new IssueAdapter(repositoryName);
        issue_recycler_view.setAdapter(issueAdapter);

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {

            public void run() {

                Gson gson = new Gson();
                String test = getString(R.string.dummy_issue_data);
                Issue[] results = gson.fromJson(test, Issue[].class);
                issueAdapter.updateIssues(results);
                loadingProgress.setVisibility(View.GONE);
                addIssue.show();
                firstRun = false;

            }

        };
        handler.postDelayed(r, 5000);

        addIssue.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent addCommentIntent = new Intent(IssueListActivity.this, EditComment.class);
                Bundle extras = new Bundle();
                extras.putString("action", "add");
                extras.putString("type", "issue");
                addCommentIntent.putExtras(extras);
                startActivity(addCommentIntent);

            }

        });

    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!addIssue.isShown() && !firstRun) {

            addIssue.show();

        }

    }

    @Override
    protected void onPause() {

        super.onPause();
        addIssue.hide();

    }

}
