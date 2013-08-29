package com.example.mysql_tester;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.mysql_tester.library.*;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class ItemList extends SherlockActivity implements LocationListener,PullToRefreshAttacher.OnRefreshListener{
	JSONArray all_item;    //存放所有店家
	UserFunctions userFunctions;
	Boolean loginCheck, thereIsNoItem = false, afterSplash = false, pullToRefresh = false, searchMode = false;
	ActionBar actionbar;
	ListView lv;
	LinearLayout listViewFoot;
	Button btn_ReadMore;
	TextView progressText,noItemHaveFound;
	EditText search_keyword;
	View loadingView,listItem_progress,btn_search,noItemHaveFoundAlert;
	MenuItem refresh_list;
	//製作一個JSONParser的物件
	JSONParser jsonParser = new JSONParser();
	ArrayList<HashMap<String,String>> all_item_list;
	//JSON Node 名稱
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_ITEM = "Store";    //表格名稱
	private static final String TAG_PID = "sID";
	private static final String TAG_PRICE = "sMinCharge";
	private static final String TAG_NAME = "sName";
	SharedPreferences sPreferences;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	//獲取GPS資訊
	private static final int ONE_MINUTE = 1000 * 60;
	SlidingMenu slidingMenu;
	LocationManager locateManager;
	String fineProvide,networkProvide,search_keywordString;
	Double double_latitude = 0.0,double_longitude = 0.0;
	int whichHasLoaded = 0 , loading_switch = 0;    //目前讀取了多少，控制ListView位置用
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_item);    //將Layout 設定成所有Item那頁
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setShadowWidthRes(R.dimen.shadowUse);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindOffsetRes(R.dimen.sidebarUse);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		slidingMenu.setMenu(R.layout.sidebar);
		btn_search = (View) findViewById(R.id.search_button);
		btn_search.setOnClickListener(search_listener);
		//檢查Intent是否有資料
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			afterSplash = bundle.getBoolean("afterSplash", false);
			double_latitude = bundle.getDouble("sLatitude");
			double_longitude = bundle.getDouble("sLongitude");
		}
		else {
			sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
			String spre_latitude = sPreferences.getString("userLatitude", "0.0");
			String spre_longitude = sPreferences.getString("userLongitude", "0.0");
			double_latitude = Double.parseDouble(spre_latitude);
			double_longitude = Double.parseDouble(spre_longitude);
		}
		if (checkInternet() == true){    //檢查是不是有連線
			//取得介面元件
			lv = (ListView)findViewById(R.id.listView);
			listItem_progress = (View) findViewById(R.id.listview_progressbar);
			progressText = (TextView)findViewById(R.id.listview_progresstext);
			lv.setOnScrollListener(listview_ScrollListener);
			LayoutInflater inflater = ItemList.this.getLayoutInflater();
			listViewFoot = (LinearLayout)inflater.inflate(R.layout.footer, null);
			lv.addFooterView(listViewFoot);
			btn_ReadMore = (Button)listViewFoot.findViewById(R.id.btn_readMore);
			loadingView = (View)listViewFoot.findViewById(R.id.progressBar1);
			noItemHaveFound = (TextView) findViewById(R.id.listview_noItemFound);
			noItemHaveFoundAlert = (View) findViewById(R.id.listview_alertIcon);
			btn_ReadMore.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					btn_ReadMore.setVisibility(View.GONE);
					loadingView.setVisibility(View.VISIBLE);
					new LoadNextItem().execute();
				}
			});
			mPullToRefreshAttacher = PullToRefreshAttacher.get(this);    //下拉重整用
			mPullToRefreshAttacher.addRefreshableView(lv, this);
			//取得 ActionBar Object Reference
			actionbar = getSupportActionBar();
			//設定ActionBar 可以按 App 圖示回到首頁
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setHomeButtonEnabled(true);
			//HashMap for ListView
			all_item_list = new ArrayList<HashMap<String,String>>();
			//設定監聽
			lv.setOnItemClickListener(listener);    //選擇一個Item，開啟編輯視窗
			//LoginCheck
			userFunctions = new UserFunctions();
			loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
			//Log.v("user logged check = ", ""+loginCheck);
			//在背景讀取所有Item，LoadAllItem是我自訂的一個Method
			new LoadAllItem().execute();
		}
		else{
			new AlertDialog.Builder(ItemList.this)
			.setTitle(getResources().getString(R.string.itemList_alertNetErrorTitle))
			.setMessage(getResources().getString(R.string.itemList_alertNetErrorMes))
			.setCancelable(false)
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				    finish();
				}
			})
			.show();
		}
	}
	
	private View.OnClickListener search_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			search_keyword = (EditText) findViewById(R.id.search_keywords);
			search_keywordString = search_keyword.getText().toString();
			if (search_keywordString.equals("") || search_keywordString == null) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_noString), Toast.LENGTH_SHORT).show();
			}
			else {
				searchMode = true;
				refresh_list.setVisible(true);
				refreshFromMenu();    //搜尋！
				slidingMenu.showContent();
			}
		}
	};

	//set for GPS provider
	public void setLocationFix() {
		//GPS定位使用
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria fine_criteria = new Criteria();    //由GPS取得
		fine_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		fineProvide = locateManager.getBestProvider(fine_criteria, false);
		Criteria network_criteria = new Criteria();    //由 WiFi/3G取得位置
		network_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		network_criteria.setPowerRequirement(Criteria.POWER_LOW);
		networkProvide = locateManager.getBestProvider(network_criteria, false);
	}
	
	//取得GPS位置
	public void gpsFix() {
        setLocationFix();
		if (locateManager.isProviderEnabled(fineProvide)) {
			locateManager.requestLocationUpdates(networkProvide, 0, 0, this);
			locateManager.requestLocationUpdates(fineProvide, ONE_MINUTE, 100, this);
			return;
		}
		else {
			locateManager.requestLocationUpdates(networkProvide, 0, 0, this);
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.gpsRequest), Toast.LENGTH_SHORT).show();
		}
	}

	/*設定按下 清單上 的物件的時候，會發生什麼事*/
	private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
			String sid = ((TextView) v.findViewById(R.id.sid)).getText().toString();
			//從選擇的ListView中取得資訊
			//Start New Intent
			Intent go_detail_item = new Intent(ItemList.this,DetailItem.class);
			//Sending sid to next activity
			go_detail_item.putExtra(TAG_PID, sid);
			//運行該Activity，如果修改成功需要回傳資料
			startActivityForResult(go_detail_item,100);
		}
	};
	//對應EditItem 的回應
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		//if resultCode = 100
		if(resultCode == 100){
			//假如收到resultCode >> 100，代表使用者有修改或刪除資訊，故重新整理這頁
			Intent intent = getIntent();
			/*重新啟動這頁，關閉後重新開啟*/
			finish();
			startActivity(intent);
		}
	}
	public void setAdapter() {
		//在背景更新介面
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				//把從遠端拿來的 JSON 資料給ListView
				//新建一個Adapter 用來存放些資料
				ListAdapter adapter = new SimpleAdapter(ItemList.this,
						all_item_list,
						R.layout.list_item,
						new String[] {TAG_PID,TAG_NAME,TAG_PRICE,"distance"},
						new int[] {R.id.sid,R.id.name,R.id.price,R.id.distance});
				//設定ListView內容
				int currentPosition = lv.getFirstVisiblePosition();
				lv.setAdapter(adapter);
				if (loading_switch == 1) lv.setSelectionFromTop(currentPosition, 0);
			}
		});
	}

	/*讀取用的*/
	class LoadNextItem extends AsyncTask<String, String, String> {
		int itemLength;
		@Override
		protected String doInBackground(String... params) {
			loading_switch = 1;
			//To avoid IllegalStateException so run in UI thread
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					try{
						itemLength = all_item.length();
						int begining = whichHasLoaded;
						int howManyShouldLoad = all_item.length() - whichHasLoaded;
						if (howManyShouldLoad >= 5)
		                    whichHasLoaded += 5;
						else
							whichHasLoaded = all_item.length();
						for (int i=begining ; i<whichHasLoaded ; i++) {
							JSONObject c = all_item.getJSONObject(i);
							//Loading HashMap to ArrayList
							all_item_list.add(ListAdapter(c));
						}
					}
					catch(JSONException e){
						e.printStackTrace();
						//Log.d("All Items: ", json.toString());
					}
				}
			});
			return null;
		}
		protected void onPostExecute(String file_url){
			int howManyUnload = itemLength - whichHasLoaded;
			if (howManyUnload <= 0) {
				lv.removeFooterView(listViewFoot);
				thereIsNoItem = true;
			}
			btn_ReadMore.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
			setAdapter();
		}
	}

	/*在背景藉由 HTTP Request 讀取Item清單的Method*/
	class LoadAllItem extends AsyncTask<String,String,String>{
		int success;
		//在背景開始讀取以前，顯示Progress Dialog(一個提醒用的對話框)
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			lv.setVisibility(View.GONE);
    		noItemHaveFound.setVisibility(View.GONE);
    		noItemHaveFoundAlert.setVisibility(View.GONE);
			progressText.setVisibility(View.VISIBLE);
            listItem_progress.setVisibility(View.VISIBLE);
		}
		//Get All Item from URL
		@Override
		protected String doInBackground(String... args) {
			//Building Parameters, 應該是一個儲存資料的資料結構，用來丟給jsonParser傳給遠方伺服器
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "get_store_list"));    //set for action
			if(searchMode)
				params.add(new BasicNameValuePair("keyword", search_keywordString));
			else
				params.add(new BasicNameValuePair("keyword", ""));
			//String action = "get_store_list";
			params.add(new BasicNameValuePair("userLatitude", Double.toString(double_latitude)));
			params.add(new BasicNameValuePair("userLongitude", Double.toString(double_longitude)));
			//Get JSON String from URL. jsonParser 是外部的Activity
			JSONObject json = jsonParser.makeHttpRequest("GET",params);
			//Log.e("JSON Tester: ", json.toString());
			try{
				//檢查SUCCESS_TAG (成功標籤)
				success = json.getInt(TAG_SUCCESS);
				//Log.v("JSON Success", ""+success);
				if (success == 1){
					//找到Item清單，Getting Array of Items
					all_item = json.getJSONArray(TAG_ITEM);
					int howManyShouldLoad = all_item.length();
					if (howManyShouldLoad >= 5) {
		                whichHasLoaded = 5;
					}
					else { 
					    whichHasLoaded = all_item.length();
					}
					//looping though all item
					for (int i=0 ; i<whichHasLoaded ; i++) {
					    JSONObject c = all_item.getJSONObject(i);
						//Loading HashMap to ArrayList
						all_item_list.add(ListAdapter(c));
					}
				}
				else{
					//假如沒找到任何Item
				}
			}
			catch(JSONException e){
				e.printStackTrace();
				//Log.d("All Items: ", json.toString());
			}
			return null;
		}
		protected void onPostExecute(String file_url){
			if (success == 1) {
				listItem_progress.setVisibility(View.GONE);
				progressText.setVisibility(View.GONE);
				lv.setVisibility(View.VISIBLE);
				setAdapter();
				if (pullToRefresh) {
					mPullToRefreshAttacher.setRefreshComplete();
					pullToRefresh = false;
				}
				int howManyUnload = all_item.length() - whichHasLoaded;
				if (howManyUnload <= 0) {
					lv.removeFooterView(listViewFoot);
					thereIsNoItem = true;
				}
			}
			else {
	    		search_keyword.setText("");
	    		search_keywordString = "";
	    		searchMode = false;
	    		refresh_list.setVisible(false);
				progressText.setVisibility(View.GONE);
	            listItem_progress.setVisibility(View.GONE);
	    		noItemHaveFound.setVisibility(View.VISIBLE);
	    		noItemHaveFoundAlert.setVisibility(View.VISIBLE);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_noItem), Toast.LENGTH_SHORT).show();
			}
		}
	}
	/*Adapter Worker*/
	private HashMap<String,String> ListAdapter(JSONObject jsonObject) {
	    //儲存每個項目
	    String id="",name="",price="",distance="";
		try {
			id = jsonObject.getString(TAG_PID);
		    name = jsonObject.getString(TAG_NAME);
		    price = jsonObject.getString(TAG_PRICE);
			double storeLatitude = Double.valueOf(jsonObject.getString("sLatitude"));
			double storeLongitude = Double.valueOf(jsonObject.getString("sLongitude"));
			distance = CountDistance(double_latitude, double_longitude, storeLatitude, storeLongitude);
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		HashMap<String,String> maps = new HashMap<String,String>();
		maps.put(TAG_PID,id);
		maps.put(TAG_NAME, name);
		maps.put(TAG_PRICE, price);
		maps.put("distance", distance);
        return maps;
	}
    /*Action Bar*/
	public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case android.R.id.home:
    		slidingMenu.showMenu();
    		break;
    	case R.id.add_object:
    		Intent go_add_page = new Intent(ItemList.this,AddItem_tab.class);
    		Bundle myPlace_add = new Bundle();
			myPlace_add.putDouble("latitude", double_latitude);
			myPlace_add.putDouble("longitude", double_longitude);
			go_add_page.putExtras(myPlace_add);
    		startActivity(go_add_page);
    		//Toast.makeText(getApplicationContext(), "經:" +double_longitude +",緯:" +double_latitude, Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.login_user:
    		Intent go_login_page = new Intent(ItemList.this,LoginActivity.class);
    		startActivity(go_login_page);
    		finish();
    		break;
    	case R.id.logout_user:
    		new AlertDialog.Builder(ItemList.this)
    		.setTitle(getResources().getString(R.string.itemList_alertLogoutTitle))
    		.setIcon(R.drawable.ic_launcher)
    		.setMessage(getResources().getString(R.string.itemList_alertLogoutMes))
    		.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					userFunctions.logoutUser(getApplicationContext());
		    		Intent intent = getIntent();
					/*重新啟動這頁，關閉後重新開啟*/
					finish();
					startActivity(intent);
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
    		break;
    	case R.id.reflesh_object:
            refreshFromMenu();
    		break;
    	case R.id.reflesh_list:
    		search_keyword.setText("");
    		search_keywordString = "";
    		searchMode = false;
    		refresh_list.setVisible(false);
    		refreshFromMenu();
    		break;
    	case R.id.map_interface:
    		if (double_latitude == 0.0 && double_longitude == 0.0) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.itemList_goMapInterfaceError), Toast.LENGTH_SHORT).show();
    		}
    		else {
    			Intent go_map_interface = new Intent(ItemList.this, MapInterface.class);
    			Bundle myPlace = new Bundle();
    			myPlace.putDouble("latitude", double_latitude);
    			myPlace.putDouble("longitude", double_longitude);
    			myPlace.putString("tag", "all");
    			go_map_interface.putExtras(myPlace);
    			startActivity(go_map_interface);
    			//Toast.makeText(getApplicationContext(), "經:" +double_longitude +",緯:" +double_latitude, Toast.LENGTH_SHORT).show();
    		}
    		break;
    	case R.id.action_about:    //關於選單
			Intent go_about = new Intent(ItemList.this,About.class);
			startActivity(go_about);
			break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
		return true;
    }
	//依登入狀態隱藏選單元件
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem logout = menu.findItem(R.id.logout_user);
		MenuItem login = menu.findItem(R.id.login_user);
		refresh_list = menu.findItem(R.id.reflesh_list);
		userFunctions = new UserFunctions();
		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			login.setVisible(false);
			logout.setVisible(true);
		}
		else {
			login.setVisible(true);
			logout.setVisible(false);
		}
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void refreshFromMenu() {
		gpsFix();
		all_item_list = new ArrayList<HashMap<String,String>>();
		ArrayAdapter<HashMap<String,String>> adapter = new ArrayAdapter<HashMap<String,String>>(this,
				R.layout.list_item, all_item_list);
		lv.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		whichHasLoaded = 0;
		if (thereIsNoItem) {
			lv.addFooterView(listViewFoot);
			thereIsNoItem = false;
		}
		new LoadAllItem().execute();
	}

	/*檢查網路狀態Method*/
	private boolean checkInternet(){
		boolean result = false;
		ConnectivityManager connect_manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = connect_manager.getActiveNetworkInfo();    //網路狀態
		if (netinfo == null || !netinfo.isConnected())
			result = false;
		else{
			if (!netinfo.isAvailable())
				result = false;
			else
				result = true;
		}
		return result;
	}

	@Override
	public void onLocationChanged(Location location) {
		double_latitude = location.getLatitude();
		double_longitude = location.getLongitude();
		sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);    //test
		Editor preEditor = sPreferences.edit();
		preEditor.putString("userLatitude", Double.toString(double_latitude));
		preEditor.putString("userLongitude", Double.toString(double_longitude));
		preEditor.commit();
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Criteria fine_criteria = new Criteria();    //由GPS取得
		fine_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		fineProvide = locateManager.getBestProvider(fine_criteria, false);
		Criteria network_criteria = new Criteria();    //由 WiFi/3G取得位置
		network_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		networkProvide = locateManager.getBestProvider(network_criteria, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//顯示上次定位的位置
		if (checkInternet() == true && afterSplash == false) {
			setLocationFix();
			locateManager.requestLocationUpdates(networkProvide, 0, 0, this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (locateManager != null) locateManager.removeUpdates(this);
	}

	//離開檢查
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.showContent();
				return true;
			}
			new AlertDialog.Builder(ItemList.this)
			.setTitle(getResources().getString(R.string.itemList_alertExitAppTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.itemList_alertExitAppMes))
			.setPositiveButton(getResources().getString(R.string.itemList_alertExitAppOkay), new DialogInterface.OnClickListener() {
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
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.showContent();
				return true;
			}
			else {
				slidingMenu.showMenu();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	//計算距離
	public String CountDistance(double userLatitude,double userLongitude,
			double storeLatitude,double storeLongitude) {
		float[] result = new float[1];
		Location.distanceBetween(userLatitude, userLongitude, storeLatitude, storeLongitude, result);
		String distance = DistanceText(result[0]);
		return distance;
	}
	//地圖位置相關(格式化)
	private String DistanceText(double distance) {
		if (distance < 1000)
			return String.valueOf((int)distance) + "m";
		else
			return new DecimalFormat("#.00").format(distance/1000) + "km";
	}

    private ListView.OnScrollListener listview_ScrollListener = new ListView.OnScrollListener() {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == SCROLL_STATE_IDLE) {
				if (lv.getLastVisiblePosition() >= lv.getCount() - 1 && !thereIsNoItem) {
					btn_ReadMore.setVisibility(View.GONE);
					loadingView.setVisibility(View.VISIBLE);
					new LoadNextItem().execute();
				}
			}
		}
    };
    //下拉重整用的
	@Override
	public void onRefreshStarted(View view) {
		gpsFix();
		all_item_list = new ArrayList<HashMap<String,String>>();
		ArrayAdapter<HashMap<String,String>> adapter = new ArrayAdapter<HashMap<String,String>>(this,
				R.layout.list_item, all_item_list);
		lv.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		whichHasLoaded = 0;
		pullToRefresh = true;
		if (thereIsNoItem) {
			lv.addFooterView(listViewFoot);
			thereIsNoItem = false;
		}
		new LoadAllItem().execute();
	}

}
