package com.nickwelna.issuemanagerforgithub;

import android.view.Menu;
import android.view.MenuItem;

public interface OptionsMenuProvider {
    void inflateOptionsMenu(Menu menu);

    boolean onOptionsItemSelected(MenuItem item);

    default void updateProviderData() {
    }
}
