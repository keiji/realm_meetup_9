package io.keiji.realmsample2.realm;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;

import io.keiji.realmsample2.realm.entity.Characters;
import io.keiji.realmsample2.realm.util.RealmAdapter;
import io.realm.Realm;

public class JsonLoaderRealmJson extends AsyncTaskLoader<LoaderResult> {

    private static final String JSON_FILE_NAME = "characters.json";

    private final RealmAdapter realmAdapter;

    public JsonLoaderRealmJson(Context context, RealmAdapter realmAdapter) {
        super(context);
        this.realmAdapter = realmAdapter;
    }

    @Override
    public LoaderResult loadInBackground() {

        LoaderResult result;

        Realm realm = Realm.getInstance(realmAdapter.getRealmConfiguration(getContext()));

        try {

            realm.beginTransaction();
            realm.createObjectFromJson(Characters.class, getContext().getAssets().open(JSON_FILE_NAME));
            realm.commitTransaction();

            result = new LoaderResult(null);

        } catch (IOException e) {
            result = new LoaderResult(e);
        }

        realm.close();

        return result;
    }
}
