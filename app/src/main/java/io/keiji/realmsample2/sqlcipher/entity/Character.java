package io.keiji.realmsample2.sqlcipher.entity;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Character {

    private long id;
    private String name;
    private Integer age;
    private boolean megane = true;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public boolean isMegane() {
        return megane;
    }

    public void setMegane(boolean megane) {
        this.megane = megane;
    }

    public static long insert(SQLiteDatabase db, Character data) {
        ContentValues values = new ContentValues();
        values.put("name", data.name);
        values.put("age", data.age);
        values.put("megane", data.megane ? 1 : 0);

        return db.insert("characters", null, values);
    }

    public static List<Character> parseAsList(JSONArray jsonArray) throws JSONException {
        List<Character> resultList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject userJson = jsonArray.getJSONObject(i);
            resultList.add(Character.parse(userJson));
        }

        return resultList;
    }

    private static Character parse(JSONObject userJson) throws JSONException {
        Character character = new Character();
        character.name = userJson.getString("name");
        String ageStr = userJson.getString("age");

        try {
            character.age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
        }

        character.megane = userJson.getBoolean("megane");
        return character;
    }

    public static Character read(Cursor cursor) {
        Character user = new Character();
        user.name = cursor.getString(cursor.getColumnIndex("name"));
        user.age = cursor.getInt(cursor.getColumnIndex("age"));
        user.megane = cursor.getInt(cursor.getColumnIndex("megane")) == 1;
        return user;
    }

    public static Cursor findAllMeganeCursor(SQLiteDatabase db) {
        return db.query("characters", new String[]{"_id", "name", "age", "megane"},
                "megane = ?", new String[]{Integer.toString(1)}, null, null, null);
    }
}
