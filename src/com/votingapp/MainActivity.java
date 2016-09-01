package com.votingapp;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import serverconnection.JSONRequest;
import utils.UserSessionManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ProgressDialog pDialog;
	private UserSessionManager session;
	private EditText ipAddress, portNumber;
	private Button connect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		session = new UserSessionManager(getApplicationContext());
		if (!session.getSession().equals("")) {
			goToNextActivity();
		}
		ipAddress = (EditText) findViewById(R.id.editText1);
		portNumber = (EditText) findViewById(R.id.editText2);
		connect = (Button) findViewById(R.id.button1);

		connect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (isConnectedToInternet()) {
					String fullAddress = "http://"
							+ ipAddress.getText().toString() + ":"
							+ portNumber.getText().toString()
							+ "/VotingServer/UserAndroidResponse";
					new Connect().execute(fullAddress);
				} else {
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("Error")
							.setMessage("Connection failed!")
							.setPositiveButton("Return",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
										}
									}).show();
				}
			}
		});
	}

	public boolean isConnectedToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
						return true;
		}
		return false;
	}

	class Connect extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Connecting...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... fullAddress) {
			if (!canConnect(fullAddress[0])) {
				return null;
			}
			Log.e("start", "Starting....");
			session.createSession(fullAddress[0]);
			return "success";
		}

		private boolean canConnect(String fullAddress) {

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters
					.add(new BasicNameValuePair("tag", "checkConnection"));
			String response = null;
			JSONObject responseObj = null;
			boolean success = false;
			try {
				JSONRequest request = new JSONRequest();
				response = request.sendHttpRequest(fullAddress, postParameters);
				responseObj = new JSONObject(response);
				success = responseObj.getBoolean("success");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (success) {
				return true;
			}
			return false;

		}

		@Override
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if (file_url != null) {
				goToNextActivity();
			} else {
				Toast.makeText(MainActivity.this, "Error connecting to server",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	public void goToNextActivity() {
		if (session.isUserLoggedIn()) {
			Intent v = new Intent(getApplicationContext(), MenuActivity.class);
			v.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(v);
			finish();
		} else {
			Intent login = new Intent(getApplicationContext(),
					LoginActivity.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(login);
			finish();
		}
	}
}
