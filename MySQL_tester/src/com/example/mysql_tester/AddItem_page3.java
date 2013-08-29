package com.example.mysql_tester;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

public class AddItem_page3 extends SherlockFragment {
	EditText edt_email,edt_web,edt_phone,edt_price;
	LinearLayout l_layout3,l_layout3_inner;
	CheckBox cb_togo,cb_toDliver;
	SharedPreferences addItemPre;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
		View view = inflater.inflate(R.layout.add_item_page3, container,false);
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		//取得畫面元件
		edt_email = (EditText)view.findViewById(R.id.editText_email);
		edt_web = (EditText)view.findViewById(R.id.editText_store_web);
		l_layout3 = (LinearLayout)view.findViewById(R.id.additemp3_layout);
		edt_phone = (EditText)view.findViewById(R.id.editText_phone);
		edt_price = (EditText)view.findViewById(R.id.editText_price);
		//Checkbox
		cb_toDliver = (CheckBox)view.findViewById(R.id.checkBox_CanDelivery);
		cb_togo = (CheckBox)view.findViewById(R.id.checkBox_CanToGo);
		//設定監聽
		l_layout3.setOnTouchListener(layout_listener);
		cb_toDliver.setOnCheckedChangeListener(cb_listener);
		cb_togo.setOnCheckedChangeListener(cb_listener);
		edt_phone.setOnFocusChangeListener(focusChangeListener);
		edt_phone.setOnFocusChangeListener(focusChangeListener);
		edt_email.setOnFocusChangeListener(focusChangeListener);
		edt_web.setOnFocusChangeListener(focusChangeListener);
	    return view;
	}
	
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			switch (v.getId()) {
			case R.id.editText_email:
				if (!hasFocus)
					inputWorker.putString("sEmail", edt_email.getText().toString());
				break;
			case R.id.editText_store_web:
				if (!hasFocus)
					inputWorker.putString("sURL", edt_web.getText().toString());
				break;
			case R.id.editText_price:
				if (!hasFocus)
					inputWorker.putString("sMinCharge", edt_price.getText().toString());
				break;
			case R.id.editText_phone:
				if (!hasFocus)
					inputWorker.putString("sPhone", edt_phone.getText().toString());
				break;
			}
			inputWorker.commit();
		}
	};
	
	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			int sCanDelivery, sCanToGo;
			if (cb_toDliver.isChecked()) 
				sCanDelivery = 1;
			else 
				sCanDelivery = 0;
	        if (cb_togo.isChecked())
	        	sCanToGo = 1;
	        else 
	        	sCanToGo = 0;
	        inputWorker.putInt("sCanDelivery", sCanDelivery);
	        inputWorker.putInt("sCanToGo", sCanToGo);
	        inputWorker.commit();
		}
	};
	
	private OnTouchListener layout_listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				informationGeter();
				break;
			}
			return false;
		}
	};
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.forceinput, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forceinput:
			informationGeter();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void informationGeter() {
        String email = edt_email.getText().toString();
        String web_url = edt_web.getText().toString();
        String store_price = edt_price.getText().toString();
        String store_phone = edt_phone.getText().toString();
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		Editor inputWorker = addItemPre.edit();
		inputWorker.putString("sMinCharge", store_price);
		inputWorker.putString("sPhone", store_phone);
		inputWorker.putString("sURL", web_url);
		inputWorker.putString("sEmail", email);
		inputWorker.commit();
	}
	
	/*離開Fragment時寫入Bundle*/
    @Override
	public void onDestroyView(){
        super.onDestroyView();
        informationGeter();
    }
}
