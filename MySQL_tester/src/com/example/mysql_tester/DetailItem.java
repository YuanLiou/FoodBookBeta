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
    //�s�@�@��JSONParser������
  	JSONParser jsonParser = new JSONParser();
  	int account_uID = 0, bigImageIndex, photoOrientation;
  	//Load Photo Use
  	ArrayList<HashMap<String, String>> store_photos;
  	JSONArray all_Photo;
  	ProgressBar photoProgress;
  	LinearLayout photoLayout;
  	View horizontalScrollView;
  	private Dialog lightBoxDialog;
    //MySQL��Ʈw����m
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;
  	//JSON Node �W��
  	private static final String TAG_SUCCESS = "success";
  	private static final String TAG_ITEM = "Store";    //���W��
  	private static final String TAG_PID = "sID";
  	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_item);    //�NLayout �]�w���s��Item����
		//�q�������o����
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
		//�qIntent���oItem��T
		Intent i = this.getIntent();
		//Bundle bundle = i.getExtras();
		//sid = bundle.getString(TAG_PID);
		//�qIntent���o�Ӯasid
		sid = i.getStringExtra(TAG_PID);
		userFunctions = new UserFunctions();
		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		//���X�ϥΪ̪�uID
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
			//Toast.makeText(getApplicationContext(), ""+account_uID, Toast.LENGTH_SHORT).show();
		}
		//�q�I��Method ���oItem���ԲӸ��
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
			editItemPre.edit().clear().commit();    //�M��
			finish();
			break;
		case R.id.modify_object:
			//�q��ܪ�ListView�����o��T
			//Start New Intent
			Intent go_modify_item = new Intent(DetailItem.this,EditItem_tab.class);
			//Sending sid to next activity
			go_modify_item.putExtra(TAG_PID, sid);
			startActivityForResult(go_modify_item,100);
			break;
		case R.id.delete_object:
			//����R�����ʧ@
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
	//�ˬd�ϥΪ̦��S���ק���
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 100){
			//���p����resultCode = 100�A�N��ϥΪ̦��ק�ΧR����T�A�G���s��z�o��
			Intent intent = getIntent();
			/*���s�Ұʳo���A�����᭫�s�}��*/
			finish();
			startActivity(intent);
		}
		//�Ϥ��\��
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;    //�p�G�O���餣���i�H����^��
		options.inInputShareable = true;
		options.inSampleSize = 4;    //�Y�񵥯�
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
				//���F����ϥΪ���V�ӻs�@
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
	
  	/*�b�I���ǥ� AsyncTask��k�AŪ��Item�ԲӸ�T*/
  	class GetItemDetails extends AsyncTask<String,String,String>{
  	    //�b�I���}�lŪ���H�e�A���Progress Dialog(�����Ϊ���ܤ��)
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(DetailItem.this);
  			pDialog.setMessage(getResources().getString(R.string.editItemTab_loadingDetailData));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(true);
  			pDialog.show();
  		}
  		//�b�I�����o�ԲӸ��
		@Override
		protected String doInBackground(String... args) {
			//�b�I���������sUI
			//�ˬdSuccess Tag
			int success;
			try{
				//�إ� Parameter,�ΨӦs���Ʊo��Ƶ��c
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("action", "get_store_details"));    //set for action
				params1.add(new BasicNameValuePair("sID",sid));    //Detail���n
				params1.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
				//�ǥѵo�eHTTP Request�A���oItem �ԲӸ�T
				// Note that product details url will use GET request
				//String action = "get_store_details";    //�ϫ汹�I
				JSONObject json = jsonParser.makeHttpRequest("GET", params1);
				//�qLog�ˬdJSON��Response
				//Log.d("Store sID", sid.toString());
				//Log.d("Single Item Detial", json.toString());
				//JSON Success Tag
				success = json.getInt(TAG_SUCCESS);
				if (success == 1){
					//���\������F��T
					JSONArray itemObj = json.getJSONArray(TAG_ITEM);    //JSON Array
					//�qJSON Array���o�Ĥ@��Item ����
					final JSONObject first_item = itemObj.getJSONObject(0);
					putDataIn(first_item);
					//�qPid��쪺Item
					//EditText�A�έӭI��Thread�B�z�P�ɧ�s����
					runOnUiThread(new Runnable(){
						public void run() {
							//�N�����ܩ�TextView����
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
					//�S��� item ��sid
				}
			}
			catch(JSONException e){
				e.printStackTrace();
			}
			return null;
		}
		//�����I���@�~��A�����ܮظ��X��
		protected void onPostExecute(String file_url) {
		    //��������ܮ�
			pDialog.dismiss();
			new LoadAllPhoto().execute();
		}
  	}

    /*�b�I���ǥ� AsyncTask��k�A�R��Item*/
	class DeleteItem extends AsyncTask<String,String,String>{
	  	//�b�I���}�lŪ���H�e�A���Progress Dialog
	    @Override
	  	protected void onPreExecute(){
	  		super.onPreExecute();
	  		pDialog = new ProgressDialog(DetailItem.this);
	  		pDialog.setMessage(getResources().getString(R.string.detailItem_deletingItem));
	  		pDialog.setIndeterminate(false);
	  		pDialog.setCancelable(true);
	  		pDialog.show();
	  	}
	  	//�b�I�����o�ԲӸ��
		@Override
		protected String doInBackground(String... args) {
			//�ˬdSUCCESS TAG
			int success;
			try{
				//Buliding Parameters�A�@���x�s��ƪ���Ƶ��c
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", "delete_store"));    //set for action
				params.add(new BasicNameValuePair("sID",sid));    //���n
				//�ǥ�Http Request���oItem��T
				//String action = "delete_store";    //�ϫ汹�I
				JSONObject json = jsonParser.makeHttpRequest("POST",params);
				//�ˬdJSON �^����log
				//Log.d("Delete Item", json.toString());
				//JSON SUCCESS TAG
				success = json.getInt(TAG_SUCCESS);
				if (success == 1){
					//�N��R�����\�A�������e�@��Activity�A�G�^��100
					Intent i =getIntent();
					setResult(100,i);
					finish();
				}
				else{
					//�R������
				}
			}
			catch(JSONException e){
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
	
	/*Ū���Ҧ����Ӥ�*/
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
				//�ˬdSuccess Tag
				int success = json.getInt("success");
				if (success == 1) {
					//���StoreImage�M��
					all_Photo = json.getJSONArray("StoreImage");
					//Log.e("Photo JSON Length", "" +all_Photo.length());
					for (int i=0;i < all_Photo.length();i++) {
						JSONObject jObject = all_Photo.getJSONObject(i);
						store_photos.add(ListAdapter(jObject));
						photoLayout.addView(insertPhoto(jObject.getString("thumb_path"), i));
					}
				}
				else {
					//�S���Ӥ�
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		protected void onPostExecute(String file_url) {
			//���槹����
			photoProgress.setVisibility(View.GONE);
			horizontalScrollView.setVisibility(View.VISIBLE);
		}

		/*�Ӥ��B�z��*/
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
					//Toast.makeText(getApplicationContext(), "�o�O�Ӥ�:" +number, Toast.LENGTH_SHORT).show();
				}
			});
			layout.addView(imageView);
			return layout;
		}
	}
	
	/*�Ӥ��sĶ��*/
	public Bitmap decodeBitmapFromUrl(String path) {
		Bitmap bm = null;
		Rect outPadding = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;    //�p�G�O���餣���i�H����^��
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
		//�x�s�C�Ӷ���
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
	
	//�̵n�J���A���ÿ�椸��
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
					//�ɮצW��tmp_avatar_�ɶ�.jpg
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
					int api_version = Build.VERSION.SDK_INT;    //API����
					String android_version = Build.VERSION.RELEASE;    //Android����
					Log.e("android_version Check:", "API:" +api_version +" ,release:" +android_version);
					if(api_version > 10 && !android_version.matches("(1|2)\\..+"))   
						intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);    //���w��������
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
	//�Ϥ��w��
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
				new SaveItemDetails().execute();    //�W�ǷӤ�
			}
		});
		//�վ�j�p
		photoOrientation = orientation;
		double width = bitmap.getWidth();
		double height = bitmap.getHeight();
		double ratio = 300/width;
		int newerHeight = (int)(ratio * height);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, newerHeight, true);    //���]�j�p
		Bitmap_preview.setImageBitmap(resizedBitmap);
		int api_version = Build.VERSION.SDK_INT;    //API����
		String android_version = Build.VERSION.RELEASE;    //Android����
		if(api_version > 10 && !android_version.matches("(1|2)\\.+"))   
			Bitmap_preview.setRotation(orientation);
		imgPreview.show();
	}
	
	//�B�z��V�����D
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
	//�W�ǷӤ�
	/*�b�I���ǥ� AsyncTask��k�A�x�s�ק�L�����e*/
  	class SaveItemDetails extends AsyncTask<String,String,String>{
  	    //�b�I���}�lŪ���H�e�A���Progress Dialog�A�����Ϊ���ܮ�
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(DetailItem.this);
  			pDialog.setMessage(getResources().getString(R.string.detailItem_sendingPhoto));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(true);
  			pDialog.show();
  		}
  		//�x�s���
		@Override
		protected String doInBackground(String... args) {
            //�ˬdbitmap �O�_����
			if (bitmap == null) return null;
			//Building Parameters�A�@���x�s��ƪ���Ƶ��c
			String message = photoMessage.getText().toString();    //�x�s�d��
			if (message.equals("") || message == null) message = "";
			ByteArrayOutputStream imgOpStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, imgOpStream);
			byte[] imgByte = imgOpStream.toByteArray();
			String imgByteString = Base64.encodeToString(imgByte,Base64.DEFAULT);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "upload_store_image"));    //set for action
			params.add(new BasicNameValuePair("sID", sid));    //���n
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("image", imgByteString));
		    params.add(new BasicNameValuePair("message", message));
		    params.add(new BasicNameValuePair("orientation", String.valueOf(photoOrientation)));
		    Log.e("Photo Orientation Int", ""+photoOrientation);
			//�ǥ�Http Request �o�e�ק��T
			//Notice that update item url accepts POST method
		    //String action = "uploadPhoto";    //�ϫ汹�I
			JSONObject json = jsonParser.makeHttpRequest("POST",params);
			Log.d("Photo Update", json.toString());
			//�ˬdJSON SUCCESS TAG
			try{
				int success = json.getInt(TAG_SUCCESS);
				Log.d("success tag =", " "+success);
				if (success == 1){
					//���\��s�I
					Intent i = getIntent();
					//�N���\��s�A�������e�@��Activity�A�G�^��100
					setResult(100,i);
					finish();
				}
				else{
                    //�إߥ���
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
			editWriter.commit();    //�g�J�I
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
  	
  	//���U���}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			editItemPre.edit().clear().commit();    //�M��
		}
		return super.onKeyDown(keyCode, event);
	}
  	
}

