package com.votingapp;

import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class LoginActivity extends Activity implements OnClickListener {
	private ProgressDialog pDialog;
	Button btnLogin, btnLinkToRegister;
	EditText inputEmail;
	EditText inputPassword;
	CheckBox mCbShowPwd;
	UserSessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		inputEmail = (EditText) findViewById(R.id.email);
		inputPassword = (EditText) findViewById(R.id.password);
		btnLogin = (Button) findViewById(R.id.login);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		mCbShowPwd = (CheckBox) findViewById(R.id.cbShowPwd);
		mCbShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// checkbox status is changed from uncheck to checked.
				if (!isChecked) {
					// show password
					inputPassword
							.setTransformationMethod(PasswordTransformationMethod
									.getInstance());
				} else {
					// hide password
					inputPassword
							.setTransformationMethod(HideReturnsTransformationMethod
									.getInstance());
				}
			}
		});
		// Login button Click Event
		btnLogin.setOnClickListener(this);
		// Link to Register Screen
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						RegisterActivity.class);
				startActivity(i);
				finish();
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			new AttemptLogin().execute();
			break;
		default:
			break;
		}
	}

	class AttemptLogin extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Attempting login...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			String email = inputEmail.getText().toString();
			String password = inputPassword.getText().toString();
			session = new UserSessionManager(getApplicationContext());
			UserFunctions userFunction = new UserFunctions(session.getSession());

			User user = userFunction.loginUser(email, password);

			if (user == null)
				return "Incorrect email or password";

			
			session.createUserLoginSession(user);

			Intent i = new Intent(getApplicationContext(), MenuActivity.class);
			startActivity(i);
			finish();

			return null;
		}

		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if (file_url != null) {
				Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG)
						.show();
			}

		}

	}

}
