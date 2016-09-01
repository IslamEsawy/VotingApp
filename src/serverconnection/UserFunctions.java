package serverconnection;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import vote.Vote;
import vote.VoteAnswers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.votingapp.Group;
import com.votingapp.User;

public class UserFunctions {

	private String URL;

	private static String login_tag = "login";
	private static String execute_tag = "execute";
	private static String register_tag = "register";
	private static String registerVote_tag = "registerVote";
	private static String getU_tag = "getUsers";
	private static String getV_tag = "getVotes";
	private static String follow_tag = "follow";
	private static String getAnswers_tag = "getAnswers";
	private static String group_tag = "insertGroup";
	private static String getgroup_tag = "getPostableGroups";

	// constructor
	public UserFunctions(String connection) {
		URL = connection;
	}

	/**
	 * function make Login Request
	 * 
	 * @param email
	 * @param password
	 * */
	public User loginUser(String email, String password) {
		// Building Parameters
		User user = null;
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("tag", login_tag));
		postParameters.add(new BasicNameValuePair("email", email));
		postParameters.add(new BasicNameValuePair("password", password));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, postParameters);
			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String userInfo = responseObj.getString("userInfo");
				user = gson.fromJson(userInfo, User.class);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * function make Login Request
	 * 
	 * @param name
	 * @param email
	 * @param password
	 * */
	public User registerUser(String name, String email, String password,
			String gender) {
		// Building Parameters
		User user = null;
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", register_tag));
		params.add(new BasicNameValuePair("username", name));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("gender", gender));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			// response = request.sendHttpRequest(URL, params);
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String userInfo = responseObj.getString("userInfo");
				user = gson.fromJson(userInfo, User.class);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return user;
	}

	public Vote registerVote(int x, String e, int h, Timestamp t, int inGroup) {
		// Building Parameters
		Vote v = null;
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", registerVote_tag));
		params.add(new BasicNameValuePair("userid", String.valueOf(x)));
		params.add(new BasicNameValuePair("voteQ", e));
		params.add(new BasicNameValuePair("time", t.toString()));
		params.add(new BasicNameValuePair("multiple", String.valueOf(h)));
		params.add(new BasicNameValuePair("inGroup", String.valueOf(inGroup)));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);

			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String voteInfo = responseObj.getString("voteInfo");
				v = gson.fromJson(voteInfo, Vote.class);
			}
		} catch (JSONException d) {
			d.printStackTrace();
		}
		return v;
	}

	public Vector<User> getUsers(String query) {
		// Building Parameters
		Vector<User> list = new Vector<User>();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", getU_tag));
		params.add(new BasicNameValuePair("query", query));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String userInfo = responseObj.getString("userInfo");
				list = gson.fromJson(userInfo, new TypeToken<Vector<User>>() {
				}.getType());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public Vector<Vote> getVotes(String query) {
		// Building Parameters
		Vector<Vote> list = new Vector<Vote>();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", getV_tag));
		params.add(new BasicNameValuePair("query", query));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String userInfo = responseObj.getString("userInfo");
				list = gson.fromJson(userInfo, new TypeToken<Vector<Vote>>() {
				}.getType());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	@SuppressWarnings("finally")
	public boolean executeQuery(String query) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", execute_tag));
		params.add(new BasicNameValuePair("query", query));
		String response = null;
		JSONObject responseObj = null;
		boolean success = false;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			success = (responseObj.getBoolean("success"));
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			return success;
		}
	}

	@SuppressWarnings("finally")
	public boolean isFollowed(int userId, int followerId) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", follow_tag));
		String query = "select * from user_follower where userid = '"
				+ String.valueOf(userId) + "' and followerid = '"
				+ String.valueOf(followerId) + "' ";
		params.add(new BasicNameValuePair("query", query));
		String response = null;
		JSONObject responseObj = null;
		boolean success = false;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			success = (responseObj.getBoolean("success"));
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			return success;
		}
	}

	public VoteAnswers getVoteContents(String query) {
		VoteAnswers ret = null;
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", getAnswers_tag));
		params.add(new BasicNameValuePair("query", query));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String voteAns = responseObj.getString("voteAns");
				ret = gson.fromJson(voteAns, new TypeToken<VoteAnswers>() {
				}.getType());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public Group insertGroup(String query1, String name) {

		Group group = null;
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", group_tag));
		params.add(new BasicNameValuePair("query1", query1));
		params.add(new BasicNameValuePair("name", name));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String groupInfo = responseObj.getString("groupInfo");
				group = gson.fromJson(groupInfo, Group.class);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return group;
	}

	public Vector<Group> getGroups(String query) {
		// Building Parameters
		Vector<Group> list = new Vector<Group>();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", getgroup_tag));
		params.add(new BasicNameValuePair("query", query));
		String response = null;
		JSONObject responseObj = null;
		try {
			JSONRequest request = new JSONRequest();
			response = request.sendHttpRequest(URL, params);
			responseObj = new JSONObject(response);
			boolean success = responseObj.getBoolean("success");
			if (success) {
				Gson gson = new Gson();
				String userInfo = responseObj.getString("userInfo");
				list = gson.fromJson(userInfo, new TypeToken<Vector<Group>>() {
				}.getType());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

}
