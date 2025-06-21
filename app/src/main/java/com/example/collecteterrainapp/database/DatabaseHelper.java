package com.example.collecteterrainapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "collecte_terrain.db";
    private static final int DATABASE_VERSION = 3;

    // Noms des colonnes pour la table collectes
    private static final String TABLE_COLLECTES = "collectes";
    private static final String COLUMN_COLLECTE_ID = "id";
    private static final String COLUMN_COLLECTE_USERNAME = "username";
    private static final String COLUMN_COLLECTE_NOM = "nom_collecte";
    private static final String COLUMN_COLLECTE_PHOTO_URL = "photo_url";
    private static final String COLUMN_COLLECTE_AMOUNT = "amount";
    private static final String COLUMN_COLLECTE_COMMENT = "comment";
    private static final String COLUMN_COLLECTE_LATITUDE = "latitude";
    private static final String COLUMN_COLLECTE_LONGITUDE = "longitude";
    private static final String COLUMN_COLLECTE_TIMESTAMP = "timestamp";
    private static final String COLUMN_COLLECTE_SYNCED = "synced";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "password TEXT, " +
                "role TEXT, " +
                "birth TEXT, " +
                "city TEXT, " +
                "photo_path TEXT)");

        String CREATE_COLLECTES_TABLE = "CREATE TABLE " + TABLE_COLLECTES + "(" +
                COLUMN_COLLECTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_COLLECTE_USERNAME + " TEXT," +
                COLUMN_COLLECTE_NOM + " TEXT," +
                COLUMN_COLLECTE_PHOTO_URL + " TEXT," +
                COLUMN_COLLECTE_AMOUNT + " REAL," +
                COLUMN_COLLECTE_COMMENT + " TEXT," +
                COLUMN_COLLECTE_LATITUDE + " REAL," +
                COLUMN_COLLECTE_LONGITUDE + " REAL," +
                COLUMN_COLLECTE_TIMESTAMP + " INTEGER," +
                COLUMN_COLLECTE_SYNCED + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_COLLECTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTES);
        onCreate(db);
    }

    // Vérifier si l'utilisateur existe
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Ajouter un utilisateur
    public boolean addUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        long result = db.insert("users", null, values);
        return result != -1;
    }


    // Récupérer l'utilisateur par username
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
    }

    // Mettre à jour le profil utilisateur
    public boolean updateUserProfile(int userId, String birth, String city, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("birth", birth);
        values.put("city", city);
        values.put("photo_path", photoPath);
        int result = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        return result > 0;
    }


    // Ajouter une collecte locale
    public void saveLocalCollecte(String username, String nomCollecte, double amount, String comment,
                                  double latitude, double longitude, long timestamp, String photoUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COLLECTE_USERNAME, username);
        values.put(COLUMN_COLLECTE_NOM, nomCollecte);
        values.put(COLUMN_COLLECTE_PHOTO_URL, photoUrl);
        values.put(COLUMN_COLLECTE_AMOUNT, amount);
        values.put(COLUMN_COLLECTE_COMMENT, comment);
        values.put(COLUMN_COLLECTE_LATITUDE, latitude);
        values.put(COLUMN_COLLECTE_LONGITUDE, longitude);
        values.put(COLUMN_COLLECTE_TIMESTAMP, timestamp);
        values.put(COLUMN_COLLECTE_SYNCED, 0);
        db.insert(TABLE_COLLECTES, null, values);
    }

    // Récupérer les collectes non synchronisées
    public List<Map<String, Object>> getUnsyncedCollectes() {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COLLECTES + " WHERE " + COLUMN_COLLECTE_SYNCED + " = 0", null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> data = new HashMap<>();
                data.put(COLUMN_COLLECTE_USERNAME, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_USERNAME)));
                data.put(COLUMN_COLLECTE_NOM, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_NOM)));
                data.put(COLUMN_COLLECTE_PHOTO_URL, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_PHOTO_URL)));
                data.put(COLUMN_COLLECTE_AMOUNT, cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_AMOUNT)));
                data.put(COLUMN_COLLECTE_COMMENT, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_COMMENT)));
                data.put(COLUMN_COLLECTE_LATITUDE, cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_LATITUDE)));
                data.put(COLUMN_COLLECTE_LONGITUDE, cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_LONGITUDE)));
                data.put(COLUMN_COLLECTE_TIMESTAMP, cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COLLECTE_TIMESTAMP)));
                list.add(data);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    // Marquer une collecte comme synchronisée
    public void markCollecteAsSynced(long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COLLECTE_SYNCED, 1);
        db.update(TABLE_COLLECTES, values, COLUMN_COLLECTE_TIMESTAMP + " = ?", new String[]{String.valueOf(timestamp)});
    }



}
