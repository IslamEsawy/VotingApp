package com.votingapp;

import java.util.Vector;

import search.SearchActivity;
import serverconnection.UserFunctions;
import utils.TabsPagerAdapter;
import utils.UserSessionManager;
import vote.Vote;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

@SuppressLint("NewApi")
public class ProfileActivity extends FragmentActivity implements
		ActionBar.TabListener {
	UserSessionManager session;
	UserFunctions funcs;
	Vector<Vote> votes;
	User loggedIn;
	private User user;
	private ToggleButton following;
	int x = 1;
	private String[] tabs = { "Vote" };
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		@SuppressWarnings("unused")
		MenuInflater inflater = getMenuInflater();
		MenuItem item = menu.findItem(R.id.profile);
		item.setTitle(loggedIn.getUsername());
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		session = new UserSessionManager(getApplicationContext());
		funcs = new UserFunctions(session.getSession());
		votes = new Vector<Vote>();
		loggedIn = session.getUserDetails();
		Bundle b = this.getIntent().getExtras();
		user = (User) b.getSerializable("user");

		setTitle("");

		following = (ToggleButton) findViewById(R.id.follow);
		TextView email = (TextView) findViewById(R.id.textView2);
		email.setText(user.getEmail());
		TextView name = (TextView) findViewById(R.id.textView1);
		name.setText(user.getUsername());

		if (user.getEmail().equals(loggedIn.getEmail())) {
			getWindow().getDecorView().findViewById(android.R.id.content);
			following.setVisibility(View.INVISIBLE);
			x = 2;
		}
		Thread t = new Thread() {
			@Override
			public void run() {
				if (funcs.isFollowed(user.getId(), loggedIn.getId()))
					following.setChecked(true);
			}
		};
		try {
			t.start();
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		actionBar.setIcon(R.drawable.ic_launcher);
		doWork();
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		following
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Thread t = null;
						if (isChecked) {
							t = new Thread() {
								@Override
								public void run() {
									funcs.executeQuery("insert into user_follower (`userid`, `followerid`)values ( '"
											+ user.getId()
											+ "' , '"
											+ loggedIn.getId() + "')");
								}
							};

						} else
							t = new Thread() {
								@Override
								public void run() {
									funcs.executeQuery("delete from user_follower where userid = '"
											+ user.getId()
											+ "' and followerid = '"
											+ loggedIn.getId() + "' ");
								}
							};
						t.start();
						try {
							t.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// ProfileActivity.this.recreate();
						doWork();
					}
				});
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	void doWork() {

		String getUsersVote, getVotes;
		if (x == 2) {
			getVotes = "SELECT * FROM user, vote WHERE vote.userid = user.uid and vote.userid = '"
					+ loggedIn.getId() + "' ORDER BY createdAt DESC";
			getUsersVote = getVotes;
		} else {
			getVotes = "SELECT * FROM user, user_follower, vote WHERE '"
					+ loggedIn.getId()
					+ "' = user_follower.followerid AND user.uid = vote.userid AND vote.inGroup = 0 AND user.uid = user_follower.userid and user.uid = '"
					+ user.getId() + "' ORDER BY vote.createdAt DESC";
			getUsersVote = getVotes;

		}

		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), 1,
				getVotes, getUsersVote);

		viewPager.setAdapter(mAdapter);

		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	}

	/**
	 * On selecting action bar icons
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case R.id.action_search:
			Intent run = new Intent(ProfileActivity.this, SearchActivity.class);
			startActivity(run);
			finish();
			return true;
		case R.id.profile:
			Intent runProfile = new Intent(ProfileActivity.this,
					ProfileActivity.class);
			Bundle b = new Bundle();
			b.putSerializable("user", user);
			runProfile.putExtras(b);
			startActivity(runProfile);
			finish();
			return true;
		case R.id.action_logout:
			session.logoutUser();
			Intent i = new Intent(getApplicationContext(), LoginActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			destoryAlarm();
			finish();
			return true;
		case R.id.action_change_password:
			changePasswordDialog();
			return true;
		case R.id.create_group:
			Intent runProfil = new Intent(ProfileActivity.this,
					CreateGroupActivity.class);
			startActivity(runProfil);
			finish();
			return true;
		case R.id.change_server:
			changeServerDialog();
			return true;
		case R.id.reset:
			reset();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("InflateParams")
	private void reset() {
		LayoutInflater li = LayoutInflater.from(this);
		final View promptsView = li.inflate(R.layout.reset_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(promptsView);
		final RadioGroup type = (RadioGroup) promptsView.findViewById(R.id.type);
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Execute",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								int selectedId = type.getCheckedRadioButtonId();
								RadioButton typeButton = (RadioButton) promptsView.findViewById(selectedId);
								final String query;
								if (typeButton.getText().toString()
										.equals("Delete all votes")) {
									query = "delete from `group_vote`;delete from `vote_contents`;delete from `vote`";
								} else {
									query = "delete from `group_user`;delete from `vote_contents` where `voteid` in (select `voteid` from `vote` where `ingroup` = 1);delete from `vote` where `inGroup` = 1;delete from `group_vote`;delete from `group`";
								}
								new Thread() {
									@Override
									public void run() {
										funcs.executeQuery(query);
									}
								}.start();
								Toast.makeText(ProfileActivity.this, "Done",
										Toast.LENGTH_LONG).show();
								finish();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void changeServerDialog() {
		session.createSession("");
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		destoryAlarm();
		finish();
	}

	protected void destoryAlarm() {
		Intent intent = new Intent(getApplicationContext(), BroadCasting.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	@SuppressLint("InflateParams")
	private void changePasswordDialog() {
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.change_password_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(promptsView);

		final EditText oldPass, newPass, confirmedPass;
		oldPass = (EditText) promptsView.findViewById(R.id.oldpassword);
		newPass = (EditText) promptsView.findViewById(R.id.newpassword);
		confirmedPass = (EditText) promptsView
				.findViewById(R.id.confirmedpassword);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (!user.getPassword().equals(
								oldPass.getText().toString())) {
							Toast.makeText(ProfileActivity.this,
									"Incorrect current password",
									Toast.LENGTH_SHORT).show();
						} else if (!newPass.getText().toString()
								.equals(confirmedPass.getText().toString())
								|| newPass.getText().toString().isEmpty()) {
							Toast.makeText(ProfileActivity.this,
									"Mismatch or empty passwords",
									Toast.LENGTH_SHORT).show();
						} else {
							final String query = "UPDATE `user` SET `password`='"
									+ newPass.getText().toString()
									+ "' WHERE email = '"
									+ user.getEmail()
									+ "'";
							new Thread() {
								@Override
								public void run() {
									funcs.executeQuery(query);
									user.setPassword(newPass.getText()
											.toString());
									Toast.makeText(ProfileActivity.this,
											"Done", Toast.LENGTH_LONG).show();
								}
							}.start();
						}
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

}
