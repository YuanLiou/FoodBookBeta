package com.example.mysql_tester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.mysql_tester.library.*;

public class RegisterActivity extends SherlockActivity {
	ActionBar actionBar;
	EditText edt_account,edt_password,edt_phone,edt_nickName,edt_password_again;
	TextView txt_error;
	RadioGroup radGroup_sex;
	RadioButton radBtn_female,radBtn_male,radBtn_xgen;
	private ProgressDialog pDialog;
	int userHasRegistered = 0;
	int gender = 3;
	//JSON Node 名稱
	private static String KEY_SUCCESS = "success";
	private static String KEY_NICKNAME = "nick_name";
	private static String KEY_EMAIL = "email";
	private static String KEY_UID = "uID";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);    //將Layout 設定成Register那頁
		//從介面取得元件
		edt_account = (EditText)findViewById(R.id.register_account);
		edt_password = (EditText)findViewById(R.id.register_password);
		edt_password_again = (EditText)findViewById(R.id.register_password_again);
		edt_nickName = (EditText)findViewById(R.id.register_nickname);
		edt_phone = (EditText)findViewById(R.id.register_phone);
		txt_error = (TextView)findViewById(R.id.register_error);
		radGroup_sex = (RadioGroup)findViewById(R.id.register_radiogroup);
		radBtn_female = (RadioButton)findViewById(R.id.register_female);
		radBtn_male = (RadioButton)findViewById(R.id.register_male);
		radBtn_xgen = (RadioButton)findViewById(R.id.register_xgender);
		//設定Group 元件的監聽
		radGroup_sex.setOnCheckedChangeListener(radio_listener);
		//actionBar
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}
	
	/*RadioGroup監聽*/
	private RadioGroup.OnCheckedChangeListener radio_listener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.register_female:
				gender = 0;
				break;
            case R.id.register_male:
				gender = 1;
				break;
            case R.id.register_xgender:
	            gender = 2;
	            break;
			}
		}
	};
    
	class doTheRegisterWork extends AsyncTask<String, String, String> {
        //在開始讀取之前，顯示Progress Dialog(對話方塊)
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RegisterActivity.this);
			pDialog.setMessage(getResources().getString(R.string.register_registing));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... params) {
			String nickName = edt_nickName.getText().toString();
			String email = edt_account.getText().toString();  //E-mail即為帳號
			String password = edt_password.getText().toString();
			String phone = edt_phone.getText().toString();
			gender = 0;
			UserFunctions userFunctions = new UserFunctions();
			JSONObject json = userFunctions.registerUser(nickName, email, password, phone, gender);
			//Log.e("Register Json = ", json.toString());
			//檢查註冊時的Response
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					String res = json.getString(KEY_SUCCESS);
					userHasRegistered = Integer.parseInt(res);
					if (Integer.parseInt(res) == 1) {
						//使用者成功註冊了
						//用SQLite儲存所有的使用者資料
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						JSONObject json_user = json.getJSONObject("user");
						//清除所有資料庫裡面的舊資料(進行登入動作？)
						userFunctions.logoutUser(getApplicationContext());
						db.addUser(json_user.getString(KEY_NICKNAME), json_user.getString(KEY_EMAIL), json_user.getString(KEY_UID));
						//開啟登入後視窗
						Intent go_Item_list = new Intent(RegisterActivity.this,ItemList.class);
						//開啟之前清除其他所有View
						go_Item_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(go_Item_list);
						finish();
					}
					else {
						//註冊時出了問題
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		//完成背景作業後，防止對話框跳出來
		protected void onPostExecute(String file_url) {
			//忽略掉對話框
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (userHasRegistered == 1) {
						txt_error.setText("");
					}
					else {
						txt_error.setText(getResources().getString(R.string.register_errorOccured));
					}
				}
			});
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.register, menu);
		return true;
	}
	/*ActionBar*/
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.register_menuButton: {
			new AlertDialog.Builder(RegisterActivity.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.register_alertComfirmMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String email = edt_account.getText().toString();
					Boolean emailBoolean = isEmailValid(email);
					String password1 = edt_password.getText().toString();
					String password2 = edt_password_again.getText().toString();
					if (edt_account.getText().toString().equals("")||
							edt_password.getText().toString().equals("")||
							edt_password_again.getText().toString().equals("")||
							edt_nickName.getText().toString().equals("")||
							edt_phone.getText().toString().equals("")||
							emailBoolean == false||
							gender == 3) {
						Toast.makeText(RegisterActivity.this, getResources().getString(R.string.register_toastEmailFormatOrEmpty), Toast.LENGTH_SHORT).show();
					}
					else {
						//檢查密碼和重複輸入的密碼是否相同
						if (password1.equals(password2)) {
							new doTheRegisterWork().execute();
						}
						else {
							Toast.makeText(RegisterActivity.this, getResources().getString(R.string.register_passwordNotSame), Toast.LENGTH_SHORT).show();
						}
					}
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			break;
		}
		case R.id.register_cancelButton:{
			new AlertDialog.Builder(RegisterActivity.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.register_exitRegisterPageMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			break;
		}
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
