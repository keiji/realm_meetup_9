package io.keiji.realmsample2.realm;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import io.keiji.realmsample2.realm.entity.Character;
import io.keiji.realmsample2.realm.entity.CharacterModel;
import io.keiji.realmsample2.realm.util.RealmAdapter;
import io.realm.Realm;

public class JsonLoaderJSONObject extends AsyncTaskLoader<LoaderResult> {

    private static final String JSON_FILE_NAME = "characters.json";

    private final RealmAdapter realmAdapter;

    public JsonLoaderJSONObject(Context context, RealmAdapter realmAdapter) {
        super(context);
        this.realmAdapter = realmAdapter;
    }

    @Override
    public LoaderResult loadInBackground() {

        LoaderResult result;

        Realm realm = Realm.getInstance(realmAdapter.getRealmConfiguration(getContext()));

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open(JSON_FILE_NAME)));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray userJsonArray = jsonObject.getJSONArray("characters");

            List<Character> characterList = CharacterModel.parseAsList(userJsonArray);

            realm.beginTransaction();
            for (Character character : characterList) {
                realm.copyToRealm(character);
            }
            realm.commitTransaction();

            result = new LoaderResult(null);

        } catch (IOException | JSONException e) {
            result = new LoaderResult(e);
        }

        realm.close();

        return result;
    }
}
