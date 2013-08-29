package com.example.mysql_tester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.mysql_tester.library.*;

public class LoginActivity extends SherlockActivity {
	ActionBar actionBar;
	EditText edt_account,edt_password;
	Button btnToRegister;
	TextView txt_error;
	private ProgressDialog pDialog;
	//JSON Node 名稱
	private static String KEY_SUCCESS = "success";
	private static String KEY_NICKNAME = "nick_name";
	private static String KEY_EMAIL = "email";
	private static String KEY_UID = "uID";
	int userHasLogined = 0;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);    //將Layout 設定成Login那頁
		//ActionBar
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		//由介面取得元件
		edt_account = (EditText)findViewById(R.id.login_account);
		edt_password = (EditText)findViewById(R.id.login_password);
		btnToRegister = (Button)findViewById(R.id.btn_toRegister);
		txt_error = (TextView)findViewById(R.id.login_error);
		//設定監聽
		btnToRegister.setOnClickListener(listener);
		edt_password.setOnEditorActionListener(done_listener);
	}
	
	private OnEditorActionListener done_listener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				String email = edt_account.getText().toString();
				Boolean emailBoolean = isEmailValid(email);
				if(edt_account.getText().toString().equals("") ||
						edt_password.getText().toString().equals("") ||
						emailBoolean == false) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_plzCheckEmailFormat), Toast.LENGTH_SHORT).show();
				}
				else {
					new doTheLoginWork().execute();
				}
			}
			return false;
		}
	};

	private Button.OnClickListener listener = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent go_register = new Intent(LoginActivity.this,RegisterActivity.class);
			startActivity(go_register);
		}
	};

	class doTheLoginWork extends AsyncTask<String, String, String> {
		//在背景開始讀取之前，顯示Progress Dialog(對話方塊)
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage(getResources().getString(R.string.login_nowLogining));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String email = edt_account.getText().toString();
			String password = edt_password.getText().toString();
			UserFunctions userFunctions = new UserFunctions();
			JSONObject json = userFunctions.loginUser(email, password);
			//Log.e("json  user detail = ", json.toString());    //Log uses
			//檢查登入的Response
			try {
				String success = json.getString(KEY_SUCCESS);
				userHasLogined = Integer.parseInt(success);
				Log.e("success = ", success);
				if (Integer.parseInt(success) == 1) {
					//使用者成功登入了
					//SQLite 儲存使用者的詳細資料
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					JSONObject json_user = json.getJSONObject("user");
					//Log.e("json user data = ", json_user.toString());
					//清除資料庫先前的所有資料
					userFunctions.logoutUser(getApplicationContext());
					db.addUser(json_user.getString(KEY_NICKNAME), json_user.getString(KEY_EMAIL), json_user.getString(KEY_UID));
					//開啟登入後視窗
					Intent go_item_list = new Intent(LoginActivity.this,ItemList.class);
					//在開啟登入視窗之前先清除掉其他的View
					go_item_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(go_item_list);
					finish();
				}
				else {
					//Error in Login
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		//完成背景作業後，將對話框關閉
		protected void onPostExecute(String file_url) {
			//忽略掉對話框
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (userHasLogined == 1) {
						txt_error.setText("");
						UserFunctions userFunctions = new UserFunctions();
						String nick_name = userFunctions.getUserName(getApplicationContext());
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_welcomeMessage1) +nick_name +getResources().getString(R.string.login_welcomeMessage2),Toast.LENGTH_SHORT).show();
						//int uid = userFunctions.getUserUid(getApplicationContext());
						//String useremail = userFunctions.getUserEmail(getApplicationContext());
						//Toast.makeText(getApplicationContext(), "你好" +nick_name +"，歡迎登入！ " +"\n uid: " +uid +"\n email:" +useremail, Toast.LENGTH_SHORT).show();
					}
					else {
						txt_error.setText(getResources().getString(R.string.login_errorMessage));
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/*ActionBar*/
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent go_back = new Intent(LoginActivity.this,ItemList.class);
			go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(go_back);
			finish();
			break;
		case R.id.login_menuButton:
			String email = edt_account.getText().toString();
			Boolean emailBoolean = isEmailValid(email);
			if(edt_account.getText().toString().equals("") ||
					edt_password.getText().toString().equals("") ||
					emailBoolean == false) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_plzCheckEmailFormat), Toast.LENGTH_SHORT).show();
			}
			else {
				new doTheLoginWork().execute();
			}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	//檢查E-mail格式是否正確(From Stack Overflow / By.Andy)
	public static boolean isEmailValid(String email) {
		boolean isValid = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	/*To fix the IllegalArgumentException*/
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		try{
			pDialog.dismiss();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
