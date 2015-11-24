package io.keiji.realmsample2.realm_with_aks.util;

import android.content.Context;

import io.realm.RealmConfiguration;

public class RealmAdapter extends io.keiji.realmsample2.realm.util.RealmAdapter {

    private static final String NAME = "realm_with_aks.realm";

    private static byte[] sKey;

    public static void setKey(byte[] key) {
        sKey = key;
    }

    @Override
    public RealmConfiguration getRealmConfiguration(Context context) {
        return new RealmConfiguration.Builder(context)
                .encryptionKey(sKey)
                .name(NAME)
                .build();
    }
}
