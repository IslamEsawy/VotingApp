package vote;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.votingapp.R;
import com.votingapp.User;

public class VoteFragment extends Fragment {
	Vector<Vote> votes;
	Vector<User> users;
	UserSessionManager session;
	UserFunctions funcs;
	Button createVote;
	View rootView;
	User ourUser;
	String getUsersVote, getVotes;

	public VoteFragment(String getUsersVote, String getVotes) {
		this.getUsersVote = getUsersVote;
		this.getVotes = getVotes;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		votes = new Vector<Vote>();
		users = new Vector<User>();
		session = new UserSessionManager(getActivity().getApplicationContext());
		funcs = new UserFunctions(session.getSession());
		ourUser = session.getUserDetails();
		rootView = inflater.inflate(R.layout.activity_vote_fragment, container,
				false);
		createVote = (Button) rootView.findViewById(R.id.createvote);
		createVote.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getActivity(), CreateVoteActivity.class);
				startActivity(i);
			}
		});

		doWork();

		return rootView;
	}

	@Override
	public void onResume() {
		votes = new Vector<Vote>();
		users = new Vector<User>();
		doWork();
		super.onResume();
	}

	private void doWork() {

		populateDataList();
		populateListView();
		registerClickCallback();

	}

	private void populateDataList() {
		Thread t = new Thread() {
			@Override
			public void run() {
				users = funcs.getUsers(getUsersVote);
				votes = funcs.getVotes(getVotes);
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void populateListView() {
		ArrayAdapter<Vote> adapter = new MyListAdapter();
		ListView list = (ListView) rootView.findViewById(R.id.votes);
		list.setAdapter(adapter);
	}

	private void registerClickCallback() {
		ListView list = (ListView) rootView.findViewById(R.id.votes);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked,
					int position, long id) {
				Vote clickedVote = votes.get(position);
				User clickedUser = users.get(position);

				if (clickedVote.getIsAlive() != 1
						&& !clickedUser.getEmail().equals(ourUser.getEmail())) {
					new AlertDialog.Builder(getActivity())
							.setTitle("Error")
							.setMessage("Oops, This vote is finished")
							.setPositiveButton("Return",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
										}
									}).show();
					return;
				}

				if (clickedUser.getEmail().equals(ourUser.getEmail())) {
					Intent m = new Intent(getActivity(), VoteResultActivity.class);
					Bundle b = new Bundle();
					b.putSerializable("vote", clickedVote);
					b.putSerializable("user", clickedUser);
					m.putExtras(b);
					startActivity(m);
				} else {
					Intent m = new Intent(getActivity(), VoteViewActivity.class);
					Bundle b = new Bundle();
					b.putSerializable("vote", clickedVote);
					b.putSerializable("user", clickedUser);
					m.putExtras(b);
					startActivity(m);
				}
			}
		});
	}

	private class MyListAdapter extends ArrayAdapter<Vote> {

		private HashMap<TextView, CountDownTimer> counters;

		public MyListAdapter() {
			super(getActivity(), R.layout.vote_item, votes);
			this.counters = new HashMap<TextView, CountDownTimer>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with (may have been given null)
			View itemView = convertView;
			if (itemView == null)
				itemView = getActivity().getLayoutInflater().inflate(
						R.layout.vote_item, parent, false);

			Vote postedVote = votes.get(position);
			User postedUser = users.get(position);


			TextView nameText = (TextView) itemView
					.findViewById(R.id.textView1);
			nameText.setText(postedUser.getUsername());
			

			TextView qText = (TextView) itemView.findViewById(R.id.textView2);
			qText.setText(postedVote.getVoteQ());

			final TextView condionText = (TextView) itemView
					.findViewById(R.id.textView3);

			CountDownTimer cdt = counters.get(condionText);
			if (cdt != null) {
				cdt.cancel();
				cdt = null;
			}

			Date date = new Date();
			Timestamp now = new Timestamp(date.getTime());
			long diff = postedVote.getEndAt().getTime() - now.getTime();
			if (diff <= 0)
				condionText.setText("Finished");
			else {
				cdt = new CountDownTimer(diff, 1000) {

					public void onTick(long millisUntilFinished) {
						long seconds = millisUntilFinished / 1000;
						long minutes = seconds / 60;
						long hours = minutes / 60;
						long days = hours / 24;
						String time = days + " : " + hours % 24 + " : "
								+ minutes % 60 + " : " + seconds % 60;
						condionText.setText("Remaining: " + time);
					}

					public void onFinish() {
						condionText.setText("Finished");
					}
				};
				counters.put(condionText, cdt);
				cdt.start();
			}
			return itemView;
		}
	}
}
