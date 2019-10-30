package com.nickwelna.issuemanagerforgithub;

import android.view.Menu;
import android.view.MenuItem;

public interface NavigationHelper {
    default void inflateOptionsMenu(Menu menu) {
        // Intentionally left blank
    }

    default void updateProviderData() {
        //Intentionally left blank
    }

    boolean onOptionsItemSelected(MenuItem item);
}
