package io.keiji.realmsample2.realm_with_aks;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.keiji.realmsample2.R;
import io.keiji.realmsample2.realm.JsonLoaderViaJpp;
import io.keiji.realmsample2.realm.LoaderResult;
import io.keiji.realmsample2.realm.entity.Character;
import io.keiji.realmsample2.realm_with_aks.util.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class RealmActivity extends AppCompatActivity {
    private static final String TAG = RealmActivity.class.getSimpleName();

    static final String PROVIDER_NAME = "AndroidKeyStore";

    static final String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES
            + "/" + KeyProperties.BLOCK_MODE_CBC
            + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;

    static final String KEY_ALIAS = "auth_key";
    private static final int AUTH_VALID_DURATION_IN_SECOND = 30;

    static final int KEY_SIZE = 512 / 8;
    static final String KEY_FILE_NAME = "encrypted_passkey.dat";

    private static final int REQUEST_CREDENTIAL = 0x00;

    static final int LOADER_ID = 0x01;

    final LoaderManager.LoaderCallbacks<LoaderResult> mLoaderCallback
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

    ListView mListView;
    Adapter mAdapter;

    Realm mRealm;

    private RealmAdapter mRealmAdapter;
    private KeyStore mKeyStore;

    @TargetApi(Build.VERSION_CODES.M)
    private static void generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    PROVIDER_NAME);

            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(
                            AUTH_VALID_DURATION_IN_SECOND)
                    .build());
            keyGenerator.generateKey();

        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(this, "Marshmallow以上に対応しています。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mRealmAdapter = new RealmAdapter();

        try {
            mKeyStore = KeyStore.getInstance(PROVIDER_NAME);
            mKeyStore.load(null);

            if (!mKeyStore.containsAlias(KEY_ALIAS)) {
                generateKey();
            }

        } catch (IOException | CertificateException | NoSuchAlgorithmException
                | KeyStoreException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }

        setContentView(R.layout.user_list);

        authorize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void authorize() {
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        Intent intent = km.createConfirmDeviceCredentialIntent("Android Keystore System",
                "Android Keystore Systemに保存した鍵を使ってRealmのデータベースにアクセスします");

        startActivityForResult(intent, REQUEST_CREDENTIAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            readyRealm();
        }

    }

    void readyRealm() {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] encryptedKey = null;

            File file = new File(getFilesDir(), KEY_FILE_NAME);
            if (!file.exists()) {
                byte[] passkey = new SecureRandom().generateSeed(KEY_SIZE);
                encryptedKey = encryptRealmKey(passkey, cipher);

                try (OutputStream os = new FileOutputStream(file)) {
                    os.write(encryptedKey);

                } catch (IOException e) {
                }
            } else {
                try (InputStream is = new FileInputStream(file)) {
                    encryptedKey = new byte[is.available()];
                    is.read(encryptedKey);

                } catch (IOException e) {
                }
            }

            RealmAdapter.setKey(decryptRealmKey(encryptedKey, cipher));

            mRealm = Realm.getInstance(new RealmAdapter().getRealmConfiguration(this));

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
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
        }
    }

    byte[] encryptRealmKey(byte[] passkey, Cipher cipher) {

        try {
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_ALIAS, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(passkey);
            byte[] initializationVector = cipher.getIV();

            byte[] result = new byte[initializationVector.length + encrypted.length];
            System.arraycopy(initializationVector, 0, result, 0, initializationVector.length);
            System.arraycopy(encrypted, 0, result, initializationVector.length, encrypted.length);

            return result;

        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException", e);
            Toast.makeText(getApplicationContext(),
                    "鍵が無効です", Toast.LENGTH_SHORT).show();
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException
                | KeyStoreException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }

        return null;
    }

    byte[] decryptRealmKey(byte[] encrypted, Cipher cipher) {
        byte[] initializationVector = new byte[16];
        byte[] passKey = new byte[encrypted.length - initializationVector.length];

        System.arraycopy(encrypted, 0, initializationVector, 0, initializationVector.length);
        System.arraycopy(encrypted, initializationVector.length, passKey, 0, passKey.length);

        try {
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_ALIAS, null);

            IvParameterSpec ivSpec = new IvParameterSpec(initializationVector);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            return cipher.doFinal(passKey);

        } catch (InvalidKeyException e) {
            Toast.makeText(getApplicationContext(),
                    "鍵が無効です", Toast.LENGTH_SHORT).show();
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException
                | KeyStoreException | BadPaddingException | IllegalBlockSizeException
                | InvalidAlgorithmParameterException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }

        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRealm != null) {
            mRealm.close();
        }
    }

    public class Adapter extends RealmBaseAdapter<Character> {

        public Adapter(Context context, RealmResults<Character> realmResults, boolean automaticUpdate) {
            super(context, realmResults, automaticUpdate);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Character character = getItem(position);

            ViewHolder holder;
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
