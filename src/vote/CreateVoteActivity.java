package vote;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import search.SingleCustomAdapter;
import serverconnection.UserFunctions;
import utils.CustomDateTimePicker;
import utils.UserSessionManager;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

import com.votingapp.Group;
import com.votingapp.R;
import com.votingapp.User;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CreateVoteActivity extends Activity {
	UserSessionManager session;
	Vector<EditText> texts = new Vector<EditText>();
	UserFunctions funcs;
	EditText question;
	Button add, post, time;
	RadioGroup type;
	RadioButton typeButton;
	RelativeLayout r2;
	User user;
	Timestamp timeStamp;
	CheckBox chB;
	CustomDateTimePicker custom;
	int i = 0;
	boolean publicType = false;
	SingleCustomAdapter adapter;
	Spinner spinner;
	Group group = null;
	Vector<Group> Grouplist;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		setContentView(R.layout.activity_create_vote);
		session = new UserSessionManager(getApplicationContext());
		funcs = new UserFunctions(session.getSession());
		user = session.getUserDetails();
		Thread t = new Thread() {
			@Override
			public void run() {
				Grouplist = funcs
						.getGroups("select * from `group`,user where adminid = '"
								+ user.getId() + "' and adminid = uid and type = 0");
				Grouplist
						.addAll(funcs
								.getGroups("select `group`.gid,adminid,name,type,email from `group`,`group_user`,user where userid = '"
										+ user.getId()
										+ "' and `group`.gid = `group_user`.gid and adminid = uid and type = 1"));
			}
		};
		try {
			t.start();
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		spinner = (Spinner) findViewById(R.id.spinner1);

		adapter = new SingleCustomAdapter(getApplicationContext(), Grouplist);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view,
					int i, long l) {
				group = (Group) spinner.getSelectedItem();
			}

			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		Date dat = new Date();
		timeStamp = new Timestamp(dat.getTime());
		prepareDialog();

		r2 = (RelativeLayout) findViewById(R.id.r2);
		question = (EditText) findViewById(R.id.editText1);
		type = (RadioGroup) findViewById(R.id.type);

		add = (Button) findViewById(R.id.add);
		post = (Button) findViewById(R.id.post);
		time = (Button) findViewById(R.id.time);
		CheckBox chB = (CheckBox) findViewById(R.id.chb1);

		// Set contact selector

		chB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					spinner.setEnabled(false);
					publicType = true;
				} else {
					spinner.setEnabled(true);
					publicType = false;
				}
			}
		});

		time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				custom.showDialog();
			}
		});

		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText t = new EditText(getBaseContext());
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						(int) LayoutParams.WRAP_CONTENT,
						(int) LayoutParams.WRAP_CONTENT);
				if (i != 0)
					params.addRule(RelativeLayout.BELOW, texts.get(i - 1)
							.getId());
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				t.setId(texts.size() + 1);
				t.setHint("Enter Answer: " + String.valueOf(texts.size() + 1));
				t.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
				t.setTextColor(Color.BLACK);
				t.setLayoutParams(params);
				i++;
				texts.add(t);
				r2.addView(t);
			}
		});

		post.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean d = true;
				Date dat = new Date();
				Timestamp now = new Timestamp(dat.getTime());
				long diff = timeStamp.getTime() - now.getTime();
				if (diff <= 0) {
					Toast.makeText(CreateVoteActivity.this,
							"Please pick a valid end date !!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (question.getText().toString().trim().isEmpty()) {
					Toast.makeText(CreateVoteActivity.this,
							"Please add a non-empty question !!!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				for (int j = 0; j < texts.size(); j++)
					if (texts.get(j).getText().toString().trim().isEmpty())
						d = false;
				if (r2.getChildCount() > 1 && d) {
					if (!publicType && group == null)
						return;
					Thread t = new Thread() {
						int selectedId = type.getCheckedRadioButtonId();
						@Override
						public void run() {
							typeButton = (RadioButton) findViewById(selectedId);
							int ind = 0, inGroup = 0;
							if (typeButton.getText().toString()
									.equals("Single"))
								ind = 1;
							if (!publicType)
								inGroup = 1;
							Vote v = funcs.registerVote(user.getId(), question
									.getText().toString(), ind, timeStamp, inGroup);
							int id = v.getVid();
							String query = "";
							for (int j = 0; j < texts.size(); j++) {
								query += "INSERT INTO `vote_contents`(`voteid`, `content`) VALUES ('"
										+ id
										+ "','"
										+ texts.get(j).getText().toString()
										+ "');";

							}
							if (!publicType)
								query += "INSERT INTO `group_vote`(`gid`, `voteid`) VALUES ('"
										+ group.getId() + "','" + id + "');";
							funcs.executeQuery(query);
						}
					};
					t.start();
					try {
						t.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					finish();
				} else
					Toast.makeText(CreateVoteActivity.this,
							"Please add at least 2 non-empty answers !!!",
							Toast.LENGTH_SHORT).show();
			}
		});

	}

	private void prepareDialog() {
		custom = new CustomDateTimePicker(CreateVoteActivity.this,
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
							Toast.makeText(CreateVoteActivity.this,
									"Wrong date", Toast.LENGTH_LONG).show();
						else {
							long seconds = diff / 1000;
							long minutes = seconds / 60;
							long hours = minutes / 60;
							long days = hours / 24;
							String time = days + " Days " + hours % 24
									+ " Hours " + minutes % 60 + " Minutes "
									+ seconds % 60 + " Seconds";

							Toast.makeText(CreateVoteActivity.this, time,
									Toast.LENGTH_LONG).show();
						}
					}

					@Override
					public void onCancel() {

					}

				});

		custom.set24HourFormat(true);
		custom.setDate(Calendar.getInstance());

	}

}
