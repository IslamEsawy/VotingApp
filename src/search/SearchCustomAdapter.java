package search;
import java.util.Vector;

import serverconnection.UserFunctions;
import utils.UserSessionManager;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.votingapp.MyApplication;
import com.votingapp.R;
import com.votingapp.User;


public class SearchCustomAdapter extends BaseAdapter implements Filterable {
	private UserFunctions funcs;
	private UserSessionManager session;
	private Context context;
	private Vector<User> userlist;
	private Vector<User> mStringFilterList;
	private ValueFilter valueFilter;

    public SearchCustomAdapter(Context context , Vector<User> userlist) {
        this.context = context;
        this.userlist = userlist;
        mStringFilterList = userlist;
        session = new UserSessionManager(MyApplication.getContext());
		funcs = new UserFunctions(session.getSession());
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
    public View getView(int position , View convertView , ViewGroup parent ) {

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
            TextView name_tv = (TextView) convertView.findViewById(R.id.name);
            TextView email_tv = (TextView) convertView.findViewById(R.id.email);
            
            User user = userlist.get(position);
            name_tv.setText(user.getUsername());
            email_tv.setText(user.getEmail());
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null)
            valueFilter = new ValueFilter();
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
            	String sql1 = "select * from user where username like '%" + constraint.toString() + "%'";
                Vector<User> filterList = userlist = funcs.getUsers(sql1);
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;
        }

        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint,
                FilterResults results) {
        	userlist = (Vector<User>) results.values;
            notifyDataSetChanged();
        }

    }

}
