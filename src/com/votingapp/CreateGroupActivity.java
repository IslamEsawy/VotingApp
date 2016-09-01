package com.votingapp;

import java.util.Vector;

import search.MultipleCustomAdapter;
import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreateGroupActivity extends Activity implements
		OnItemClickListener {
	private UserSessionManager session;
	private UserFunctions funcs;
	private User loggedIn;
	private Button create;
	private RadioButton type;
	private RadioGroup rd;
	private Vector<User> userlist;
	private MultipleCustomAdapter adapter;
	private EditText name;
	private ProgressDialog pDialog;

	private Vector<User> selected = new Vector<User>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		session = new UserSessionManager(getApplicationContext());
		funcs = new UserFunctions(session.getSession());
		loggedIn = session.getUserDetails();
		setContentView(R.layout.activity_create_group);

		Thread t = new Thread() {
			@Override
			public void run() {
				userlist = funcs.getUsers("select * from user");
			}
		};
		try {
			t.start();
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		name = (EditText) findViewById(R.id.name);
		name.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (!name.getText().toString().isEmpty()
						&& selected.size() != 0) {
					create.setEnabled(true);
				} else {
					create.setEnabled(false);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!name.getText().toString().isEmpty()
						&& selected.size() != 0) {
					create.setEnabled(true);
				} else {
					create.setEnabled(false);
				}
			}
		});
		rd = (RadioGroup) findViewById(R.id.radioGroup1);

		ListView accountList = (ListView) findViewById(R.id.accounts);
		adapter = new MultipleCustomAdapter(getApplicationContext(), userlist);
		accountList.setAdapter(adapter);
		accountList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		accountList.setOnItemClickListener(CreateGroupActivity.this);

		create = (Button) findViewById(R.id.create_group);
		create.setOnClickListener(btnCreateListener);
		create.setEnabled(false);
	}

	private OnClickListener btnCreateListener = new OnClickListener() {
		public void onClick(View v) {
			if (name.getText().toString().isEmpty())
				return;
			new CreateGroup().execute();
		}
	};

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// Check if number is in participants list
		User sel = (User) parent.getItemAtPosition(position);
		adapter.toggleChecked(position);
		Log.e("msg1", sel.getEmail());
		if (selected.contains(sel)) {
			// Number is in list, we remove it
			selected.remove(sel);
		} else {
			Log.e("msg2", sel.getEmail());
			// Number is not in list, add it
			selected.add(sel);
		}

		// Disable the invite button if no contact selected
		if (selected.size() == 0 || name.getText().toString().isEmpty()) {
			create.setEnabled(false);
		} else {
			create.setEnabled(true);
		}
	}

	class CreateGroup extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CreateGroupActivity.this);
			pDialog.setMessage("Creating Group...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			int selectedId = rd.getCheckedRadioButtonId();
			type = (RadioButton) findViewById(selectedId);
			int gType = 1;
			if (type.getText().toString().equals("Only me"))
				gType = 0;
			String query1 = "INSERT INTO `group`(`adminid`, `name`, `type`) VALUES ('"
					+ loggedIn.getId()
					+ "', '"
					+ name.getText().toString()
					+ "', '" + gType + "')";
			Group g = funcs.insertGroup(query1, name.getText().toString());
			if (g != null) {
				query1 = "INSERT INTO `group_user`(`gid`, `userid`) VALUES ('"
						+ g.getId() + "', '" + loggedIn.getId() + "');";
				for (int i = 0; i < selected.size(); i++) {
					query1 += "INSERT INTO `group_user`(`gid`, `userid`) VALUES ('"
							+ g.getId()
							+ "', '"
							+ selected.get(i).getId()
							+ "');";
				}
				funcs.executeQuery(query1);
				finish();
				return null;
			}
			return "Duplicated Name";
		}

		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if (file_url != null) {
				Toast.makeText(CreateGroupActivity.this, file_url,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
