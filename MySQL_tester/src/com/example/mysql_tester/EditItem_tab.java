package com.example.mysql_tester;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.mysql_tester.library.*;


public class EditItem_tab extends SherlockFragmentActivity {
	UserFunctions userFunctions;
	int account_uID = 0;
    //Process Dialog
  	private ProgressDialog pDialog;
    //製作一個JSONParser的物件
	//SherlockActionBar
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	TextView tabCenter,tabText;
  	JSONParser jsonParser = new JSONParser();
  	//sID
  	String sid,current_country;
  	//ActionBar
  	ActionBar actionBar;
  	SharedPreferences editItemPre;
  	//JSON Node 名稱
  	private static final String TAG_SUCCESS = "success";
  	private static final String TAG_PID = "sID";
  	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
		//取出使用者的uID
		userFunctions = new UserFunctions();
		boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
		}
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.edit_item_pager);
		setContentView(mViewPager);
		actionBar = getSupportActionBar();
  		actionBar.setDisplayHomeAsUpEnabled(true);
  		actionBar.setHomeButtonEnabled(true);
  		mTabsAdapter = new TabsAdapter(this, mViewPager);
		//從Intent取得Item資訊
		Intent i = this.getIntent();
		//從Intent取得Item sid 和 目前選擇的直轄市縣
		sid = i.getStringExtra(TAG_PID);
		setActionBarTabs();
  	}

  	public void setActionBarTabs() {
  	//ActionBar
  		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
  		mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName1)).setIcon(android.R.drawable.ic_menu_info_details),EditItem.class, null);
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName2)).setIcon(android.R.drawable.ic_menu_recent_history),EditItem_page2.class, null);
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName3)).setIcon(android.R.drawable.ic_menu_agenda),EditItem_page3.class, null);
  	}
  	
	//TabAdapter
	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        
        static final class TabInfo {
        	private final Class<?> clss;
        	private final Bundle args;
        	TabInfo(Class<?> _cClass, Bundle _args) {
				clss = _cClass;
				args = _args;
			}
        }
		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = activity.getSupportActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}
		
		public void addTabs(ActionBar.Tab tab,Class<?> clss,Bundle args) {
			TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}
		
		@Override
		public void onPageScrollStateChanged(int state) {}
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}
		@Override
		public void onTabSelected(Tab tab,FragmentTransaction ft) {
		    Object tag = tab.getTag();
		    for (int i = 0;i < mTabs.size();i++) {
		    	if (mTabs.get(i)  == tag) {
		    		mViewPager.setCurrentItem(i);
		    	}
		    }
		}
		@Override
		public void onTabUnselected(Tab tab,FragmentTransaction ft) {}
		@Override
		public void onTabReselected(Tab tab,FragmentTransaction ft) {}
		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}
		
		@Override
		public int getCount() {
			return mTabs.size();
		}
	}

  	/*在背景藉由 AsyncTask方法，儲存修改過的內容*/
  	class SaveItemDetails extends AsyncTask<String,String,String>{
  	    //在背景開始讀取以前，顯示Progress Dialog，提醒用的對話框
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(EditItem_tab.this);
  			pDialog.setMessage(getResources().getString(R.string.editItemTab_saving));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(true);
  			pDialog.show();
  		}
  		//儲存資料
		@Override
		protected String doInBackground(String... args) {
			//從Bundle 之中取得資訊
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
            String price,phone,sCountry,sTownship,sLocation,startTime,closeTime,sWeek,sMemo,sEmail,sURL,sDili,sTogo;
            price = editItemPre.getString("sMinCharge", "");
            phone = editItemPre.getString("sPhone", "");
            sCountry = editItemPre.getString("sCountry","臺北市");
            sTownship = editItemPre.getString("sTownship", "");
            sLocation = editItemPre.getString("sLocation", "");
            startTime = editItemPre.getString("startTime", "");
            closeTime = editItemPre.getString("closeTime", "");
            String is24Hours = Integer.toString(editItemPre.getInt("is24Hours", 0));
            sWeek = editItemPre.getString("sWeek", "");
            sMemo = editItemPre.getString("sMemo", "");
            sEmail = editItemPre.getString("sEmail", "");
            sURL = editItemPre.getString("sURL", "");
            sDili = Integer.toString(editItemPre.getInt("sCanDelivery", 0));
            sTogo = Integer.toString(editItemPre.getInt("sCanToGo", 0));
            //Log.d("sID = ", ""+sid);
			//Building Parameters，一個儲存資料的資料結構
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "updateItem"));    //set for action
			params.add(new BasicNameValuePair("sID", sid));    //Update必要
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("sMinCharge", price));  //低銷
			params.add(new BasicNameValuePair("sPhone", phone));   //電話
			params.add(new BasicNameValuePair("sCountry", sCountry));    //直轄市縣
			params.add(new BasicNameValuePair("sTownship", sTownship));    //鄉鎮市區地址
			params.add(new BasicNameValuePair("sLocation", sLocation));    //後墜地址
			params.add(new BasicNameValuePair("is24Hours", is24Hours));
		    params.add(new BasicNameValuePair("startTime", startTime));    //開始營業時間
			params.add(new BasicNameValuePair("closeTime", closeTime));    //結束營業時間
			params.add(new BasicNameValuePair("sWeek", sWeek));    //休息日
			params.add(new BasicNameValuePair("sMemo", sMemo));    //備註
    		params.add(new BasicNameValuePair("sEmail", sEmail));    //E-mail
    		params.add(new BasicNameValuePair("sURL", sURL));    //網站
    		params.add(new BasicNameValuePair("sCanDelivery", sDili));    //可外送
    		params.add(new BasicNameValuePair("sCanToGo", sTogo));    //可外帶
			//藉由Http Request 發送修改資訊
			//Notice that update item url accepts POST method
    		//String action = "updateItem";    //救急措施
			JSONObject json = jsonParser.makeHttpRequest("POST",params);
			Log.d("Edit Item Detial", json.toString());
			//檢查JSON SUCCESS TAG
			try{
				int success = json.getInt(TAG_SUCCESS);
				Log.d("success tag =", " "+success);
				if (success == 1){
					//成功更新！
					Intent i = getIntent();
					//代表成功更新，為提醒前一個Activity，故回傳100
					setResult(100,i);
					editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
					editItemPre.edit().clear().commit();    //清空
					finish();
				}
				else{
                    //建立失敗
				}
			}
			catch (JSONException e){
				e.printStackTrace();
			}
			return null;
		}
		//完成背景作業後，防止對話框跳出來
		protected void onPostExecute(String file_url) {
		    //忽略掉對話框
			pDialog.dismiss();
		}
  	}
  	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.edit_page, menu);
		return true;
	}
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			editItemPre.edit().clear().commit();    //清空
			finish();
			break;
		case R.id.edit_object_cancel:
			new AlertDialog.Builder(EditItem_tab.this)
			.setTitle(getResources().getString(R.string.editItemTab_alertAskForExitTitle))
			.setMessage(getResources().getString(R.string.editItemTab_alertAskForExitMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
					editItemPre.edit().clear().commit();    //清空
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
		case R.id.edit_object_save:
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			//執行儲存變更的動作
			String name = editItemPre.getString("sName", "");
			String price = editItemPre.getString("sMinCharge", "");
			String phone = editItemPre.getString("sPhone", "");
			String sCountry = editItemPre.getString("sCountry", "臺北市");
			String sTownship = editItemPre.getString("sTownship", "");
			String sLocation = editItemPre.getString("sLocation", "");
			String startTime = editItemPre.getString("startTime", "");
			String closeTime = editItemPre.getString("closeTime", "");
			String rest_day = editItemPre.getString("sWeek", "");
			new AlertDialog.Builder(EditItem_tab.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.editItemTab_confirmMessage)+name
					+getResources().getString(R.string.ItemTab_comfirmMessage3)+price
					+getResources().getString(R.string.ItemTab_comfirmMessage4)+phone
					+getResources().getString(R.string.ItemTab_comfirmMessage5)+sCountry+sTownship+sLocation
					+getResources().getString(R.string.ItemTab_comfirmMessage6)+startTime
					+getResources().getString(R.string.ItemTab_comfirmMessage7)+closeTime
					+getResources().getString(R.string.ItemTab_comfirmMessage8)+rest_day)
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//在背景處理：新增一個Item
					new SaveItemDetails().execute();
				}
			})
			.show();
			break;
		case R.id.edit_marker_drag:
			Intent intent = new Intent(EditItem_tab.this,MapDragger.class);
			Bundle bundle = new Bundle();
			bundle.putString("sid", sid);
			bundle.putString("tag", "EditItem");
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
			break;
		}
  		return true;
  	}

	//離開提示
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		    new AlertDialog.Builder(EditItem_tab.this)
		    .setTitle(getResources().getString(R.string.ItemTab_cancelToEdit))
		    .setIcon(R.drawable.ic_launcher)
		    .setMessage(getResources().getString(R.string.ItemTab_cancelToEditMessage))
		    .setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
					editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
					editItemPre.edit().clear().commit();    //清空
				    finish();
			    }
		    })
		    .setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
		    	}
		    })
		    .show();
		    return true;
		}
	return super.onKeyDown(keyCode, event);
	}
}
