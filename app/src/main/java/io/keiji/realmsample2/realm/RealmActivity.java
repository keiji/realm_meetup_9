package io.keiji.realmsample2.realm;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.keiji.realmsample2.R;
import io.keiji.realmsample2.realm.entity.Character;
import io.keiji.realmsample2.realm.util.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class RealmActivity extends AppCompatActivity {
    private static final String TAG = RealmActivity.class.getSimpleName();

    private static final int LOADER_ID = 0x01;

    private final LoaderManager.LoaderCallbacks<LoaderResult> mLoaderCallback
            = new LoaderManager.LoaderCallbacks<LoaderResult>() {

        @Override
        public Loader<LoaderResult> onCreateLoader(int id, Bundle args) {
            Loader loader = new JsonLoaderViaJpp(RealmActivity.this, mRealmAdapter);
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult data) {
            if (data.exception != null) {
                Toast.makeText(RealmActivity.this, data.exception.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onLoaderReset(Loader<LoaderResult> loader) {

        }
    };

    private ListView mListView;
    private Adapter mAdapter;

    private Realm mRealm;
    private RealmAdapter mRealmAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_list);

        mRealmAdapter = new RealmAdapter();

        mRealm = Realm.getInstance(mRealmAdapter.getRealmConfiguration(this));

        RealmResults<Character> realmResult = mRealm
                .where(Character.class)
                .equalTo("megane", true)
                .findAll();

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new Adapter(this, realmResult, true);
        mListView.setAdapter(mAdapter);

        if (realmResult.size() == 0) {
            getSupportLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    public class Adapter extends RealmBaseAdapter<Character> {

        public Adapter(Context context, RealmResults<Character> realmResults, boolean automaticUpdate) {
            super(context, realmResults, automaticUpdate);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Character character = getItem(position);

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(RealmActivity.this, R.layout.user_list_row, null);
                holder = new ViewHolder((TextView) convertView.findViewById(R.id.label));
                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();
            holder.label.setText(character.getName() + " " + character.getAge());

            return convertView;
        }

        private class ViewHolder {
            public final TextView label;

            private ViewHolder(TextView label) {
                this.label = label;
            }
        }
    }
}
