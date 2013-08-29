package com.example.mysql_tester;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class About extends SherlockActivity{
	ActionBar actionbar;
	TextView txt_lan,txt_lng;
	ImageView logo_img;
	LocationManager myLocation = null;    //Location Manager Object Reference,Call when Activity first created
	int i = 0;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);    //將Layout 設定成About
		//取得介面元素
		logo_img = (ImageView)findViewById(R.id.imageView_logo);
		logo_img.setOnClickListener(listener);
		TextView sThanks = (TextView)findViewById(R.id.sThanks);
		String thanks = "JakeWharton\n[ActionBarSherlock]\n"
				+"Jeremy Feinstein\n[SlidingMenu]\n"
				+"chrisbanes\n[ActionBar-PullToRefresh]\n"
				+"Jeff Gilfelt\n[Android Action Bar Style Generator]\n"
				+"Justin Schultz\n[android-lightbox]\n"
				+ "Andy\n[Email Format Check / Stack Overflow]";
		sThanks.setText(thanks);
		//ActionBar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true);
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent go_back = new Intent(About.this,ItemList.class);
			go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(go_back);
			break;
		default:
			super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	private ImageView.OnClickListener listener = new ImageView.OnClickListener(){
		@Override
		public void onClick(View v) {
			if (i == 10){
				logo_img.setImageDrawable(getResources().getDrawable(R.drawable.yaya));
			    i = i + 1;
			}
			else if(i > 10){
			}
			else
				i = i + 1;
		}
	};
}
