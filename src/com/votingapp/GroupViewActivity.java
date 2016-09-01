package com.votingapp;

import search.SearchActivity;
import serverconnection.UserFunctions;
import utils.TabsPagerAdapter;
import utils.UserSessionManager;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

@SuppressLint("NewApi")
public class GroupViewActivity extends FragmentActivity implements
		ActionBar.TabListener {
	UserSessionManager session;
	UserFunctions funcs;
	User user;
	Group group;
	private String[] tabs = { "Vote" };
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		@SuppressWarnings("unused")
		MenuInflater inflater = getMenuInflater();
		MenuItem item = menu.findItem(R.id.profile);
		item.setTitle(user.getUsername());
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_view);
		session = new UserSessionManager(getApplicationContext());
		funcs = new UserFunctions(session.getSession());
		user = session.getUserDetails();

		Bundle b = this.getIntent().getExtras();
		group = (Group) b.getSerializable("group");

		String getUsersVote = "SELECT * FROM user, group_vote, vote WHERE gid = '"
				+ group.getId()
				+ "' and group_vote.voteid = vote.vid and user.uid = vote.userid ORDER BY vote.createdAt DESC ";
		String getVotes = "SELECT * FROM user, group_vote, vote WHERE gid = '"
				+ group.getId()
				+ "' and group_vote.voteid = vote.vid and user.uid = vote.userid ORDER BY vote.createdAt DESC ";

		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		actionBar.setIcon(R.drawable.ic_action_group);
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), 1,
				getVotes, getUsersVote);

		viewPager.setAdapter(mAdapter);

		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Adding Tabs
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
		setTitle(group.getName());
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

	/**
	 * On selecting action bar icons
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case R.id.action_search:
			Intent run = new Intent(GroupViewActivity.this, SearchActivity.class);
			startActivity(run);
			finish();
			return true;
		case R.id.profile:
			Intent runProfile = new Intent(GroupViewActivity.this,
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
			Intent runProfil = new Intent(GroupViewActivity.this,
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
								Toast.makeText(GroupViewActivity.this, "Done",
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
							Toast.makeText(GroupViewActivity.this,
									"Incorrect current password",
									Toast.LENGTH_SHORT).show();
						} else if (!newPass.getText().toString()
								.equals(confirmedPass.getText().toString())
								|| newPass.getText().toString().isEmpty()) {
							Toast.makeText(GroupViewActivity.this,
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
									Toast.makeText(GroupViewActivity.this,
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
