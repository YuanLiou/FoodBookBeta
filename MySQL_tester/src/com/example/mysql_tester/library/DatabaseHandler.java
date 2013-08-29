package com.example.mysql_tester.library;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    //全部 static 變數
	//資料庫的版本
	private static final int DATABASE_VERSION = 1;
	//資料庫的名稱
	private static final String DATABASE_NAME = "android_api";
	//登入表格名稱
	private static final String TABLE_LOGIN = "login";
	//登入表格欄位名稱
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "nick_name";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_UID = "uID";
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
    //建立表格
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LOGIN_TABLE = "CREATE TABLE " +TABLE_LOGIN +"("
				+KEY_ID +" INTEGER PRIMARY KEY,"
				+KEY_NAME +" TEXT,"
				+KEY_UID +" TEXT,"
				+KEY_EMAIL +" TEXT UNIQUE"+")";
		db.execSQL(CREATE_LOGIN_TABLE);
	}
    //更新表格
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//如果表格已存在的話，將舊版的刪掉
		db.execSQL("DROP TABLE IF EXISTS " +TABLE_LOGIN);
		//再建立一個表格
		onCreate(db);
	}
	//儲存USER的詳細資料到表格裡面
	public void addUser(String name,String email,String uid) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_EMAIL, email);
		values.put(KEY_UID, uid);
		//values.put(KEY_CREATE_AT, created_at);
		//Log.e("DB Handler value", values.toString());
		//放入列表之中
		db.insert(TABLE_LOGIN, null, values);
		db.close();
	}
	//從表格裡面取得顧客的資料
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT * FROM " +TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		//移到第一列
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("name", cursor.getString(1));
			user.put("uID", cursor.getString(2));
			user.put("email", cursor.getString(3));
			//user.put("created_at", cursor.getString(4));
		}
		cursor.close();
		db.close();
		//return user
		return user;
	}
	//取得使用者登入狀態，return true if rows are there in table
	public int getRowCount() {
		String countQuery = "SELECT * FROM " +TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		//Log.e("DB Handeler Count = ", ""+rowCount);
		db.close();
		cursor.close();
		//return row count
		return rowCount;
	}
	//重新建立表格，也就是>>刪除所有的表格然後再重新製造出來
	public void resetTable() {
		SQLiteDatabase db = this.getWritableDatabase();
		//刪除所有列
		db.delete(TABLE_LOGIN, null, null);
		db.close();
	}
}
