package utils;

import vote.VoteFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.votingapp.GroupsViewFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {
	int count;
	String getVotes, getUsersVote;

	public TabsPagerAdapter(FragmentManager fm, int count, 
			String getVotes,  String getUsersVote) {
		super(fm);
		this.count = count;
		this.getVotes = getVotes;
		this.getUsersVote = getUsersVote;
	}

	@Override
	public Fragment getItem(int index) {
		switch (index) {
		case 0:
			return new VoteFragment(getUsersVote, getVotes);
		case 1:
			return new GroupsViewFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return count;
	}

}