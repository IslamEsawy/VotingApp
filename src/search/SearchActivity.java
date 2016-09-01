package search;

import java.util.Vector;

import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;

import com.votingapp.MyApplication;
import com.votingapp.ProfileActivity;
import com.votingapp.R;
import com.votingapp.User;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class SearchActivity extends Activity implements
		SearchView.OnQueryTextListener {
	
	private UserFunctions funcs;
	private UserSessionManager session;
	private ListView lv;
	private SearchView search_view;

	private Vector<User> userlist;
	private SearchCustomAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // the status bar.
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		setContentView(R.layout.activity_search);
		
		session = new UserSessionManager(MyApplication.getContext());
		funcs = new UserFunctions(session.getSession());

		lv = (ListView) findViewById(R.id.list_view);
		search_view = (SearchView) findViewById(R.id.search_view);

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

		adapter = new SearchCustomAdapter(getApplicationContext(), userlist);
		lv.setAdapter(adapter);

		search_view.setOnQueryTextListener(this);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1,
					int location, long arg3) {
				Intent runProfile = new Intent(SearchActivity.this,
						ProfileActivity.class);
				Bundle b = new Bundle();
				b.putSerializable("user", (User) parent.getItemAtPosition(location));
				runProfile.putExtras(b); //Put your id to your next Inte
				startActivity(runProfile);
				finish();
			}
		});
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		adapter.getFilter().filter(newText);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

}
