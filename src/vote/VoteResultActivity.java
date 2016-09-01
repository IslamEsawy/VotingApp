package vote;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import serverconnection.UserFunctions;
import utils.CustomDateTimePicker;
import utils.UserSessionManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.votingapp.R;
import com.votingapp.User;

@SuppressLint("NewApi")
public class VoteResultActivity extends Activity {
	Vote vote;
	User loggedIn;
	UserSessionManager session;
	VoteAnswers va;
	ProgressBar pB;
	UserFunctions funcs;
	Vector<String> ans;
	TextView tot;
	Vector<Integer> ansC;
	double total = 0;
	private ProgressDialog pDialog;
	Timestamp timeStamp;
	CustomDateTimePicker custom;
	CountDownTimer countDown;
	boolean finished = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ans = new Vector<String>();
		ansC = new Vector<Integer>();
		session = new UserSessionManager(getApplicationContext());
		funcs = new UserFunctions(session.getSession());
		Bundle b = this.getIntent().getExtras();
		loggedIn = session.getUserDetails();
		vote = (Vote) b.getSerializable("vote");
		setContentView(R.layout.activity_vote_result);

		ActionBar actionBar = getActionBar();
		actionBar.hide();

		setData();
		setDate();
		doWork();

	}

	private void setDate() {
		TextView end = (TextView) findViewById(R.id.endDate);
		end.setText("To:\t\t" + vote.getEndAt().toString());
		final TextView duration = (TextView) findViewById(R.id.duration);

		Date date = new Date();
		Timestamp now = new Timestamp(date.getTime());
		long diff = vote.getEndAt().getTime() - now.getTime();
		Log.e("time", "" + diff);
		if (diff <= 0) {
			duration.setText("Finished");
			finished = true;
		} else {
			countDown = new CountDownTimer(diff, 1000) {
				public void onTick(long millisUntilFinished) {
					long seconds = millisUntilFinished / 1000;
					long minutes = seconds / 60;
					long hours = minutes / 60;
					long days = hours / 24;
					String time = days + " : " + hours % 24 + " : " + minutes
							% 60 + " : " + seconds % 60;
					duration.setText("Remaining: " + time);
				}

				public void onFinish() {
					duration.setText("Finished");
					finished = true;
				}
			};
			countDown.start();
		}

	}

	private void setData() {
		TextView question = (TextView) findViewById(R.id.question);
		question.setText(vote.getVoteQ());

		tot = (TextView) findViewById(R.id.total);
		TextView start = (TextView) findViewById(R.id.startDate);
		start.setText("From:\t\t" + vote.getCreatedAt().toString());
		TextView end = (TextView) findViewById(R.id.endDate);
		end.setText("To:\t\t" + vote.getEndAt().toString());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.vote, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		if (item.getItemId() == R.id.postpone) {
			prepareDialog();
			custom.showDialog();
			return true;
		}
		return false;
	}

	private void prepareDialog() {
		custom = new CustomDateTimePicker(VoteResultActivity.this,
				new CustomDateTimePicker.ICustomDateTimeListener() {
					@Override
					public void onSet(Dialog dialog, Calendar calendarSelected,
							Date dateSelected, int year, String monthFullName,
							String monthShortName, int monthNumber, int date,
							String weekDayFullName, String weekDayShortName,
							int hour24, int hour12, int min, int sec,
							String AM_PM) {

						if (monthNumber == 13)
							monthNumber = 1;
						String timeAndDate = String.valueOf(year) + "-"
								+ String.format("%02d", monthNumber) + "-"
								+ String.format("%02d", date) + " "
								+ String.format("%02d", hour24) + ":"
								+ String.format("%02d", min) + ":"
								+ String.format("%02d", sec) + ".0";
						timeStamp = Timestamp.valueOf(timeAndDate);

						Date dat = new Date();
						Timestamp now = new Timestamp(dat.getTime());
						long diff = timeStamp.getTime() - now.getTime();

						if (diff <= 0)
							Toast.makeText(VoteResultActivity.this,
									"Wrong date", Toast.LENGTH_LONG).show();
						else {
							long seconds = diff / 1000;
							long minutes = seconds / 60;
							long hours = minutes / 60;
							long days = hours / 24;
							String time = days + " Days " + hours % 24
									+ " Hours " + minutes % 60 + " Minutes "
									+ seconds % 60 + " Seconds";

							Toast.makeText(VoteResultActivity.this, time,
									Toast.LENGTH_LONG).show();

							new PostponeAttempt().execute();
						}
					}

					@Override
					public void onCancel() {

					}

				});

		custom.set24HourFormat(true);
		custom.setDate(Calendar.getInstance());

	}

	class PostponeAttempt extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(VoteResultActivity.this);
			pDialog.setMessage("Postpone Attempting ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			funcs.executeQuery("UPDATE `vote` SET `isAlive`= 1,`endAt`= '"
					+ timeStamp + "' WHERE `vid` = '" + vote.getVid() + "'");
			vote.setEndAt(timeStamp);
			vote.setIsAlive(1);
			return null;
		}

		protected void onPostExecute(String n) {
			pDialog.dismiss();
			if (!finished)
				countDown.cancel();
			setDate();
		}

	}

	private void doWork() {
		getContents();
		populateListView();
	}

	private void getContents() {
		Thread t = new Thread() {
			@Override
			public void run() {
				String query = "SELECT * from vote_contents where voteid = '"
						+ vote.getVid() + "'";
				va = funcs.getVoteContents(query);
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (Map.Entry<String, Integer> entry : va.getAnswers().entrySet()) {
			total += entry.getValue();
			ans.add(entry.getKey());
			ansC.add(entry.getValue());
		}
		tot.setText("Total of " + String.valueOf((int) total) + " voters");
		sort(ans, ansC);
	}

	private void populateListView() {
		ArrayAdapter<String> adapter = new MyListAdapter();
		ListView list = (ListView) findViewById(R.id.lv);
		list.setAdapter(adapter);
	}

	private void sort(Vector<String> names, Vector<Integer> grade) {
		boolean swapped;
		do {
			swapped = false;
			for (int i = 0; i < names.size() - 1; i++) {
				if (grade.get(i) < grade.get(i + 1)) {
					String temp = names.get(i + 1);
					names.set(i + 1, names.get(i));
					names.set(i, temp);

					int temp1 = grade.get(i + 1);
					grade.set(i + 1, grade.get(i));
					grade.set(i, temp1);

					swapped = true;
				}
			}
		} while (swapped);
	}

	private class MyListAdapter extends ArrayAdapter<String> {
		public MyListAdapter() {
			super(VoteResultActivity.this, R.layout.content_item, ans);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with (may have been given null)
			View itemView = convertView;
			if (itemView == null)
				itemView = getLayoutInflater().inflate(R.layout.content_item,
						parent, false);

			TextView nameText = (TextView) itemView
					.findViewById(R.id.textView1);
			nameText.setText(ans.get(position));

			pB = (ProgressBar) itemView.findViewById(R.id.progressBar1);
			pB.setProgress((int) ((Double.valueOf(ansC.get(position)) / total) * 100));

			TextView presstext = (TextView) itemView
					.findViewById(R.id.textView2);
			presstext.setText(String.valueOf((int) ((Double.valueOf(ansC
					.get(position)) / total) * 100)) + "%");

			return itemView;
		}
	}
}
