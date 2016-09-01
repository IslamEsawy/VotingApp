package com.votingapp;

import java.util.Vector;

import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GroupsViewFragment extends Fragment {
	Vector<Group> groups;
	UserSessionManager session;
	UserFunctions funcs;
	View rootView;
	User ourUser;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		groups = new Vector<Group>();
		session = new UserSessionManager(getActivity().getApplicationContext());
		funcs = new UserFunctions(session.getSession());
		ourUser = session.getUserDetails();
		rootView = inflater.inflate(R.layout.activity_groups_fragment,
				container, false);

		doWork();

		return rootView;
	}

	@Override
	public void onResume() {
		groups = new Vector<Group>();
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
				groups = funcs
						.getGroups("select `group`.gid,adminid,name,type,email from `group`,`group_user`,`user` where userid = '"
								+ ourUser.getId()
								+ "' and `group`.gid = `group_user`.gid and adminid = uid");
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
		ArrayAdapter<Group> adapter = new MyListAdapter();
		ListView list = (ListView) rootView.findViewById(R.id.groups);
		list.setAdapter(adapter);
	}

	private void registerClickCallback() {
		ListView list = (ListView) rootView.findViewById(R.id.groups);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked,
					int position, long id) {
				Group clickedGroup = groups.get(position);

				Intent m = new Intent(getActivity(), GroupViewActivity.class);
				Bundle b = new Bundle();
				b.putSerializable("group", clickedGroup);
				m.putExtras(b);
				startActivity(m);
			}
		});
	}

	private class MyListAdapter extends ArrayAdapter<Group> {

		public MyListAdapter() {
			super(getActivity(), R.layout.group_item, groups);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with (may have been given null)
			View itemView = convertView;
			if (itemView == null)
				itemView = getActivity().getLayoutInflater().inflate(
						R.layout.group_item, parent, false);

			// Find the car to work with.
			Group postedGroup = groups.get(position);

			TextView name = (TextView) itemView.findViewById(R.id.name);
			name.setText(postedGroup.getName());

			TextView admin = (TextView) itemView.findViewById(R.id.admin);
			admin.setText(postedGroup.getEmail());

			TextView type = (TextView) itemView.findViewById(R.id.type);
			if (postedGroup.getType() == 1) {
				type.setText("Anyone");
			} else
				type.setText("Only Me");

			return itemView;
		}
	}

	public GroupsViewFragment() {

	}
	/*
	 * "select `group`.gid,adminid,name,type from `group`,`group_user` where userid = '"
	 * + user.getId() + "' and `group`.gid = `group_user`.gid and type = 1"
	 */

}
