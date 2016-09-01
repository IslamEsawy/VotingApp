package utils;

import vote.Vote;
import vote.VoteResultActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.votingapp.R;

public class NotificationService extends Service {

	int numMessages = 0;
	Vote vote;
	int notifyID = 9001;
	public NotificationService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent == null)
			return;
		Bundle b = intent.getExtras();
		vote = (Vote) b.getSerializable("vote");
		Intent resultIntent = new Intent(this, VoteResultActivity.class);
		Bundle f = new Bundle();
		f.putSerializable("vote", vote);
		resultIntent.putExtras(f);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mNotifyBuilder;
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Sets an ID for the notification, so it can be updated
		mNotifyBuilder = new NotificationCompat.Builder(this)
				.setContentTitle("Vote Finished")
				.setContentText("Your vote is finished.")
				.setSmallIcon(R.drawable.ic_launcher);
		// Set pending intent
		mNotifyBuilder.setContentIntent(resultPendingIntent);
		// Set Vibrate, Sound and Light
		int defaults = 0;
		defaults = defaults | Notification.DEFAULT_LIGHTS;
		defaults = defaults | Notification.DEFAULT_VIBRATE;
		defaults = defaults | Notification.DEFAULT_SOUND;
		mNotifyBuilder.setDefaults(defaults);
		// Set the content for Notification
		mNotifyBuilder.setContentText("Press to view results.");
		// Set autocancel
		mNotifyBuilder.setAutoCancel(true);
		// Post a notification
		mNotificationManager.notify(notifyID, mNotifyBuilder.build());
		notifyID++;
	}

	@Override
	public void onDestroy() {
	}

}
