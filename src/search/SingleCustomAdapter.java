package search;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.votingapp.Group;
import com.votingapp.R;

public class SingleCustomAdapter extends BaseAdapter {
	private Context context;
	private Vector<Group> Grouplist;

	@SuppressLint("UseSparseArrays")
	public SingleCustomAdapter(Context context, Vector<Group> Grouplist) {
		this.context = context;
		this.Grouplist = Grouplist;
	}

	@Override
	public int getCount() {
		return Grouplist.size();
	}

	@Override
	public Object getItem(int position) {
		return Grouplist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Grouplist.indexOf(getItem(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		convertView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.group_item, parent, false);

			Group postedGroup = Grouplist.get(position);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(postedGroup.getName());

			TextView admin = (TextView) convertView.findViewById(R.id.admin);
			admin.setText(postedGroup.getEmail());

			TextView type = (TextView) convertView.findViewById(R.id.type);
			if (postedGroup.getType() == 1) {
				type.setText("Anyone");
			} else
				type.setText("Only Me");

		}
		return convertView;
	}

}
