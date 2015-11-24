package io.keiji.realmsample2.realm.util;

import android.content.Context;

import io.realm.RealmConfiguration;

public class RealmAdapter {

    private static final byte[] KEY = "thisismypa55w0rdthisismypa55w0rdthisismypa55w0rdthisismypa55w0rd".getBytes();

    public RealmConfiguration getRealmConfiguration(Context context) {
        return new RealmConfiguration.Builder(context)
                .encryptionKey(KEY)
                .build();
    }
}
