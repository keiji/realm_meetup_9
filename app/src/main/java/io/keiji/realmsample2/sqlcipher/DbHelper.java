package io.keiji.realmsample2.sqlcipher;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_FILE_NAME = "sqlcipher.db";
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
