package com.nickwelna.issuemanagerforgithub;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class UpdateWidgetDataService extends IntentService {

    public static final String ACTION_UPDATE_REPOS =
            "com.nickwelna.issuemanagerforgithub.update_repos";

    public UpdateWidgetDataService() {

        super("UpdateWidgetDataService");

    }

    public static void startActionUpdateRepos(Context context) {

        Intent intent = new Intent(context, UpdateWidgetDataService.class);
        intent.setAction(ACTION_UPDATE_REPOS);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {

            String action = intent.getAction();

            if (action.equals(ACTION_UPDATE_REPOS)) {

                handleActionUpdateRepos();

            }

        }

    }

    private void handleActionUpdateRepos() {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager
                .getAppWidgetIds(new ComponentName(this, PinnedRepositoryWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        PinnedRepositoryWidget.updateAppWidgets(this, appWidgetManager, appWidgetIds);

    }

}
