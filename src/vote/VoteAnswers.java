package vote;

import java.util.HashMap;

public class VoteAnswers {
	private int voteid;
	private HashMap<String, Integer> answers;

	VoteAnswers() {
	}

	public int getVoteid() {
		return voteid;
	}

	public void setVoteid(int voteid) {
		this.voteid = voteid;
	}

	public HashMap<String, Integer> getAnswers() {
		return answers;
	}

	public void setAnswers(HashMap<String, Integer> answers) {
		this.answers = answers;
	}

	public VoteAnswers(int voteid, HashMap<String, Integer> answers) {
		super();
		this.voteid = voteid;
		this.answers = answers;
	}

}
