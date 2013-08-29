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
import com.example.mysql_tester.library.UserFunctions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.actionbarsherlock.app.ActionBar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MapDragger extends SherlockFragmentActivity implements OnMarkerDragListener {
	private ProgressDialog pDialog;
	UserFunctions userFunctions;
	int account_uID = 0;
	TextView txt_AddressSample;
	Button btn_reset;
	LatLng myPlace = null;
	LocationManager locationManager;
	Location location;
	Marker newer_marker,last_opened = null;
	private GoogleMap googleMap;
	Double original_latitude, original_longitude, newer_latitude,
			newer_longitude;
	String sid,tag;
	JSONParser jsonParser = new JSONParser();
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_makerdragger);
		new AlertDialog.Builder(MapDragger.this).setTitle(getResources().getString(R.string.mapdragger_alertCautionTitle))
				.setIcon(R.drawable.ic_launcher).setMessage(getResources().getString(R.string.mapdragger_alertCautionMessage))
				.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
		// �q�������o����
		txt_AddressSample = (TextView) findViewById(R.id.mapdragger_sample_address);
		btn_reset = (Button) findViewById(R.id.btn_drag_reset);
		btn_reset.setOnClickListener(btn_listener);
		//���X�ϥΪ̪�uID
		userFunctions = new UserFunctions();
		boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
		}
		// ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		Intent mapInterface = this.getIntent();
		Bundle myPosition = mapInterface.getExtras();
		tag = myPosition.getString("tag");
		if (tag.equals("EditItem")) {
			sid = myPosition.getString("sid"); // ���o�O���@�ө��a
		}
		else {
			original_latitude = myPosition.getDouble("oriLatitude");
			original_longitude = myPosition.getDouble("oriLongitude");
		}
		setUpMapifNeeded();
	}

	// ���s��ť
	private Button.OnClickListener btn_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			newer_latitude = original_latitude;
			newer_longitude = original_longitude;
			LatLng oriPlace = new LatLng(original_latitude, original_longitude);
			newer_marker.setPosition(oriPlace);
			txt_AddressSample.setText("");
		}
	};

	// Set up map
	public void setUpMapifNeeded() {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
					R.id.map_dragger)).getMap();
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);    //�V�X��
			googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));    //test for bug
			googleMap.setOnMarkerDragListener(this);
			googleMap.setMyLocationEnabled(true); // ��ܡu�ۤv�v���w���I
			googleMap.setTrafficEnabled(false); // ��ܥ�q��T
			// �]�wUI
			UiSettings uiSettings = googleMap.getUiSettings();
			uiSettings.setScrollGesturesEnabled(false);
			uiSettings.setTiltGesturesEnabled(false);
			uiSettings.setRotateGesturesEnabled(false);
			uiSettings.setZoomGesturesEnabled(false);
			uiSettings.setMyLocationButtonEnabled(false); // �ڦb���̩w����s����
			uiSettings.setZoomControlsEnabled(false); // �Y�񱱨�ϰ�����
			if (tag.equals("EditItem"))  new GetAllStore().execute();
			if (tag.equals("AddItem")) setMapforComfirm();
		}
	}
	
	//AddItem �T�{�a�}�ɨϥ�
	private void setMapforComfirm() {
		myPlace = new LatLng(original_latitude, original_longitude);
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace,18));
		newer_marker = googleMap.addMarker(new MarkerOptions()
		.title(getResources().getString(R.string.map_markerForConfirm))
		.position(new LatLng(original_latitude,original_longitude))
		.draggable(true));
	}

	// ���o�ѦҦa�}
	private String getMyAddress(LatLng location) {
		Geocoder geocoder = new Geocoder(getApplicationContext());
		String address = "";
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(location.latitude,location.longitude, 1);
			address = addresses.get(0).getAddressLine(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return address;
	}

	// �b�I��Ū������
	class GetAllStore extends AsyncTask<String, String, String> {
		JSONObject json;
		@Override
		protected void onPreExecute() {
			Toast.makeText(getBaseContext(), getResources().getString(R.string.map_getInformation), Toast.LENGTH_SHORT).show();
		}
		@Override
		protected String doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "get_store_details"));    //set for action
			params.add(new BasicNameValuePair("sID", sid));    //detail ���n����
			params.add(new BasicNameValuePair("uID", "0"));
			//String action = "get_store_details";    //�ϫ汹�I
			json = jsonParser.makeHttpRequest("GET", params);
			//Log.e("Single Map Interface: ", json.toString());
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						createMarkerFromJSON(json);
					} catch (JSONException e) {
						Log.e("Map Interface", "Error procesing JSON.", e);
					}
				}
			});
			return null;
		}
		// �NCamera���ʦܲ{�b��m
		@Override
		protected void onPostExecute(String file_url) {
			myPlace = new LatLng(original_latitude, original_longitude);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 17));
		}
		void createMarkerFromJSON(JSONObject json) throws JSONException {
			// de-serialize the JSON string into an array of store object
			Log.e("Map Interface", json.toString());
			JSONArray jsonArray = json.getJSONArray("Store");
			for (int i = 0; i < jsonArray.length(); i = i + 1) {
				// create marker from each store in the store data
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				original_latitude = Double.parseDouble(jsonObject
						.getString("sLatitude"));
				original_longitude = Double.parseDouble(jsonObject
						.getString("sLongitude"));
				newer_marker = googleMap.addMarker(new MarkerOptions()
						.title(jsonObject.getString("sName"))
						.snippet(getResources().getString(R.string.map_snipetStartTime) +jsonObject.getString("startTime")
								+getResources().getString(R.string.map_snipetEndTime)+ jsonObject.getString("closeTime"))
						.position(new LatLng(original_latitude,original_longitude))
						.draggable(true));
			}
		}
	}

 	/*�b�I���ǥ� AsyncTask��k�A�x�s�ק�L�����e*/
  	class SaveItemDetails extends AsyncTask<String,String,String>{
  	    //�b�I���}�lŪ���H�e�A���Progress Dialog�A�����Ϊ���ܮ�
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(MapDragger.this);
  			pDialog.setMessage(getResources().getString(R.string.mapdragger_updatingOurLocation));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(false);
  			pDialog.show();
  		}
  		//�x�s���
		@Override
		protected String doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "updateItemPosition"));    //set for action
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("sID", sid));    //���n����
			params.add(new BasicNameValuePair("sLatitude", Double.toString(newer_latitude)));
			params.add(new BasicNameValuePair("sLongitude", Double.toString(newer_longitude)));
			//�ǥ�Http Request �o�e�ק��T
			//Notice that update item url accepts POST method
			//String action = "updateItemPosition";    //�ϫ汹�I
			JSONObject json = jsonParser.makeHttpRequest("POST",params);
			//Log.d("Drag Item Detial", json.toString());
			//�ˬdJSON SUCCESS TAG
			try{
				int success = json.getInt("success");
				Log.d("success tag =", " "+success);
				if (success == 1){
					Intent i = getIntent();
					//�N���\��s�A�������e�@��Activity�A�G�^��100
					setResult(100,i);
					finish();
				}
				else{
				}
			}
			catch (JSONException e){
				e.printStackTrace();
			}
			return null;
		}
		//�����I���@�~��A�����ܮظ��X��
		protected void onPostExecute(String file_url) {
		    //��������ܮ�
			pDialog.dismiss();
		}
  	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapifNeeded();
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		txt_AddressSample.setText(getMyAddress(marker.getPosition()));
		newer_latitude = marker.getPosition().latitude;
		newer_longitude = marker.getPosition().longitude;
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.drag_item, menu);
		return true;
	}

	/* ActionBar */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.drag_marker_save:
			new AlertDialog.Builder(MapDragger.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setMessage(getResources().getString(R.string.mapdragger_updateLocationMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new SaveItemDetails().execute();
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
			.show();
			break;
		case R.id.drag_marker_confirm:
			Intent intent = getIntent();
			intent.putExtra("sLatitude", newer_latitude);
			intent.putExtra("sLongitude", newer_longitude);
			setResult(1,intent);
			finish();
		case R.id.drag_marker_cancel:
			finish();
			break;
		case android.R.id.home:
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	//���I�窱�A���ÿ�椸��
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem EditSave = menu.findItem(R.id.drag_marker_save);
		MenuItem AddComfirm = menu.findItem(R.id.drag_marker_confirm);
		if (tag.equals("EditItem")) 
			AddComfirm.setVisible(false);
		else 
			EditSave.setVisible(false);
		return true;
	}
}
