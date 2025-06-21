package com.example.collecteterrainapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.collecteterrainapp.shared.models.User;

public class SessionManager {
    private static final String PREF_NAME = "session";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId"; // Local DB ID
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_ROLE = "role";
    private static final String KEY_PHOTO_URL = "photoUrl";
    // Add other keys if more user data is stored (e.g., KEY_EMAIL, KEY_UID for Firebase)

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    private static SessionManager instance;

    private SessionManager(Context context) {
        this._context = context.getApplicationContext(); // Use application context
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void createLoginSession(int userId, String username, String displayName, String role, String photoUrl) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.putString(KEY_ROLE, role);
        if (photoUrl != null) {
            editor.putString(KEY_PHOTO_URL, photoUrl);
        }
        editor.commit(); // Use commit to save synchronously
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null; // Or throw an exception, or return an empty User object
        }
        User user = new User();
        // Note: User model uses setUid for Firebase UID. We are storing local DB id in KEY_USER_ID.
        // If User model needs local DB ID, add a field like setLocalDbId().
        // For now, we'll set what we have that maps to the User model.
        // user.setUid(pref.getString(KEY_UID, null)); // If Firebase UID was stored
        user.setDisplayName(pref.getString(KEY_DISPLAY_NAME, null));
        user.setRole(pref.getString(KEY_ROLE, null));
        user.setPhotoUrl(pref.getString(KEY_PHOTO_URL, null));
        // user.setEmail(pref.getString(KEY_EMAIL, null)); // If email was stored

        // You might want to add other fields from SharedPreferences to the User object if they exist
        // For example, a local user ID if your User model has a field for it.
        // user.setLocalId(pref.getInt(KEY_USER_ID, 0)); 

        return user;
    }
    
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, 0); // Return 0 or -1 if not found
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getDisplayName() {
        return pref.getString(KEY_DISPLAY_NAME, null);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, null);
    }

    public String getPhotoUrl() {
        return pref.getString(KEY_PHOTO_URL, null);
    }

    public void clearSession() {
        editor.clear();
        editor.commit();

        // Optional: Redirect to login or handle as needed after clearing session
        // Intent i = new Intent(_context, MainActivity.class);
        // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // _context.startActivity(i);
    }
} 