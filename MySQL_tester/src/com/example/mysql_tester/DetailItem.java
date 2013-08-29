package com.example.mysql_tester;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysql_tester.library.*;

public class DetailItem extends SherlockActivity {
    private Uri mImageCaptureUri;
	SharedPreferences editItemPre;
    EditText photoMessage;
    DisplayMetrics dm;
    Bitmap bitmap = null, bm = null;
	UserFunctions userFunctions;
	Boolean loginCheck;
	ActionBar actionbar;
    private TextView detail_Name,detail_Price,detail_Address;
    Double user_latitude = 0.0, user_longitude = 0.0;
    String sid,account;
    //Process Dialog
  	private ProgressDialog pDialog;
    //製作一個JSONParser的物件
  	JSONParser jsonParser = new JSONParser();
  	int account_uID = 0, bigImageIndex, photoOrientation;
  	//Load Photo Use
  	ArrayList<HashMap<String, String>> store_photos;
  	JSONArray all_Photo;
  	ProgressBar photoProgress;
  	LinearLayout photoLayout;
  	View horizontalScrollView;
  	private Dialog lightBoxDialog;
    //MySQL資料庫的位置
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;
  	//JSON Node 名稱
  	private static final String TAG_SUCCESS = "success";
  	private static final String TAG_ITEM = "Store";    //表格名稱
  	private static final String TAG_PID = "sID";
  	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_item);    //將Layout 設定成編輯Item那頁
		//從介面取得元件
		detail_Name = (TextView)findViewById(R.id.textView_detail_name);
		detail_Price = (TextView)findViewById(R.id.textView_detail_price);
		detail_Address = (TextView)findViewById(R.id.textView_detail_address);
		ImageView add_Photos = (ImageView)findViewById(R.id.add_Photo);
		add_Photos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				uploadImageAlertDialog();
			}
		});
		photoProgress = (ProgressBar)findViewById(R.id.Detail_progressBar);
		photoLayout = (LinearLayout)findViewById(R.id.Detail_gallery);
		horizontalScrollView = (View)findViewById(R.id.Detail_horizontalScrollView);
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		store_photos = new ArrayList<HashMap<String,String>>();
		//Actionbar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true);
		//從Intent取得Item資訊
		Intent i = this.getIntent();
		//Bundle bundle = i.getExtras();
		//sid = bundle.getString(TAG_PID);
		//從Intent取得商家sid
		sid = i.getStringExtra(TAG_PID);
		userFunctions = new UserFunctions();
		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		//取出使用者的uID
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
			//Toast.makeText(getApplicationContext(), ""+account_uID, Toast.LENGTH_SHORT).show();
		}
		//從背景Method 取得Item的詳細資料
		new GetItemDetails().execute();
  	}
  	/*ActionBar*/
  	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent go_back = new Intent(DetailItem.this,ItemList.class);
			go_back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(go_back);
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			editItemPre.edit().clear().commit();    //清空
			finish();
			break;
		case R.id.modify_object:
			//從選擇的ListView中取得資訊
			//Start New Intent
			Intent go_modify_item = new Intent(DetailItem.this,EditItem_tab.class);
			//Sending sid to next activity
			go_modify_item.putExtra(TAG_PID, sid);
			startActivityForResult(go_modify_item,100);
			break;
		case R.id.delete_object:
			//執行刪除的動作
			new AlertDialog.Builder(DetailItem.this)
			.setTitle(getResources().getString(R.string.detailItem_alertDeleteTitle))
			.setMessage(getResources().getString(R.string.detailItem_alertDeleteMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new DeleteItem().execute();
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			break;
		case R.id.detail_map_interface:
			if (user_latitude == 0.0 && user_longitude == 0.0) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.detailItem_gpsFixingNow), Toast.LENGTH_SHORT).show();
			}
			else {
    			Intent go_map_interface = new Intent(DetailItem.this, MapInterface.class);
    			Bundle myPlace = new Bundle();
    			myPlace.putDouble("latitude", user_latitude);
    			myPlace.putDouble("longitude", user_longitude);
    			myPlace.putString("tag", "single");
    			myPlace.putString("sid", sid);
    			go_map_interface.putExtras(myPlace);
    			startActivity(go_map_interface);
			}
			break;
		case R.id.register_mystore:
			userFunctions = new UserFunctions();
			loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
			if (loginCheck == true) {
				new AlertDialog.Builder(DetailItem.this)
				.setTitle(getResources().getString(R.string.detailItem_registerMyStoreTitle))
				.setIcon(R.drawable.ic_launcher)
				.setMessage(getResources().getString(R.string.detailItem_registerMyStoreMes))
				.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						registerStore();
					}
				})
				.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
			}
			else {
				new AlertDialog.Builder(DetailItem.this)
				.setTitle(getResources().getString(R.string.detailItem_alertGoToRegisterTitle))
				.setIcon(R.drawable.ic_launcher)
				.setMessage(getResources().getString(R.string.detailItem_alertGoToRegisterMes))
				.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent go_login_page = new Intent(DetailItem.this,LoginActivity.class);
						go_login_page.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(go_login_page);
						finish();
					}
				})
				.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
			}
			break;
		default:
			super.onOptionsItemSelected(item);
		}
  		return true;
  	}
	//檢查使用者有沒有修改資料
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 100){
			//假如收到resultCode = 100，代表使用者有修改或刪除資訊，故重新整理這頁
			Intent intent = getIntent();
			/*重新啟動這頁，關閉後重新開啟*/
			finish();
			startActivity(intent);
		}
		//圖片功能
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;    //如果記憶體不足可以先行回收
		options.inInputShareable = true;
		options.inSampleSize = 4;    //縮放等級
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		Rect outPadding = null;
		if (resultCode != RESULT_OK) return;
		String path = "";
		if (requestCode == PICK_FROM_FILE) {
			InputStream inputStream;
			mImageCaptureUri = data.getData();
			path = getRealPathFromURI(mImageCaptureUri);   //from Gallery
			inputStream = getInputStream(path);
			if (path == null) {
				path = mImageCaptureUri.getPath();    //from File Manager
				inputStream = getInputStream(path);
			}
			if (path != null)
				bitmap = BitmapFactory.decodeStream(inputStream, outPadding, options);
		}
		else {
			if (mImageCaptureUri != null) {
				InputStream inputStream;
			    path = mImageCaptureUri.getPath();
			    inputStream = getInputStream(path);
			    bitmap = BitmapFactory.decodeStream(inputStream, outPadding, options);				
			}
			else {
				//為了防止使用者轉向而製作
				InputStream inputStream = null;
				Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{Media.DATA,Media.DATE_ADDED,MediaStore.Images.ImageColumns.ORIENTATION}, Media.DATE_ADDED, null, "Data Added ASC");
				if (cursor != null && cursor.moveToFirst()) {
					do {
						mImageCaptureUri = Uri.parse(cursor.getString(cursor.getColumnIndex(Media.DATA)));
						path = mImageCaptureUri.getPath();
						inputStream = getInputStream(path);
					} while (cursor.moveToNext());
				    cursor.close();
				}
				bitmap = BitmapFactory.decodeStream(inputStream, outPadding, options);
			}
		}
		int orientation = DetectPhotoOrientation(path);
		ImagePreview(bitmap,orientation);
	}
	
	public InputStream getInputStream(String path) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(path);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return inputStream;
	}
	
  	/*在背景藉由 AsyncTask方法，讀取Item詳細資訊*/
  	class GetItemDetails extends AsyncTask<String,String,String>{
  	    //在背景開始讀取以前，顯示Progress Dialog(提醒用的對話方塊)
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(DetailItem.this);
  			pDialog.setMessage(getResources().getString(R.string.editItemTab_loadingDetailData));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(true);
  			pDialog.show();
  		}
  		//在背景取得詳細資料
		@Override
		protected String doInBackground(String... args) {
			//在背景執行緒更新UI
			//檢查Success Tag
			int success;
			try{
				//建立 Parameter,用來存放資料得資料結構
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("action", "get_store_details"));    //set for action
				params1.add(new BasicNameValuePair("sID",sid));    //Detail必要
				params1.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
				//藉由發送HTTP Request，取得Item 詳細資訊
				// Note that product details url will use GET request
				//String action = "get_store_details";    //救急措施
				JSONObject json = jsonParser.makeHttpRequest("GET", params1);
				//從Log檢查JSON的Response
				//Log.d("Store sID", sid.toString());
				//Log.d("Single Item Detial", json.toString());
				//JSON Success Tag
				success = json.getInt(TAG_SUCCESS);
				if (success == 1){
					//成功的獲取了資訊
					JSONArray itemObj = json.getJSONArray(TAG_ITEM);    //JSON Array
					//從JSON Array取得第一個Item 物件
					final JSONObject first_item = itemObj.getJSONObject(0);
					putDataIn(first_item);
					//從Pid找到的Item
					//EditText，用個背景Thread處理同時更新介面
					runOnUiThread(new Runnable(){
						public void run() {
							//將資料顯示於TextView之中
							try{
								String sName = first_item.getString("sName");
								detail_Name.setText(sName);
								setTitle(sName);
							}
							catch(JSONException e){
								e.printStackTrace();
							}
							try{
								detail_Price.setText(first_item.getString("sMinCharge"));
							}
							catch(JSONException e){
								e.printStackTrace();
							}
							try{
								String full_address = first_item.getString("sCountry")
										+ first_item.getString("sTownship")
										+ first_item.getString("sLocation");
								detail_Address.setText(full_address);
							}
							catch(JSONException e){
								e.printStackTrace();
							}
							try{
								user_latitude = Double.parseDouble(first_item.getString("sLatitude"));
								user_longitude = Double.parseDouble(first_item.getString("sLongitude"));
							}
							catch(JSONException e){
								e.printStackTrace();
							}
						}
					});
				}
				else{
					//沒找到 item 的sid
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
			new LoadAllPhoto().execute();
		}
  	}

    /*在背景藉由 AsyncTask方法，刪除Item*/
	class DeleteItem extends AsyncTask<String,String,String>{
	  	//在背景開始讀取以前，顯示Progress Dialog
	    @Override
	  	protected void onPreExecute(){
	  		super.onPreExecute();
	  		pDialog = new ProgressDialog(DetailItem.this);
	  		pDialog.setMessage(getResources().getString(R.string.detailItem_deletingItem));
	  		pDialog.setIndeterminate(false);
	  		pDialog.setCancelable(true);
	  		pDialog.show();
	  	}
	  	//在背景取得詳細資料
		@Override
		protected String doInBackground(String... args) {
			//檢查SUCCESS TAG
			int success;
			try{
				//Buliding Parameters，一個儲存資料的資料結構
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", "delete_store"));    //set for action
				params.add(new BasicNameValuePair("sID",sid));    //必要
				//藉由Http Request取得Item資訊
				//String action = "delete_store";    //救急措施
				JSONObject json = jsonParser.makeHttpRequest("POST",params);
				//檢查JSON 回應的log
				//Log.d("Delete Item", json.toString());
				//JSON SUCCESS TAG
				success = json.getInt(TAG_SUCCESS);
				if (success == 1){
					//代表刪除成功，為提醒前一個Activity，故回傳100
					Intent i =getIntent();
					setResult(100,i);
					finish();
				}
				else{
					//刪除失敗
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
	
	/*讀取所有的照片*/
	class LoadAllPhoto extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			//Bulid Parameters
			List<NameValuePair> params_photo = new ArrayList<NameValuePair>();
			params_photo.add(new BasicNameValuePair("action", "get_store_images"));
			params_photo.add(new BasicNameValuePair("sID", sid));
			//Get JSON String from URL
			JSONObject json = jsonParser.makeHttpRequest("GET", params_photo);
			//Log.e("Photo JSON test: ", json.toString());
			try {
				//檢查Success Tag
				int success = json.getInt("success");
				if (success == 1) {
					//找到StoreImage清單
					all_Photo = json.getJSONArray("StoreImage");
					//Log.e("Photo JSON Length", "" +all_Photo.length());
					for (int i=0;i < all_Photo.length();i++) {
						JSONObject jObject = all_Photo.getJSONObject(i);
						store_photos.add(ListAdapter(jObject));
						photoLayout.addView(insertPhoto(jObject.getString("thumb_path"), i));
					}
				}
				else {
					//沒有照片
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		protected void onPostExecute(String file_url) {
			//執行完執行
			photoProgress.setVisibility(View.GONE);
			horizontalScrollView.setVisibility(View.VISIBLE);
		}

		/*照片處理者*/
		View insertPhoto(String url,int index) {
			final int number = index;
			bm = decodeBitmapFromUrl(url);
			LinearLayout layout = new LinearLayout(getApplicationContext());
			layout.setLayoutParams(new LayoutParams(getPixels(250),getPixels(250)));
			layout.setGravity(Gravity.CENTER);
			ImageView imageView = new ImageView(getApplicationContext());
			imageView.setLayoutParams(new LayoutParams(getPixels(220),getPixels(220)));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setImageBitmap(bm);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					bigImageIndex = number;
					new LoadSinglePhoto().execute();
					//Toast.makeText(getApplicationContext(), "這是照片:" +number, Toast.LENGTH_SHORT).show();
				}
			});
			layout.addView(imageView);
			return layout;
		}
	}
	
	/*照片編譯者*/
	public Bitmap decodeBitmapFromUrl(String path) {
		Bitmap bm = null;
		Rect outPadding = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;    //如果記憶體不足可以先行回收
		options.inInputShareable = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		try {
			URL url = new URL(path);
			bm = BitmapFactory.decodeStream(url.openConnection().getInputStream(), outPadding, options);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return bm;
	}
	
	/*Adapter Worker*/
	private HashMap<String, String> ListAdapter(JSONObject jsonObject) {
		//儲存每個項目
		String photoId = "", storeId = "", upload_userId = "", message = "",
				bigSizeUrl = "", uploadTime = "";
		try {
			photoId = jsonObject.getString("iID");
			storeId = jsonObject.getString("sID");
			upload_userId = jsonObject.getString("uID");
			message = jsonObject.getString("message");
			bigSizeUrl = jsonObject.getString("path");
			//smallSizeUrl = jsonObject.getString("thumb_path");
			uploadTime = jsonObject.getString("uploadTime");
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		HashMap<String, String> maps = new HashMap<String, String>();
		maps.put("photoId", photoId);
		maps.put("storeId", storeId);
		maps.put("upload_userId", upload_userId);
		maps.put("message", message);
		maps.put("bigSizeUrl", bigSizeUrl);
		//maps.put("smallSizeUrl", smallSizeUrl);
		maps.put("uploadTime", uploadTime);
		return maps;
	}
	
	//依登入狀態隱藏選單元件
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem modifyItem = menu.findItem(R.id.modify_object);
		MenuItem deleteItem = menu.findItem(R.id.delete_object);
		userFunctions = new UserFunctions();
		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			modifyItem.setVisible(true);
			deleteItem.setVisible(true);
		}
		else {
			modifyItem.setVisible(false);
			deleteItem.setVisible(false);
		}
		return true;
	}
	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.detail_menu, menu);
		return true;
	}
	//Register Store
	public void registerStore(){
		account = userFunctions.getUserEmail(getApplicationContext());
		//Log.e("pid and username = ",sid +", " +account);
		Intent sendMail = new Intent(Intent.ACTION_SEND);
		sendMail.setType("message/rfc822");
		sendMail.putExtra(Intent.EXTRA_EMAIL, new String[]{"louis383@gmail.com"});
		sendMail.putExtra(Intent.EXTRA_SUBJECT, "[Shopkeeper] FoodBook, Register for a store.");
		sendMail.putExtra(Intent.EXTRA_TEXT, "Store id number: " +sid +"\n account:  " +account +"\n Request a register for a store to be a shopkeeper.");
		try {
			startActivity(Intent.createChooser(sendMail, getResources().getString(R.string.detailItem_createChooser)));
			finish();
		}
		catch(android.content.ActivityNotFoundException e) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.detailItem_doNotFindAppforSend), Toast.LENGTH_SHORT).show();
		}
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
	
	public void uploadImageAlertDialog() {
		final String[] items = new String[] {getResources().getString(R.string.detailItem_takeaPicture),getResources().getString(R.string.detailItem_choosePhotos)};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item,items);
		new AlertDialog.Builder(DetailItem.this)
		.setTitle(getResources().getString(R.string.detailItem_choosePhotos))
		.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					//檔案名稱tmp_avatar_時間.jpg
					String imagePath = Environment.getExternalStorageDirectory() +"/foodbook/";
					File imageFolder = new File(imagePath);
					imageFolder.mkdirs();
					File file = new File(imagePath,"tmp_foodbook_"+String.valueOf(System.currentTimeMillis())+".jpg");
					mImageCaptureUri = Uri.fromFile(file);
					try {
						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
						intent.putExtra("return-data", true);
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
					dialog.cancel();
				}
				else {
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					int api_version = Build.VERSION.SDK_INT;    //API版本
					String android_version = Build.VERSION.RELEASE;    //Android版本
					Log.e("android_version Check:", "API:" +api_version +" ,release:" +android_version);
					if(api_version > 10 && !android_version.matches("(1|2)\\..+"))   
						intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);    //限定本機物件
					startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.detailItem_alertPlzChoosePitureApp)), PICK_FROM_FILE);
					dialog.cancel();
				}
			}
		})
		.show();
	}
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
		if (cursor == null) return null;
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	//圖片預覽
	private void ImagePreview(Bitmap bitmap,int orientation) {
		final Dialog imgPreview = new Dialog(DetailItem.this);
		imgPreview.setTitle(getResources().getString(R.string.detailItem_imagePreview));
		imgPreview.setContentView(R.layout.photo_preview);
		photoMessage = (EditText)imgPreview.findViewById(R.id.photoMessage);
		ImageView Bitmap_preview = (ImageView) imgPreview.findViewById(R.id.imagePreview);
		Button btn_upload = (Button) imgPreview.findViewById(R.id.imgUpload_ok);
		Button btn_cancel = (Button) imgPreview.findViewById(R.id.imgUpload_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				imgPreview.cancel();
			}
		});
		btn_upload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SaveItemDetails().execute();    //上傳照片
			}
		});
		//調整大小
		photoOrientation = orientation;
		double width = bitmap.getWidth();
		double height = bitmap.getHeight();
		double ratio = 300/width;
		int newerHeight = (int)(ratio * height);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, newerHeight, true);    //重設大小
		Bitmap_preview.setImageBitmap(resizedBitmap);
		int api_version = Build.VERSION.SDK_INT;    //API版本
		String android_version = Build.VERSION.RELEASE;    //Android版本
		if(api_version > 10 && !android_version.matches("(1|2)\\.+"))   
			Bitmap_preview.setRotation(orientation);
		imgPreview.show();
	}
	
	//處理方向的問題
	private int DetectPhotoOrientation(String path) {
		int orientation = 0;
		try {
			ExifInterface eInterface = new ExifInterface(path);
			int orientationExif = eInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Log.e("Photo OrientationExif", "" +orientationExif);
			switch (orientationExif) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				orientation = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				orientation = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				orientation = 270;
				break;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return orientation;
	}
	//上傳照片
	/*在背景藉由 AsyncTask方法，儲存修改過的內容*/
  	class SaveItemDetails extends AsyncTask<String,String,String>{
  	    //在背景開始讀取以前，顯示Progress Dialog，提醒用的對話框
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(DetailItem.this);
  			pDialog.setMessage(getResources().getString(R.string.detailItem_sendingPhoto));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(true);
  			pDialog.show();
  		}
  		//儲存資料
		@Override
		protected String doInBackground(String... args) {
            //檢查bitmap 是否為空
			if (bitmap == null) return null;
			//Building Parameters，一個儲存資料的資料結構
			String message = photoMessage.getText().toString();    //儲存留言
			if (message.equals("") || message == null) message = "";
			ByteArrayOutputStream imgOpStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, imgOpStream);
			byte[] imgByte = imgOpStream.toByteArray();
			String imgByteString = Base64.encodeToString(imgByte,Base64.DEFAULT);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "upload_store_image"));    //set for action
			params.add(new BasicNameValuePair("sID", sid));    //必要
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("image", imgByteString));
		    params.add(new BasicNameValuePair("message", message));
		    params.add(new BasicNameValuePair("orientation", String.valueOf(photoOrientation)));
		    Log.e("Photo Orientation Int", ""+photoOrientation);
			//藉由Http Request 發送修改資訊
			//Notice that update item url accepts POST method
		    //String action = "uploadPhoto";    //救急措施
			JSONObject json = jsonParser.makeHttpRequest("POST",params);
			Log.d("Photo Update", json.toString());
			//檢查JSON SUCCESS TAG
			try{
				int success = json.getInt(TAG_SUCCESS);
				Log.d("success tag =", " "+success);
				if (success == 1){
					//成功更新！
					Intent i = getIntent();
					//代表成功更新，為提醒前一個Activity，故回傳100
					setResult(100,i);
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
  	
  	class LoadSinglePhoto extends AsyncTask<String, String, String> {
        Bitmap temp;
        ProgressBar lightBox_progressBar;
        ImageView bigSizePhoto;
        TextView messageText;
        @Override
        protected void onPreExecute() {
        	super.onPreExecute();
        	showLightBoxDialog();
        	lightBox_progressBar = (ProgressBar) lightBoxDialog.findViewById(R.id.dialog_processBar);
        	bigSizePhoto = (ImageView) lightBoxDialog.findViewById(R.id.imageView_lightbox);
        	messageText = (TextView) lightBoxDialog.findViewById(R.id.dialogText);
        }
		@Override
		protected String doInBackground(String... params) {
			String path = store_photos.get(bigImageIndex).get("bigSizeUrl");
			temp = decodeBitmapFromUrl(path);
			return null;
		}

		protected void onPostExecute(String file_url) {
			String message = store_photos.get(bigImageIndex).get("message");
			if(message.equals("") || message == null) message = "";
			messageText.setText(message);
	  		bigSizePhoto.setImageBitmap(temp);
	  		new ImageViewHelper(getApplicationContext(), dm, bigSizePhoto, temp);
	  		bigSizePhoto.setVisibility(View.VISIBLE);
	  		lightBox_progressBar.setVisibility(View.GONE);
		}
  	}
  	
  	//Get The Data and put in sharePre
  	public void putDataIn(JSONObject target_item) {
		editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
		Editor editWriter = editItemPre.edit();
		try {
			editWriter.putString("sName", target_item.getString("sName"));
			editWriter.putString("sMinCharge", target_item.getString("sMinCharge"));
			editWriter.putString("sPhone", target_item.getString("sPhone"));
			editWriter.putString("sCountry", target_item.getString("sCountry"));
			editWriter.putString("sTownship", target_item.getString("sTownship"));
			editWriter.putString("sLocation", target_item.getString("sLocation"));
			editWriter.putString("startTime", target_item.getString("startTime"));
			editWriter.putString("closeTime", target_item.getString("closeTime"));
			editWriter.putString("sWeek", target_item.getString("sWeek"));
			editWriter.putString("sMemo", target_item.getString("sMemo"));
			editWriter.putString("sEmail", target_item.getString("sEmail"));
			editWriter.putString("sURL", target_item.getString("sURL"));
			int sCanDelivery = Integer.parseInt(target_item.getString("sCanDelivery"));
			editWriter.putInt("sCanDelivery", sCanDelivery);
			int sCanToGo = Integer.parseInt(target_item.getString("sCanToGo"));
			editWriter.putInt("sCanToGo", sCanToGo);
			int is24Hours = Integer.parseInt(target_item.getString("is24Hours"));
			editWriter.putInt("is24Hours", is24Hours);
			editWriter.commit();    //寫入！
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
  	}
  	
  	//GetDpiToPixels
  	public int getPixels(int dipValue) {
  		Resources resources = getResources();
  		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
  		return px;
  	}
  	
  	//LightBox Dialog
  	public void showLightBoxDialog() {
  		lightBoxDialog = new Dialog(this, R.style.lightbox_dialog);
  		lightBoxDialog.setContentView(R.layout.lightbox_dialog);
  		lightBoxDialog.show();
  	}
  	
  	//按下離開
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			editItemPre.edit().clear().commit();    //清空
		}
		return super.onKeyDown(keyCode, event);
	}
  	
}

