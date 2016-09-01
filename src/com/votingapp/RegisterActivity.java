package com.votingapp;

import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {
	private ProgressDialog pDialog;
	private Button btnRegister;
	private EditText inputFullName, inputEmail, inputPassword,
			confirmedPassword;
	private RadioGroup gender;
	private RadioButton genderButton;
	String genValue = "Male";
	private UserSessionManager session;
	Bitmap selectedImg;

	// JSON Response node names

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// Importing all assets like buttons, text fields
		inputFullName = (EditText) findViewById(R.id.username);
		inputEmail = (EditText) findViewById(R.id.registerEmail);
		inputPassword = (EditText) findViewById(R.id.registerPassword);
		confirmedPassword = (EditText) findViewById(R.id.confirmedPass);
		gender = (RadioGroup) findViewById(R.id.gender);
		btnRegister = (Button) findViewById(R.id.btnRegister);

		btnRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRegister:
			new CreateUser().execute();
			break;
		default:
			break;
		}
	}

	class CreateUser extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RegisterActivity.this);
			pDialog.setMessage("Creating User...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {

			String name = inputFullName.getText().toString();
			String email = inputEmail.getText().toString();
			String password = inputPassword.getText().toString();
			String confirmedPass = confirmedPassword.getText().toString();

			int selectedId = gender.getCheckedRadioButtonId();
			genderButton = (RadioButton) findViewById(selectedId);

			if (name.trim().isEmpty())
				return "Username is required";
			if (email.trim().isEmpty())
				return "Email is required";
			if (password.trim().isEmpty())
				return "Password is required";
			if (!password.equals(confirmedPass))
				return "Passwords does not match";

			session = new UserSessionManager(getApplicationContext());
			UserFunctions userFunction = new UserFunctions(session.getSession());
			User user = userFunction.registerUser(name, email, password,
					genderButton.getText().toString());

			if (user == null)
				return "This email is already exists";

			session.createUserLoginSession(user);

			Intent menu = new Intent(getApplicationContext(),
					MenuActivity.class);
			menu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(menu);
			finish();
			// Close Registration Screen

			return null;
		}

		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if (file_url != null) {
				Toast.makeText(RegisterActivity.this, file_url,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
