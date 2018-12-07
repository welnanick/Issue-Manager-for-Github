package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class CreateEditIssueFragment extends Fragment implements OptionsMenuProvider {

    public static final String CREATE_ISSUE = "create_issue";
    private boolean createIssue;
    private NewMainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (NewMainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        createIssue = arguments.getBoolean(CREATE_ISSUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (createIssue) {
            this.activity.setTitle(R.string.create_issue_title);
        }
        else {
            this.activity.setTitle(R.string.edit_issue_title);

        }
        activity.setMenuProvider(this);
        activity.invalidateOptionsMenu();
        return inflater.inflate(R.layout.fragment_create_edit_issue, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void inflateOptionsMenu(Menu menu) {
        // Intentionally left blank
    }

    @Override
    public void updateProviderData() {

    }
}
