package vote;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.votingapp.MyApplication;
import com.votingapp.R;
import com.votingapp.User;

@SuppressLint("NewApi")
public class VoteViewActivity extends Activity {

	private UserSessionManager session;
	private UserFunctions funcs;
	@SuppressWarnings("unused")
	private User loggedIn, user;

	private CheckBox[] cB;
	private RadioGroup rg;
	private RadioButton[] rb;
	private Vote vote;
	private Button submit;
	private TextView username, question;
	private LinearLayout l2;
	private VoteAnswers va;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		session = new UserSessionManager(MyApplication.getContext());
		funcs = new UserFunctions(session.getSession());
		Bundle b = this.getIntent().getExtras();
		loggedIn = session.getUserDetails();
		vote = (Vote) b.getSerializable("vote");
		user = (User) b.getSerializable("user");
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		setContentView(R.layout.activity_vote);

		l2 = (LinearLayout) findViewById(R.id.l2);
		username = (TextView) findViewById(R.id.username);
		question = (TextView) findViewById(R.id.question);

		username.setText(user.getUsername());
		question.setText(vote.getVoteQ());

		submit = (Button) findViewById(R.id.submit);

		setDate();

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

		if (vote.getMultiple() == 0) {
			cB = new CheckBox[va.getAnswers().size()];
			createCheckBoxes();
		} else {
			rg = new RadioGroup(this); // create the RadioGroup
			rb = new RadioButton[va.getAnswers().size()];
			rg.setOrientation(RadioGroup.VERTICAL);// or RadioGroup.VERTICAL
			createRadioGroup();
			rb[0].setChecked(true);
		}

		submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Thread t = new Thread() {
					@Override
					public void run() {
						if (vote.getMultiple() == 1) {
							int selectedId = rg.getCheckedRadioButtonId();
							RadioButton rButton = (RadioButton) findViewById(selectedId);
							String query = "UPDATE `vote_contents` SET contentcount=contentcount+1 WHERE voteid = '"
									+ vote.getVid()
									+ "' and content = '"
									+ rButton.getText().toString() + "' ";
							funcs.executeQuery(query);
							finish();
						} else {
							String query = "UPDATE `vote_contents` SET contentcount=contentcount+1 WHERE voteid = '"
									+ vote.getVid() + "' and (";
							for (int i = 0; i < va.getAnswers().size(); i++) {
								if (cB[i].isChecked())
									query += " content = '"
											+ cB[i].getText().toString()
											+ "' or ";
							}
							if (query.lastIndexOf("(") != query.length() - 1) {
								query = query.substring(0, query.length() - 3)
										+ ")";
								funcs.executeQuery(query);
								finish();
							} else {
								runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(VoteViewActivity.this,
												"Please pick an answer!!!",
												Toast.LENGTH_SHORT).show();
									}
								});
							}
						}
					}
				};
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});

	}

	private void setDate(){
		final TextView duration = (TextView) findViewById(R.id.duration);

		Date date = new Date();
		Timestamp now = new Timestamp(date.getTime());
		long diff = vote.getEndAt().getTime() - now.getTime();
		Log.e("time", "" + diff);
		if (diff <= 0)
			duration.setText("Finished");
		else {
			new CountDownTimer(diff, 1000) {
				public void onTick(long millisUntilFinished) {
					long seconds = millisUntilFinished / 1000;
					long minutes = seconds / 60;
					long hours = minutes / 60;
					long days = hours / 24;
					String time = days + " : " + hours % 24 + " : " + minutes % 60
							+ " : " + seconds % 60;
					duration.setText("Remaining: " + time);
				}

				public void onFinish() {
					duration.setText("Finished");
					submit.setEnabled(false);
				}
			}.start();
		}

	}

	private void createRadioGroup() {
		int i = 0;
		for (Map.Entry<String, Integer> entry : va.getAnswers().entrySet()) {
			rb[i] = new RadioButton(this);
			rb[i].setText(entry.getKey());
			rb[i].setId(i + 100);
			rg.addView(rb[i]);
			i++;
		}
		l2.addView(rg);// you add the whole RadioGroup to the layout
	}

	private void createCheckBoxes() {
		int i = 0;
		for (Map.Entry<String, Integer> entry : va.getAnswers().entrySet()) {
			cB[i] = new CheckBox(this);
			cB[i].setText(entry.getKey());
			cB[i].setId(i + 100);
			l2.addView(cB[i]);// you add the whole RadioGroup to the layout
			i++;
		}
	}

}
