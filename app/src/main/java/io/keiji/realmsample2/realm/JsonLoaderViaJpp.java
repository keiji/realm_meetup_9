package io.keiji.realmsample2.realm;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import net.vvakame.util.jsonpullparser.JsonFormatException;

import java.io.IOException;

import io.keiji.realmsample2.realm.entity.Character;
import io.keiji.realmsample2.realm.entity.Characters4Jpp;
import io.keiji.realmsample2.realm.entity.Characters4JppGen;
import io.keiji.realmsample2.realm.util.RealmAdapter;
import io.realm.Realm;

public class JsonLoaderViaJpp extends AsyncTaskLoader<LoaderResult> {

    private static final String JSON_FILE_NAME = "characters.json";

    private final RealmAdapter realmAdapter;

    public JsonLoaderViaJpp(Context context, RealmAdapter realmAdapter) {
        super(context);
        this.realmAdapter = realmAdapter;
    }

    @Override
    public LoaderResult loadInBackground() {

        LoaderResult result;

        Realm realm = Realm.getInstance(realmAdapter.getRealmConfiguration(getContext()));

        try {
            Characters4Jpp characters = Characters4JppGen.get(
                    getContext().getAssets().open(JSON_FILE_NAME));

            realm.beginTransaction();
            for (Character character : characters.getCharacters()) {
                realm.copyToRealm(character);
            }
            realm.commitTransaction();

            result = new LoaderResult(null);

        } catch (IOException | JsonFormatException e) {
            result = new LoaderResult(e);
        }

        realm.close();

        return result;
    }
}
