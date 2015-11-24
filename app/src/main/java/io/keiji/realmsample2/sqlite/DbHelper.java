package io.keiji.realmsample2.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_FILE_NAME = "sql_raw.db";
    private static final int DB_VERSION = 1;


    public DbHelper(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);
    }

    private static final String CREATE_TABLE_USERS = "CREATE TABLE characters ( " +
            "_id INTEGER PRIMARY KEY," +
            "name TEXT," +
            "age INTEGER," +
            "megane INTEGER" +
            " )";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
