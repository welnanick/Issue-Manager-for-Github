package com.nickwelna.issuemanagerforgithub;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class PinnedRepositoryWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        UpdateWidgetDataService.startActionUpdateRepos(context);

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views =
                new RemoteViews(context.getPackageName(), R.layout.pinned_repository_widget);

        Intent intent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.widget_list_view, intent);

        views.setEmptyView(R.id.widget_list_view, R.id.empty_view);

        Intent refreshIntent = new Intent(context, UpdateWidgetDataService.class);
        refreshIntent.setAction(UpdateWidgetDataService.ACTION_UPDATE_REPOS);
        PendingIntent updateIntent = PendingIntent
                .getService(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.refresh, updateIntent);

        Intent startActivityIntent = new Intent(context, IssueListActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent
                .getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_view, startActivityPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                        int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId);

        }

    }

}

