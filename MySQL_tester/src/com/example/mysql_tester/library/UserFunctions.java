package com.example.mysql_tester.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;

public class UserFunctions {
	private JSONParser jsonparser;
	//Testing in localhost using wamp or xampp
	//use http://10.0.0.2/ to connect to your localhost ie http://localhost/
	private static String loginURL = "http://proposal.yuer.tw/foodbook/";
	private static String registerURL = "http://proposal.yuer.tw/foodbook/";
	private static String login_tag = "login";
	private static String register_tag = "register";
	
	//Contructor
	public UserFunctions() {
		jsonparser = new JSONParser();
	}
	//Function make Login Request
	//@param email,@param password
	public JSONObject loginUser(String email,String password) {
		// Buliding Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", login_tag));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("password", password));
		JSONObject json = jsonparser.MemberRequest(loginURL, "POST", params);
		//return json
		//Log.e("JSON login = ", json.toString());
		return json;
	}
	//Function make Login Request
	//@param name,@param email,@param password
	public JSONObject registerUser(String nick_name,String email,String password,String phone,int gender) {
		//Buliding Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", register_tag));
		params.add(new BasicNameValuePair("nick_name", nick_name));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("gender", String.valueOf(gender)));
		JSONObject json = jsonparser.MemberRequest(registerURL, "POST", params);
		//return json
		//Log.e("JSON register = ", json.toString());
		return json;
	}
	
	public String getUserName(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		HashMap<String, String> user = db.getUserDetails();
		String name = user.get("name");
		return name;
	}
	
	public String getUserEmail(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		HashMap<String, String> user = db.getUserDetails();
		String email = user.get("email");
		return email;
	}
	
	public int getUserUid(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		HashMap<String, String> user = db.getUserDetails();
		int uID = Integer.parseInt(user.get("uID"));
		return uID;
	}
	
	//Function get Login status
	public boolean isUserLoggedIn(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		int count = db.getRowCount();
		//Log.e("get row count = ", "" +count);
		if (count > 0) {
			//使用者已經登入
			return true;
		}
		return false;
	}
    //Function to logout user
	public boolean logoutUser(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		db.resetTable();
		return true;
	}
}
