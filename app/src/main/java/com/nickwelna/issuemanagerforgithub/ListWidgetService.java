package com.nickwelna.issuemanagerforgithub;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nickwelna.issuemanagerforgithub.models.Repository;

import java.util.ArrayList;
import java.util.List;

public class ListWidgetService extends RemoteViewsService {

    boolean connected;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new ListRemoteViewFactory(this.getApplicationContext());

    }

    class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        Context context;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference userDataReference =
                database.getReference("users").child(auth.getCurrentUser().getUid());
        List<Repository> pinnedRepositories;

        public ListRemoteViewFactory(Context applicationContext) {

            context = applicationContext;

        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

            DatabaseReference connectedRef =
                    FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    connected = snapshot.getValue(Boolean.class);

                }

                @Override
                public void onCancelled(DatabaseError error) {

                }

            });

            if (connected) {
                DatabaseReference pinnedRepos = userDataReference.child("pinned_repos");
                ValueEventListener pinnedRepositoryListener = new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        pinnedRepositories = new ArrayList<>();
                        for (DataSnapshot pinnedRepoSnapshot : dataSnapshot.getChildren()) {

                            Repository temp = new Repository();
                            temp.setFullName(pinnedRepoSnapshot.getValue(String.class));
                            pinnedRepositories.add(temp);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                pinnedRepos.addListenerForSingleValueEvent(pinnedRepositoryListener);
            }
            else {

                Toast.makeText(context, R.string.network_error_toast, Toast.LENGTH_LONG).show();

            }

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {

            if (pinnedRepositories != null) {

                return pinnedRepositories.size();

            }
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews views =
                    new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
            views.setTextViewText(R.id.repository_name,
                    pinnedRepositories.get(position).getFull_name());

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("repository", pinnedRepositories.get(position).getFull_name());
            views.setOnClickFillInIntent(R.id.repository_name, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {

            return null;

        }

        @Override
        public int getViewTypeCount() {

            return 1;

        }

        @Override
        public long getItemId(int position) {

            return position;

        }

        @Override
        public boolean hasStableIds() {

            return true;

        }

    }

}
