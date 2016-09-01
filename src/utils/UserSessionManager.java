package utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.votingapp.User;

public class UserSessionManager {

	// Shared Preferences reference
	SharedPreferences pref;

	// Editor reference for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREFER_NAME = "AndroidExamplePref";

	// All Shared Preferences Keys
	private static final String IS_USER_LOGIN = "IsUserLoggedIn";


	// Constructor
	@SuppressLint("CommitPrefEdits")
	public UserSessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	// Create login session
	public void createUserLoginSession(User user) {
		// Storing login value as TRUE
		editor.putBoolean(IS_USER_LOGIN, true);
		Gson gson = new Gson();
		String json = gson.toJson(user);
		editor.putString("user", json);
		// commit changes
		editor.commit();
	}

	// Create session
	public void createSession(String connection) {
		editor.putString("connection", connection);
		// commit changes
		editor.commit();
	}
	// get session
	public String getSession() {
		return pref.getString("connection", "");
	}

     
	/**
	 * Get stored session data
	 * */
	public User getUserDetails() {
		Gson gson = new Gson();
		String json = pref.getString("user", "");
		User user = gson.fromJson(json, User.class);
		// return user
		return user;
	}

	/**
	 * Clear session details
	 * */
	public void logoutUser() {
		editor.putString("user", "");
		editor.putBoolean(IS_USER_LOGIN, false);
		editor.commit();
	}

	// Check for login
	public boolean isUserLoggedIn() {
		return pref.getBoolean(IS_USER_LOGIN, false);
	}
}