package io.keiji.realmsample2.realm.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CharacterModel {

    public static List<Character> parseAsList(JSONArray jsonArray) throws JSONException {
        List<Character> resultList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject userJson = jsonArray.getJSONObject(i);
            resultList.add(CharacterModel.parse(userJson));
        }

        return resultList;
    }

    private static Character parse(JSONObject characterJson) throws JSONException {
        Character character = new Character();
        character.setName(characterJson.getString("name"));
        String ageStr = characterJson.getString("age");
        if ("N/A".equals(ageStr)) {
            character.setAge(-1);
        } else {
            character.setAge(Integer.parseInt(ageStr));
        }
        character.setMegane(characterJson.getBoolean("megane"));

        return character;
    }
}
