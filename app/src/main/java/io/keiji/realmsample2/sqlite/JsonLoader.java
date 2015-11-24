package io.keiji.realmsample2.sqlite;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import io.keiji.realmsample2.sqlite.entity.Character;

public class JsonLoader extends AsyncTaskLoader<JsonLoader.Result> {

    private static final String JSON_FILE_NAME = "characters.json";

    public JsonLoader(Context context) {
        super(context);
    }

    @Override
    public Result loadInBackground() {

        Result result;

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open(JSON_FILE_NAME)));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray characterJsonArray = jsonObject.getJSONArray("characters");

            List<Character> characterList = Character.parseAsList(characterJsonArray);

            result = new Result(characterList);

        } catch (IOException | JSONException e) {
            result = new Result(e);
        }
        return result;
    }

    public static class Result {
        public final List<Character> characterList;
        public final Exception exception;

        Result(List<Character> characterList) {
            this.characterList = characterList;
            this.exception = null;
        }

        Result(Exception exception) {
            this.characterList = null;
            this.exception = exception;
        }
    }
}
