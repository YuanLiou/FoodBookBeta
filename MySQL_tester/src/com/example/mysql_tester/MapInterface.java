package com.example.mysql_tester;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.mysql_tester.library.JSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.actionbarsherlock.app.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.widget.Toast;

public class MapInterface extends SherlockFragmentActivity implements LocationListener{
	LatLng myPlace = null;
	LocationManager locationManager;
	Location location;
	Criteria criteria;
	private GoogleMap googleMap;
	Double my_latitude,my_longitude,single_latitude,single_longitude;
	String sid,tag,myProvide;
	JSONParser jsonParser = new JSONParser();
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_interface);
		//ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		Intent mapInterface = this.getIntent();
		Bundle myPosition = mapInterface.getExtras();
		tag = myPosition.getString("tag");
		if (tag.equals("") || tag == null) tag = "all";
		my_latitude = myPosition.getDouble("latitude", 23.979548);    //緯度
		my_longitude = myPosition.getDouble("longitude", 120.696745);    //經度
		myPlace = new LatLng(my_latitude, my_longitude);
		if (tag.equals("single")) {
			sid = myPosition.getString("sid");
			//取得自己的位置
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			criteria = new Criteria();
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			myProvide = locationManager.getBestProvider(criteria, false);
			locationManager.requestLocationUpdates(myProvide, 0, 0, this);
		}
		setUpMapifNeeded();
	}
	
	//Set up map
	public void setUpMapifNeeded() {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			googleMap.setMyLocationEnabled(true);    //顯示「自己」的定位點
			googleMap.setTrafficEnabled(true);    //顯示交通資訊
			//設定UI
			UiSettings uiSettings = googleMap.getUiSettings();
			uiSettings.setTiltGesturesEnabled(false);    //把傾斜手勢取消掉
			uiSettings.setRotateGesturesEnabled(false);    //把旋轉手勢取消
			//Move camera instantly to myPlace with a zoom 15
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 15));
			new GetAllStore().execute();
		}
	}
	
    /*Action Bar*/
  	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.single_map, menu);
		return true;
	}
  	
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem navigation = menu.findItem(R.id.navgation_map);
		//設定如果是大地圖就看不到
		if (tag.equals("all")) {
			navigation.setVisible(false);
		}
		return true;
	}
  	
	public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case android.R.id.home:
    		finish();
    		break;
    	case R.id.navgation_map:
    		//設定要前往的URL(single > my)
    		String urlString = String.format("http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f",
    				single_latitude, single_longitude, my_latitude, my_longitude);
    		Intent navi_intent = new Intent();
    		//交由Google 地圖應用程式接手
    		navi_intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
    		//ActionView: 呈現資料給使用者觀看
    		navi_intent.setAction(android.content.Intent.ACTION_VIEW);
    		//將URL資訊附加在Intent上
    		navi_intent.setData(Uri.parse(urlString));
    		startActivity(navi_intent);
    		break;
    	}
		return true;
	}
	
	
	//在背景讀取項目
	class GetAllStore extends AsyncTask<String, String, String> {
		JSONObject json;
		@Override
		protected void onPreExecute(){
			Toast.makeText(getBaseContext(), getResources().getString(R.string.map_getInformation), Toast.LENGTH_SHORT).show();
		}
		@Override
		protected String doInBackground(String... args) {
			if (tag.equals("single")) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", "get_store_details"));    //set for action
				params.add(new BasicNameValuePair("sID",sid));    //Detail 必要條件
				params.add(new BasicNameValuePair("uID", "0"));
				//String action = "get_store_details";     //救急措施
				json = jsonParser.makeHttpRequest("GET", params);
				//Log.e("Single Map Interface: ", json.toString());
			}
			else {
				List<NameValuePair> item_list = new ArrayList<NameValuePair>();
				item_list.add(new BasicNameValuePair("action", "get_store_list"));    //set for action
				//String action = "get_store_list";    //救急措施
				json = jsonParser.makeHttpRequest("GET", item_list);
				//Log.e("All Map Interface: ", json.toString());
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						createMarkerFromJSON(json);
					} 
					catch (JSONException e) {
						Log.e("Map Interface", "Error procesing JSON.", e);
					}
				}
			});
			return null;
		}
		void createMarkerFromJSON(JSONObject json) throws JSONException {
			//de-serialize the JSON string into an array of store object
			Log.e("Map Interface", json.toString());
			JSONArray jsonArray = json.getJSONArray("Store");
			for (int i = 0;i < jsonArray.length(); i = i+1) {
				//create marker from each store in the store data
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				googleMap.addMarker(new MarkerOptions()
				.title(jsonObject.getString("sName"))
				.snippet(getResources().getString(R.string.map_snipetStartTime)+jsonObject.getString("startTime") 
						+getResources().getString(R.string.map_snipetEndTime)+jsonObject.getString("closeTime"))
				.position(new LatLng(Double.parseDouble(jsonObject.getString("sLatitude")), Double.parseDouble(jsonObject.getString("sLongitude"))))
				);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setUpMapifNeeded();
	}

	@Override
	public void onLocationChanged(Location location) {
		single_latitude = location.getLatitude();
		single_longitude = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		myProvide = locationManager.getBestProvider(criteria, false);
	}
}
