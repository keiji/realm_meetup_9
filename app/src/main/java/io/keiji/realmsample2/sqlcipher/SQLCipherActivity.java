package io.keiji.realmsample2.sqlcipher;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;

import io.keiji.realmsample2.R;
import io.keiji.realmsample2.sqlcipher.entity.Character;

public class SQLCipherActivity extends AppCompatActivity {
    private static final String TAG = SQLCipherActivity.class.getSimpleName();

    private static final int LOADER_ID = 0x01;
    private static final String DB_PASSWORD = "pa55w0rd";

    private final LoaderManager.LoaderCallbacks<JsonLoader.Result> mLoaderCallback
            = new LoaderManager.LoaderCallbacks<JsonLoader.Result>() {

        @Override
        public Loader<JsonLoader.Result> onCreateLoader(int id, Bundle args) {
            JsonLoader loader = new JsonLoader(SQLCipherActivity.this);
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<JsonLoader.Result> loader, JsonLoader.Result data) {
            if (data.exception != null) {
                Toast.makeText(SQLCipherActivity.this, data.exception.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
            saveToDatabase(data.userList);

            showDatabase();
        }

        @Override
        public void onLoaderReset(Loader<JsonLoader.Result> loader) {

        }
    };

    private void showDatabase() {
        Cursor cursor = Character.findAllMeganeCursor(mDb);

        if (mAdapter == null) {
            mAdapter = new Adapter(this, cursor, false);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.changeCursor(cursor);

        }
    }

    private void saveToDatabase(List<Character> userList) {
        for (Character user : userList) {
            Character.insert(mDb, user);
        }
    }

    private ListView mListView;
    private Adapter mAdapter;

    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteDatabase.loadLibs(this);

        setContentView(R.layout.user_list);
        mListView = (ListView) findViewById(R.id.listview);

        if (!getDatabasePath(DbHelper.DB_FILE_NAME).exists()) {
            mDb = new DbHelper(this).getWritableDatabase(DB_PASSWORD);
            getSupportLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallback);

        } else {
            mDb = new DbHelper(this).getWritableDatabase(DB_PASSWORD);
            showDatabase();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAdapter.changeCursor(null);
        mDb.close();
    }

    public class Adapter extends CursorAdapter {

        public Adapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, android.database.Cursor cursor, ViewGroup parent) {
            View view = View.inflate(SQLCipherActivity.this, R.layout.user_list_row, null);
            ViewHolder holder = new ViewHolder((TextView) view.findViewById(R.id.label));
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, android.database.Cursor cursor) {
            Character user = Character.read(cursor);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.label.setText(user.getName() + " " + user.getAge());
        }

        private class ViewHolder {
            public final TextView label;

            private ViewHolder(TextView label) {
                this.label = label;
            }
        }
    }

}
