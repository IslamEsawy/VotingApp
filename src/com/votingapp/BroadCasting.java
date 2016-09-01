package com.votingapp;

import java.util.Vector;

import serverconnection.UserFunctions;
import utils.NotificationService;
import utils.UserSessionManager;
import vote.Vote;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BroadCasting extends BroadcastReceiver {
	static int noOfTimes = 0;
	private UserSessionManager session;
	private UserFunctions funcs;
	Vector<Vote> votes;
	User loggedIn;

	// Method gets called when Broad Case is issued from MainActivity for every
	// 10 seconds
	@Override
	public void onReceive(final Context context, Intent intent) {
		session = new UserSessionManager(MyApplication.getContext());
		funcs = new UserFunctions(session.getSession());
		votes = new Vector<Vote>();
		Bundle b = intent.getExtras();
		loggedIn = (User) b.getSerializable("user");

		Thread t = new Thread() {
			@Override
			public void run() {
				votes = funcs.getVotes("select * from vote where userid = '"
						+ loggedIn.getId() + "' and vcount = 1 limit 1");
				if (!votes.isEmpty())
					funcs.executeQuery("update vote set vcount = 0 where vcount = 1 and userid = '"
							+ loggedIn.getId() + "' limit 1");
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.e("test", "" + votes.size() + "  " + loggedIn.getId());
		if (!votes.isEmpty()) {
			for (Vote vote : votes) {
				Intent intnt = new Intent(context, NotificationService.class);
				Bundle f = new Bundle();
				f.putSerializable("vote", vote);
				intnt.putExtras(f);
				context.startService(intnt);
			}
		}

		noOfTimes++;
	}

}
