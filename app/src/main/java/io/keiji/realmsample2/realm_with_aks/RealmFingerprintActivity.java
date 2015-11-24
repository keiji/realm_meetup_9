package io.keiji.realmsample2.realm_with_aks;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@TargetApi(Build.VERSION_CODES.M)
public class RealmFingerprintActivity extends RealmActivity {
    private static final String TAG = RealmFingerprintActivity.class.getSimpleName();

    private final AuthenticationCallback mAuthenticationCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            readyRealm();
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    void authorize() {
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        //noinspection ResourceType
        if (!fingerprintManager.isHardwareDetected()) {
            finish();
            return;
        }

        //noinspection ResourceType
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            finish();
            return;
        }

        try {
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_ALIAS, null);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

            Toast.makeText(this, "指紋スキャナーに指を当てて下さい...", Toast.LENGTH_LONG)
                    .show();

            //noinspection ResourceType
            fingerprintManager.authenticate(cryptoObject, null, 0, mAuthenticationCallback, null);

        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException", e);
            Toast.makeText(getApplicationContext(),
                    "鍵が無効です", Toast.LENGTH_SHORT).show();
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException
                | KeyStoreException | NoSuchPaddingException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }
    }
}
