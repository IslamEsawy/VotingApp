package search;

import java.util.HashMap;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.votingapp.R;
import com.votingapp.User;

public class MultipleCustomAdapter extends BaseAdapter {
	Context context;
	Vector<User> userlist;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Boolean> myChecked = new HashMap<Integer, Boolean>();

	public MultipleCustomAdapter(Context context, Vector<User> userlist) {
		this.context = context;
		this.userlist = userlist;
		for (int i = 0; i < userlist.size(); i++) {
			myChecked.put(i, false);
		}
	}

	public void toggleChecked(int position) {
		if (myChecked.get(position)) {
			myChecked.put(position, false);
		} else {
			myChecked.put(position, true);
		}

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return userlist.size();
	}

	@Override
	public Object getItem(int position) {
		return userlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return userlist.indexOf(getItem(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		convertView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item2, parent, false);
			CheckedTextView name_tv = (CheckedTextView) convertView
					.findViewById(R.id.name);
			TextView email_tv = (TextView) convertView.findViewById(R.id.email);

			User user = userlist.get(position);
			name_tv.setText(user.getUsername());
			email_tv.setText(user.getEmail());
			boolean checked = myChecked.get(position);
			name_tv.setChecked(checked);

		}
		return convertView;
	}

}
