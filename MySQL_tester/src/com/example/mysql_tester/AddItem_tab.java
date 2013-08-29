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
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.mysql_tester.library.*;

public class AddItem_tab extends SherlockFragmentActivity implements LocationListener {
	UserFunctions userFunctions;
	ActionBar actionBar;
	//SherlockActionBar
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	TextView tabCenter,tabText;
	//Process Dialog 正在讀取中對話框
	private ProgressDialog pDialog;
	//製作一個JSONParser的物件
	JSONParser jsonParser = new JSONParser();
	//JSON Node 名稱
	private static final String TAG_SUCCESS = "success";
	//獲取GPS資訊
	private static final int ONE_MINUTE = 1000 * 60;
	Location currentBestLocation = null;
	LocationManager locateManager = null;
	String fineProvide,networkProvide;
	Double double_latitude = 0.0,double_longitude = 0.0;
	int account_uID = 0;
	boolean isLocationEdited = false;
	SharedPreferences addItemPre;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.add_item_pager);
		setContentView(mViewPager);
		//ActionBar
		actionBar = getSupportActionBar();
		//設定ActionBar的顯示模式
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    //設定ActionBar 可以按App圖示回上一頁
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mTabsAdapter = new TabsAdapter(this, mViewPager);
		//GPS定位使用
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria fine_criteria = new Criteria();    //由GPS取得
		fine_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		fineProvide = locateManager.getBestProvider(fine_criteria, false);
		Criteria network_criteria = new Criteria();    //由 WiFi/3G取得位置
		network_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		network_criteria.setPowerRequirement(Criteria.POWER_LOW);
		networkProvide = locateManager.getBestProvider(network_criteria, false);
		gpsFix();    //Request 一個GPS位置
		//從Bundle 之中拿出 ItemList 的資料
		Intent mapInterface = this.getIntent();
		if (!isLocationEdited) {
			Bundle myPosition = mapInterface.getExtras();
			double_latitude = myPosition.getDouble("latitude", 23.979548);    //緯度
			double_longitude = myPosition.getDouble("longitude", 120.696745);    //經度
		}
		getMyAddress();
		//取出使用者的uID
		userFunctions = new UserFunctions();
		boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
			//Toast.makeText(getApplicationContext(), ""+account_uID, Toast.LENGTH_SHORT).show();
		}
        setActionbarTabs();
	}
	
	private void setActionbarTabs() {
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName1)).setIcon(android.R.drawable.ic_menu_info_details),AddItem.class, null, "tab1");
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName2)).setIcon(android.R.drawable.ic_menu_recent_history),AddItem_page2.class, null, "tab2");
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName3)).setIcon(android.R.drawable.ic_menu_agenda),AddItem_page3.class, null, "tab3");
	}

	//取得GPS位置
	public void gpsFix() {
		if (locateManager.isProviderEnabled(fineProvide)) {
			locateManager.requestLocationUpdates(networkProvide, 0, 0, this);
			locateManager.requestLocationUpdates(fineProvide, 1, 0, this);
			return;
		}
		else {
			locateManager.requestLocationUpdates(networkProvide, 1, 0, this);
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.gpsRequest), Toast.LENGTH_SHORT).show();
		}
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
        	private final String tags;
        	TabInfo(Class<?> _cClass, Bundle _args, String _tags) {
				clss = _cClass;
				args = _args;
				tags = _tags;
			}
        }
		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = activity.getSupportActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
			mViewPager.setOffscreenPageLimit(2);    //Access the behind page
		}
		
		public void addTabs(ActionBar.Tab tab,Class<?> clss,Bundle args, String tags) {
			TabInfo info = new TabInfo(clss, args, tags);
			tab.setTag(tags);
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
		    	if (mTabs.get(i).tags  == tag) {
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

	//Source code from my past version
	//在背景執行：新增一個Item
	class CreateNewItem extends AsyncTask<String,String,String>{
		//在背景開始讀取以前，顯示Progress Dialog(對話方塊)
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			pDialog = new ProgressDialog(AddItem_tab.this);
			pDialog.setMessage(getResources().getString(R.string.ItemTab_processing));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		//在背景實作
		@Override
		protected String doInBackground(String... args) {
			//從Bundle中取得物件，並且轉成字串或int
			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
			String name = addItemPre.getString("sName", "");
			String price = addItemPre.getString("sMinCharge", "");
			String phone = addItemPre.getString("sPhone", "");
			String sCountry = addItemPre.getString("sCountry", "臺北市");
			String sTownship = addItemPre.getString("sTownship", "");
			String sLocation = addItemPre.getString("sLocation", "");
			int Is24Hours = addItemPre.getInt("is24Hours", 0);
			String startTime = addItemPre.getString("startTime", "");
			String closeTime = addItemPre.getString("closeTime", "");
			String rest_day = addItemPre.getString("sWeek", "");
			String sMemo = addItemPre.getString("sMemo", "");
			String email = addItemPre.getString("sEmail", "");
			String webpage = addItemPre.getString("sURL", "");
			int sCanDelivery = addItemPre.getInt("sCanDelivery",0);
			int sCanToGo = addItemPre.getInt("sCanToGo",0);
			//Building Parameters，一個儲存資料的資料結構
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "create_store"));    //set for action
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("sLatitude", Double.toString(double_latitude)));    //必要
			params.add(new BasicNameValuePair("sLongitude", Double.toString(double_longitude)));    //必要
			params.add(new BasicNameValuePair("sName",name));   //店家名稱,必要
			params.add(new BasicNameValuePair("sMinCharge",price));  //低銷
			params.add(new BasicNameValuePair("sPhone",phone));   //電話
			params.add(new BasicNameValuePair("sLocation",sLocation));    //後墜地址
			params.add(new BasicNameValuePair("sTownship",sTownship));    //鄉鎮市區地址
			params.add(new BasicNameValuePair("sCountry",sCountry));    //加入地址
			params.add(new BasicNameValuePair("is24Hours", Integer.toString(Is24Hours)));    //是不是24小時
			params.add(new BasicNameValuePair("startTime",startTime));    //開始營業時間
			params.add(new BasicNameValuePair("closeTime",closeTime));    //結束營業時間
			params.add(new BasicNameValuePair("sWeek",rest_day));    //休息日
			params.add(new BasicNameValuePair("sMemo",sMemo));    //備註
			params.add(new BasicNameValuePair("sEmail",email));    //E-mail
			params.add(new BasicNameValuePair("sURL",webpage));    //網站
			params.add(new BasicNameValuePair("sCanDelivery",Integer.toString(sCanDelivery)));    //可外送
			params.add(new BasicNameValuePair("sCanToGo",Integer.toString(sCanToGo)));    //可外帶
			//取得JSON物件
			//Note that create item url accepts POST method
			//String action = "create_store";    //救急措施
			JSONObject json = jsonParser.makeHttpRequest("POST",params);
			//檢查Log cat看看有沒有回應
			Log.d("Create Response",json.toString());
			//檢查SUCCESS TAG
			try{
				int success = json.getInt(TAG_SUCCESS);
				if (success == 1){
					//建立成功，回到項目清單那頁
					Intent go_item_list = new Intent(AddItem_tab.this,ItemList.class);
					go_item_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(go_item_list);
					addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
					addItemPre.edit().clear().commit();
					finish(); //關閉這個Activity
				}
				else{
					//建立失敗
				}
			}
			catch(JSONException e){
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
		getSupportMenuInflater().inflate(R.menu.add_page, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent go_back = new Intent(AddItem_tab.this,ItemList.class);
			go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
			addItemPre.edit().clear().commit();
			startActivity(go_back);
			break;
		case R.id.add_an_object:
			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
			String name = addItemPre.getString("sName", "");
			String price = addItemPre.getString("sMinCharge", "");
			String phone = addItemPre.getString("sPhone", "");
			String sCountry = addItemPre.getString("sCountry", "臺北市");
			String sTownship = addItemPre.getString("sTownship", "");
			String sLocation = addItemPre.getString("sLocation", "");
			String startTime = addItemPre.getString("startTime", "");
			String closeTime = addItemPre.getString("closeTime", "");
			String rest_day = addItemPre.getString("sWeek", "");
			if (double_latitude == 0.0 && double_longitude == 0.0) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.waitForGps), Toast.LENGTH_SHORT).show();
				break;
			}
			if (name.equals("") || sCountry.equals(getResources().getString(R.string.ItemTab_countryNull))){
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ItemTab_nameOrCountryError), Toast.LENGTH_SHORT).show();
			}
			else{
				//Toast.makeText(getApplicationContext(), "經:" +double_longitude +",緯:" +double_latitude, Toast.LENGTH_SHORT).show();    //Just Test
				new AlertDialog.Builder(AddItem_tab.this)
				.setTitle(getResources().getString(R.string.alertComfirmTitle))
				.setIcon(R.drawable.ic_launcher)
				.setMessage(getResources().getString(R.string.ItemTab_comfirmMessage1)
						+getResources().getString(R.string.ItemTab_comfirmMessage2)+name
						+getResources().getString(R.string.ItemTab_comfirmMessage3)+price
						+getResources().getString(R.string.ItemTab_comfirmMessage4)+phone
						+getResources().getString(R.string.ItemTab_comfirmMessage5)+sCountry+sTownship+sLocation
						+getResources().getString(R.string.ItemTab_comfirmMessage6)+startTime
						+getResources().getString(R.string.ItemTab_comfirmMessage7)+closeTime
						+getResources().getString(R.string.ItemTab_comfirmMessage8)+rest_day
						+getResources().getString(R.string.ItemTab_comfirmMessage9))
				.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//在背景處理：新增一個Item
						new CreateNewItem().execute();
					}
				})
				.show();
			}
			break;
		case R.id.addItem_modifyLocation:
			if (double_latitude != 0.0 || double_longitude != 0.0) {
				Intent location_confirm = new Intent(AddItem_tab.this,MapDragger.class);
				Bundle bundle = new Bundle();
				bundle.putString("tag", "AddItem");
				bundle.putDouble("oriLatitude", double_latitude);
				bundle.putDouble("oriLongitude", double_longitude);
				location_confirm.putExtras(bundle);
				startActivityForResult(location_confirm, 1);
			}
			else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.waitForGps), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.addItem_refresh:
			isLocationEdited = false;
			gpsFix();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	//使用者送回了位置資訊
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 1) {
			isLocationEdited = true;
			locateManager.removeUpdates(this);
			double_latitude = data.getDoubleExtra("sLatitude", 0.0);
			double_longitude = data.getDoubleExtra("sLongitude", 0.0);
			if (double_latitude == null || double_latitude == 0.0 ||
					double_longitude == null || double_longitude == 0.0) {
				gpsFix();    //如果沒有取得資料那麼就重新定位
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.ItemTab_dataGetError), Toast.LENGTH_SHORT).show();
			}
			//Toast.makeText(getApplicationContext(), "經度:" +double_longitude +"緯度:" +double_latitude, Toast.LENGTH_LONG).show();
		}
	}
	/*To fix the IllegalArgumentException：轉向問題*/
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
	@Override
	public void onLocationChanged(Location location) {
		if (isBetterLocation(location, currentBestLocation) == true) {
			currentBestLocation = location;
			double_latitude = location.getLatitude();
			double_longitude = location.getLongitude();
		}
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
	//是否為更好的 Location 檢查
	protected boolean isBetterLocation(Location location,Location currentBestLocation) {
	    //Determine whether one Location reading is better than the current Location fix
		//@Param location The new location that you want to evaluate
		//@Param currentBestLocation The current Location fix, to which you want to compare the new one
		if (currentBestLocation == null) {
			//A new location is always better than no location
			return true;
		}

		//Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSegnificantlyNewer = timeDelta > ONE_MINUTE;
		boolean isSegnificantlyOlder = timeDelta < ONE_MINUTE;
		boolean isNewer = timeDelta > 0;

		//if it's been more than one minute since the current location,use the new location
		//because the user has likely moved
		if (isSegnificantlyNewer) {
			return true;
		}
		//if the new location is more than one minuted older, it must be worst
		else if (isSegnificantlyOlder) {
			return false;
		}
		//Check whether the new location fix is more or less accurate
		int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSegnificantlyLessAccurate = accuracyDelta > 200;
		//Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),currentBestLocation.getProvider());
		//Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		}
		else if (isNewer && !isLessAccurate) {
			return true;
		}
		else if (isNewer && !isSegnificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}
	/*檢查兩個Provider是不是同一個*/
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return false;
		}
		return provider1.equals(provider2);
	}
	@Override
	protected void onResume() {
		super.onResume();
		//顯示上次定位的位置
		if (!isLocationEdited) {
			Location lastLocation = locateManager.getLastKnownLocation(fineProvide);
			currentBestLocation = lastLocation;
			if (lastLocation != null) {
				double_latitude = lastLocation.getLatitude();
				double_longitude = lastLocation.getLongitude();
			}
			else {
				Location lastLocation_network = locateManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				currentBestLocation = lastLocation_network;
				if (lastLocation_network != null) {
					double_latitude = lastLocation_network.getLatitude();
					double_longitude = lastLocation_network.getLongitude();
				}
			}			
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		locateManager.removeUpdates(this);
	}
    /*取得參考地址*/
    public void getMyAddress() {
    		try {
    		Geocoder geocoder = new Geocoder(getBaseContext());
    		List<Address> addresses = null;
    		if (double_latitude != 0.0 && double_longitude != 0.0){
    			addresses = geocoder.getFromLocation(double_latitude, double_longitude, 1);
    			String subLocality = addresses.get(0).getSubLocality();
    			if (subLocality == null) subLocality = "";
    			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
    			Editor preWriter = addItemPre.edit();    //設定好Editor
    			preWriter.putString("sampleAdd1", addresses.get(0).getAddressLine(0));
    			preWriter.putString("sampleAdd2", addresses.get(0).getLocality());
    			preWriter.putString("sampleAdd3", subLocality +addresses.get(0).getThoroughfare() + addresses.get(0).getFeatureName() +getResources().getString(R.string.ItemTab_addressNo));
    			preWriter.putString("sampleAdd4", addresses.get(0).getAdminArea());
    			preWriter.commit();    //寫入！
    		}
    		else {
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	//離開提示
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		    new AlertDialog.Builder(AddItem_tab.this)
		    .setTitle(getResources().getString(R.string.ItemTab_cancelToEdit))
		    .setIcon(R.drawable.ic_launcher)
		    .setMessage(getResources().getString(R.string.ItemTab_cancelToEditMessage))
		    .setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
					addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
					addItemPre.edit().clear().commit();
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
