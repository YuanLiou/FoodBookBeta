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
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

public class EditItem_page3 extends SherlockFragment {
	EditText edt_email,edt_web,edt_price,edt_phone;
	LinearLayout l_layout3,l_layout3_inner;
	CheckBox cb_togo,cb_toDliver;
	int sw_togo,sw_todliver;
  	SharedPreferences editItemPre;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
  		View view = inflater.inflate(R.layout.edit_item_page3, container,false);
  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
  		Editor editWriter = editItemPre.edit();
		//取得畫面元件
		edt_email = (EditText)view.findViewById(R.id.editText_email_editItem);
		edt_web = (EditText)view.findViewById(R.id.editText_store_web_editItem);
		l_layout3 = (LinearLayout)view.findViewById(R.id.editItem_layout_p3);
		edt_price = (EditText)view.findViewById(R.id.editText_price_editItem);
		edt_phone = (EditText)view.findViewById(R.id.editItem_phone);
		//Checkbox
		cb_toDliver = (CheckBox)view.findViewById(R.id.checkBox_CanDelivery_editItem);
		cb_togo = (CheckBox)view.findViewById(R.id.checkBox_CanToGo_editItem);
		//從Bundle取得物件
  		edt_phone.setText(editItemPre.getString("sPhone", ""));
  		edt_price.setText(editItemPre.getString("sMinCharge", ""));
		edt_email.setText(editItemPre.getString("sEmail", ""));
		edt_web.setText(editItemPre.getString("sURL", ""));
		sw_togo = editItemPre.getInt("sCanToGo", 0);
		sw_todliver = editItemPre.getInt("sCanDelivery", 0);
		//Log.d("sw_togo = ", ""+sw_togo);
		//Log.d("sw_todiliver = ", ""+sw_todliver);
		if (sw_togo == 1) {
			cb_togo.setChecked(true);
			editWriter.putInt("sCanToGo", 1);
			editWriter.commit();
		}
		if (sw_todliver == 1) {
			cb_toDliver.setChecked(true);
			editWriter.putInt("sCanDelivery", 1);
			editWriter.commit();
		}
		//設定監聽
		edt_price.setOnFocusChangeListener(focusChangeListener);
		edt_phone.setOnFocusChangeListener(focusChangeListener);
		edt_email.setOnFocusChangeListener(focusChangeListener);
		edt_web.setOnFocusChangeListener(focusChangeListener);
		l_layout3.setOnTouchListener(layout_listener);
		cb_toDliver.setOnCheckedChangeListener(cb_listener);
		cb_togo.setOnCheckedChangeListener(cb_listener);
	    return view;
	}
	
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
	  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
	  		Editor editWriter = editItemPre.edit();
			switch (v.getId()) {
			case R.id.editText_email_editItem:
				if (!hasFocus)
					editWriter.putString("sEmail", edt_email.getText().toString());
				break;
			case R.id.editText_store_web_editItem:
				if (!hasFocus)
					editWriter.putString("sURL", edt_web.getText().toString());
				break;
			case R.id.editText_price_editItem:
				if (!hasFocus)
					editWriter.putString("sMinCharge", edt_price.getText().toString());
			    break;
			case R.id.editItem_phone:
				if (!hasFocus)
					editWriter.putString("sPhone", edt_phone.getText().toString());
				break;
			}
			editWriter.commit();
		}
	};
	
	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int sCanDelivery, sCanToGo;
			if (cb_toDliver.isChecked()) 
				sCanDelivery = 1;
			else 
				sCanDelivery = 0;
	        if (cb_togo.isChecked())
	        	sCanToGo = 1;
	        else 
	        	sCanToGo = 0;
	  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
	  		Editor editWriter = editItemPre.edit();
	  		editWriter.putInt("sCanDelivery", sCanDelivery);
	  		editWriter.putInt("sCanToGo", sCanToGo);
	  		editWriter.commit();
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
		String price = edt_price.getText().toString();
		String phone = edt_phone.getText().toString();
        String email = edt_email.getText().toString();
        String web_url = edt_web.getText().toString();
  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
  		Editor editWriter = editItemPre.edit();
  		editWriter.putString("sMinCharge", price);
  		editWriter.putString("sPhone", phone);
  		editWriter.putString("sURL", web_url);
  		editWriter.putString("sEmail", email);
  		editWriter.commit();
	}
	
	/*離開Fragment時寫入Bundle*/
    @Override
	public void onDestroyView(){
        super.onDestroyView();
        informationGeter();
    }
}
