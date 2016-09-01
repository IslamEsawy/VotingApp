package vote;

import java.io.Serializable;
import java.sql.Timestamp;

@SuppressWarnings("serial")
public class Vote implements Serializable{
	private int vid;
	private int userID;
	private String voteQ;
	private int count;
	private Timestamp createdAt;
	private int multiple;
	private Timestamp endAt;
	private int isAlive;
	
	

	public int getIsAlive() {
		return isAlive;
	}

	public void setIsAlive(int isAlive) {
		this.isAlive = isAlive;
	}

	public Vote(int vid, int userID, String voteQ, int count, Timestamp c, Timestamp e, int mul, int isAlive) {
		super();
		this.vid = vid;
		this.userID = userID;
		this.voteQ = voteQ;
		this.count = count;
		this.createdAt = c;
		this.multiple = mul;
		this.endAt = e;
		this.isAlive = isAlive;
	}


	public Timestamp getEndAt() {
		return endAt;
	}

	public void setEndAt(Timestamp endAt) {
		this.endAt = endAt;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Vote() {
	}

	public int getVid() {
		return vid;
	}

	public void setVid(int vid) {
		this.vid = vid;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getVoteQ() {
		return voteQ;
	}

	public void setVoteQ(String voteQ) {
		this.voteQ = voteQ;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getMultiple() {
		return multiple;
	}

	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}

}
