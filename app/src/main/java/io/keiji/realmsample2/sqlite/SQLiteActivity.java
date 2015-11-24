package io.keiji.realmsample2.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.keiji.realmsample2.R;
import io.keiji.realmsample2.sqlite.entity.Character;

public class SQLiteActivity extends AppCompatActivity {
    private static final String TAG = SQLiteActivity.class.getSimpleName();

    private static final String IV_FILE_NAME = "initialization_vector.data";
    private static final String ENCRYPTED_DB_FILE_NAME = "db_file_encrypted.db";

    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final byte[] KEY = "thisismypa55w0rdthisismypa55w0rd".getBytes();

    private static final int LOADER_ID = 0x01;
    private static final int LOADER_ID_DECRYPT = 0x02;

    private final LoaderManager.LoaderCallbacks<Boolean> mCipherLoaderCallback
            = new LoaderManager.LoaderCallbacks<Boolean>() {

        @Override
        public Loader<Boolean> onCreateLoader(int id, Bundle args) {
            Loader loader = new DecryptTaskLoader(SQLiteActivity.this);
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
            if (data) {
                Log.d(TAG, "decryption complete.");
                mDb = new DbHelper(SQLiteActivity.this).getWritableDatabase();
                showDatabase();

            } else {
                Log.d(TAG, "new database.");
                mDb = new DbHelper(SQLiteActivity.this).getWritableDatabase();
                getSupportLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallback);
            }
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {

        }
    };

    private final LoaderManager.LoaderCallbacks<JsonLoader.Result> mLoaderCallback
            = new LoaderManager.LoaderCallbacks<JsonLoader.Result>() {

        @Override
        public Loader<JsonLoader.Result> onCreateLoader(int id, Bundle args) {
            JsonLoader loader = new JsonLoader(SQLiteActivity.this);
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<JsonLoader.Result> loader, JsonLoader.Result data) {
            if (data.exception != null) {
                Toast.makeText(SQLiteActivity.this, data.exception.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
            saveToDatabase(data.characterList);

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

    private void saveToDatabase(List<Character> characterList) {
        mDb.beginTransaction();

        for (Character character : characterList) {
            Character.insert(mDb, character);
        }

        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    private ListView mListView;
    private Adapter mAdapter;

    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_list);
        mListView = (ListView) findViewById(R.id.listview);

        getSupportLoaderManager().restartLoader(LOADER_ID_DECRYPT, null, mCipherLoaderCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDb.close();

        new EncryptTask(getDatabasePath(DbHelper.DB_FILE_NAME)).execute();
    }

    public class Adapter extends CursorAdapter {

        public Adapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = View.inflate(SQLiteActivity.this, R.layout.user_list_row, null);
            ViewHolder holder = new ViewHolder((TextView) view.findViewById(R.id.label));
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Character character = Character.read(cursor);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.label.setText(character.getName() + " " + character.getAge());
        }

        private class ViewHolder {
            public final TextView label;

            private ViewHolder(TextView label) {
                this.label = label;
            }
        }
    }

    private static class EncryptTask extends AsyncTask<Void, Void, Boolean> {

        private final File rawDbFile;

        public EncryptTask(File dbFile) {
            rawDbFile = dbFile;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d(TAG, "encryption complete.");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!rawDbFile.exists()) {
                return false;
            }

            File dbPath = rawDbFile.getParentFile();
            File ivFile = new File(dbPath, IV_FILE_NAME);
            File encryptedDbFile = new File(dbPath, ENCRYPTED_DB_FILE_NAME);

            OutputStream os = null;
            OutputStream osIv = null;
            InputStream is = null;

            try {
                SecretKeySpec key = new SecretKeySpec(KEY, "AES");

                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.ENCRYPT_MODE, key);

                is = new FileInputStream(rawDbFile);
                os = new FileOutputStream(encryptedDbFile);

                byte[] buffer = new byte[cipher.getBlockSize()];
                int len;

                while ((len = is.read(buffer)) != -1) {
                    byte[] encrypted = cipher.update(buffer, 0, len);
                    os.write(encrypted);
                }
                os.write(cipher.doFinal());

                rawDbFile.deleteOnExit();

                osIv = new FileOutputStream(ivFile);
                osIv.write(cipher.getIV());

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                Log.e(TAG, e.getClass().getSimpleName(), e);
            } catch (IOException e) {
                Log.e(TAG, e.getClass().getSimpleName(), e);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                Log.e(TAG, e.getClass().getSimpleName(), e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                if (os != null) {
                    try {
                        os.flush();
                        os.close();
                    } catch (IOException e) {
                    }
                }
                if (osIv != null) {
                    try {
                        osIv.flush();
                        osIv.close();
                    } catch (IOException e) {
                    }
                }
            }

            return true;
        }
    }

    private static class DecryptTaskLoader extends AsyncTaskLoader<Boolean> {

        public DecryptTaskLoader(Context context) {
            super(context);
        }

        @Override
        public Boolean loadInBackground() {
            File ivFile = getContext().getDatabasePath(IV_FILE_NAME);
            File encryptedDbFile = getContext().getDatabasePath(ENCRYPTED_DB_FILE_NAME);
            if (!encryptedDbFile.exists() || !ivFile.exists()) {
                return false;
            }

            File rawDbFile = getContext().getDatabasePath(DbHelper.DB_FILE_NAME);

            OutputStream os = null;
            InputStream is = null;
            InputStream isIV = null;

            try {
                isIV = new FileInputStream(ivFile);
                byte[] initializationVector = new byte[isIV.available()];

                int length = isIV.read(initializationVector);
                Log.d(TAG, "iv length = " + length);

                IvParameterSpec ivSpec = new IvParameterSpec(initializationVector);
                SecretKeySpec key = new SecretKeySpec(KEY, "AES");

                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

                is = new FileInputStream(encryptedDbFile);
                os = new FileOutputStream(rawDbFile);

                byte[] buffer = new byte[cipher.getBlockSize()];
                int len;

                while ((len = is.read(buffer)) != -1) {
                    byte[] decrypted = cipher.update(buffer, 0, len);
                    os.write(decrypted);
                }
                os.write(cipher.doFinal());

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                    | InvalidAlgorithmParameterException e) {
                Log.e(TAG, e.getClass().getSimpleName(), e);
            } catch (IOException e) {
                Log.e(TAG, e.getClass().getSimpleName(), e);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                Log.e(TAG, e.getClass().getSimpleName(), e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                if (os != null) {
                    try {
                        os.flush();
                        os.close();
                    } catch (IOException e) {
                    }
                }
                if (isIV != null) {
                    try {
                        isIV.close();
                    } catch (IOException e) {
                    }
                }
            }

            return true;
        }
    }
}
