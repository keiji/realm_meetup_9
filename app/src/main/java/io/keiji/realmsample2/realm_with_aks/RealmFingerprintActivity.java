package io.keiji.realmsample2.realm_with_aks;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

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
            KeyStore keyStore = KeyStore.getInstance(PROVIDER_NAME);
            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

            //noinspection ResourceType
            fingerprintManager.authenticate(cryptoObject, null, 0, mAuthenticationCallback, null);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException", e);
            Toast.makeText(getApplicationContext(),
                    "鍵が無効です", Toast.LENGTH_SHORT).show();
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException
                | KeyStoreException | NoSuchPaddingException | CertificateException | IOException e) {
            Log.e(TAG, e.getClass().getSimpleName(), e);
        }


    }
}
