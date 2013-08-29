package com.example.mysql_tester;

import com.actionbarsherlock.app.SherlockActivity;

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
import android.os.Bundle;
import android.view.Window;

public class splash extends SherlockActivity implements LocationListener{
	LocationManager locateManager;
	String netProvider;
	SharedPreferences sPreferences;
	double userLatitude = 0.0,userLongitude = 0.0;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(checkInternet() == false) {
			new AlertDialog.Builder(splash.this)
			.setTitle(getResources().getString(R.string.splash_alertNetErrorTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.splash_alertNetErrorMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.show();
		}
		else {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);   //隱藏標題列
			setContentView(R.layout.splash);
			getNetworkFix();
			sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
	        Thread userLocationFix = new Thread() {
				@Override
	        	public void run() {
					try {
						int times = 0;
						while (userLatitude == 0.0 || userLongitude == 0.0) {
							sleep(150);
							times += 1;
							if (times > 250) {
								getLastLocation();
								break;
							}
						}
					}
					catch (Exception e){
						e.printStackTrace();
					}
					finally {
						Intent intent = new Intent(splash.this,ItemList.class);
						Bundle bundle = new Bundle();
						bundle.putBoolean("afterSplash", true);
						bundle.putDouble("sLatitude", userLatitude);
						bundle.putDouble("sLongitude", userLongitude);
						intent.putExtras(bundle);
						startActivity(intent);
						overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);    //動畫
						finish();
					}
				}
	        };
	        userLocationFix.start();
		}
	}
	
	public void getLastLocation() {
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Location lastlocation = locateManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastlocation != null) {
			userLatitude = lastlocation.getLatitude();
			userLongitude = lastlocation.getLongitude();
		}
		else {
			Location lastlocation_net = locateManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastlocation_net != null) {
				userLatitude = lastlocation_net.getLatitude();
				userLongitude = lastlocation_net.getLongitude();
			}
			else {
				userLatitude = 0.0;
				userLongitude = 0.0;
			}
		}
	}
	
	public void getNetworkFix() {
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria netCriteria = new Criteria();
		netCriteria.setAccuracy(Criteria.ACCURACY_COARSE);    //使用3G/Wifi定位
		netCriteria.setPowerRequirement(Criteria.POWER_LOW);
		netProvider = locateManager.getBestProvider(netCriteria, false);
		locateManager.requestLocationUpdates(netProvider, 80, 10, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		userLatitude = location.getLatitude();
		userLongitude = location.getLongitude();
		Editor preEditor = sPreferences.edit();
		preEditor.putString("userLatitude", Double.toString(userLatitude));
		preEditor.putString("userLongitude", Double.toString(userLongitude));
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
		Criteria netCriteria = new Criteria();
		netCriteria.setAccuracy(Criteria.ACCURACY_COARSE);    //使用3G/Wifi定位
		netCriteria.setPowerRequirement(Criteria.POWER_LOW);
		netProvider = locateManager.getBestProvider(netCriteria, false);
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
	protected void onStop() {
		super.onStop();
		if (locateManager != null) locateManager.removeUpdates(this);
	}
}
