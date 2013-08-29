package com.example.mysql_tester;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class EditItem_page2 extends SherlockFragment {
	SharedPreferences editItemPre;
	int is24Hours = 0;
	CheckBox cb_w1, cb_w2, cb_w3, cb_w4, cb_w5, cb_w6, cb_w7, cb_holiday,
			cb_is24Hour;
	LinearLayout l_layout2;
	LinearLayout l_layout2_inner;
	TextView txt_restdays;
	private static TextView txt_Start, txt_Close;
	private Button btn_set_start, btn_set_close;
	private static int mHour;
	private static int mMinute;
	static String start_backup,end_backup;
	static int switcher = 0;
	EditText edt_Memo;
	String startTime, endTime, start, end;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceBundle) {
		View view = inflater
				.inflate(R.layout.edit_item_page2, container, false);
		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
		Editor editWriter = editItemPre.edit();
		// 從介面取得物件
		txt_restdays = (TextView) view
				.findViewById(R.id.textView_restdays_edit);
		txt_Start = (TextView) view.findViewById(R.id.textView_store_start2);
		txt_Close = (TextView) view.findViewById(R.id.textView_store_end2);
		btn_set_start = (Button) view.findViewById(R.id.button_add_start_time2);
		btn_set_close = (Button) view.findViewById(R.id.button_add_close_time2);
		Button btn_set_restReset = (Button) view
				.findViewById(R.id.btn_restReset);
		btn_set_restReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
				Editor editWriter = editItemPre.edit();
				editWriter.putString("sWeek", "");
				editWriter.commit();
				cb_w1.setChecked(false);
				cb_w2.setChecked(false);
				cb_w3.setChecked(false);
				cb_w4.setChecked(false);
				cb_w5.setChecked(false);
				cb_w6.setChecked(false);
				cb_w7.setChecked(false);
				cb_holiday.setChecked(false);
				txt_restdays.setText("");
			}
		});
		l_layout2 = (LinearLayout) view.findViewById(R.id.editItem_layout_p2);
		// l_layout2_inner =
		// (LinearLayout)view.findViewById(R.id.editItem_layout_p2_inner);
		edt_Memo = (EditText) view.findViewById(R.id.editText_sMemo2);
		// 從Bundle取得資訊
		is24Hours = editItemPre.getInt("is24Hours", 0);
		start = editItemPre.getString("startTime", "");
		end = editItemPre.getString("closeTime", "");
		start_backup = start;
		end_backup = end;
		if (!start.equals("") || !end.equals("")) {
			txt_Start.setText(start);
			txt_Close.setText(end);
		}
		cb_is24Hour = (CheckBox) view.findViewById(R.id.is24hr_editItem);
		if (is24Hours == 1) {
			cb_is24Hour.setChecked(true);
			btn_set_start.setClickable(false);
			btn_set_close.setClickable(false);
			btn_set_start.setBackgroundColor(Color.parseColor("#808080"));
			btn_set_close.setBackgroundColor(Color.parseColor("#808080"));
		}
		txt_restdays.setText(editItemPre.getString("sWeek", ""));
		edt_Memo.setText(editItemPre.getString("sMemo", ""));
		editWriter.putInt("tab2_viewed", 1); // 這是？？？
		editWriter.commit();
		// CheckBox
		cb_w1 = (CheckBox) view.findViewById(R.id.checkBox_w1_editItem);
		cb_w2 = (CheckBox) view.findViewById(R.id.checkBox_w2_editItem);
		cb_w3 = (CheckBox) view.findViewById(R.id.checkBox_w3_editItem);
		cb_w4 = (CheckBox) view.findViewById(R.id.checkBox_w4_editItem);
		cb_w5 = (CheckBox) view.findViewById(R.id.checkBox_w5_editItem);
		cb_w6 = (CheckBox) view.findViewById(R.id.checkBox_w6_editItem);
		cb_w7 = (CheckBox) view.findViewById(R.id.checkBox_w7_editItem);
		cb_holiday = (CheckBox) view
				.findViewById(R.id.checkBox_holiday_editItem);
		// 設定監聽
		cb_is24Hour.setOnCheckedChangeListener(hourListener);
		btn_set_close.setOnClickListener(btn_listener);
		btn_set_start.setOnClickListener(btn_listener);
		l_layout2.setOnTouchListener(layout_listener);
		cb_w1.setOnCheckedChangeListener(cb_listener);
		cb_w2.setOnCheckedChangeListener(cb_listener);
		cb_w3.setOnCheckedChangeListener(cb_listener);
		cb_w4.setOnCheckedChangeListener(cb_listener);
		cb_w5.setOnCheckedChangeListener(cb_listener);
		cb_w6.setOnCheckedChangeListener(cb_listener);
		cb_w7.setOnCheckedChangeListener(cb_listener);
		cb_holiday.setOnCheckedChangeListener(cb_listener);
		edt_Memo.setOnFocusChangeListener(focusChangeListener);
		return view;
	}

	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
				Editor editWriter = editItemPre.edit();
				editWriter.putString("sMemo", edt_Memo.getText().toString());
				editWriter.commit();
			}

		}
	};

	private CheckBox.OnCheckedChangeListener hourListener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
			Editor editWriter = editItemPre.edit();
			if (cb_is24Hour.isChecked()) {
				is24Hours = 1;
				txt_Start.setText("00:00");
				txt_Close.setText("24:00");
				editWriter.putString("startTime", "00:00");
				editWriter.putString("closeTime", "24:00");
				btn_set_start.setClickable(false);
				btn_set_close.setClickable(false);
				btn_set_start.setBackgroundColor(Color.parseColor("#808080"));
				btn_set_close.setBackgroundColor(Color.parseColor("#808080"));
			} else {
				is24Hours = 0;
				if (start_backup.equals("") || end_backup.equals("")) {
					final String start_word = getActivity().getResources().getString(R.string.Item_startTime);
					final String end_word = getActivity().getResources().getString(R.string.Item_endTime);
					txt_Start.setText(start_word);
					txt_Close.setText(end_word);
				}
				txt_Start.setText(start_backup);
				txt_Close.setText(end_backup);
				editWriter.putString("startTime", start);
				editWriter.putString("closeTime", end);
				btn_set_start.setClickable(true);
				btn_set_close.setClickable(true);
				btn_set_start
						.setBackgroundResource(android.R.drawable.btn_default);
				btn_set_close
						.setBackgroundResource(android.R.drawable.btn_default);
			}
			editWriter.putInt("is24Hours", is24Hours);
			editWriter.commit();
		}
	};

	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			String rest_day = ""; // 總
			String w1, w2, w3, w4, w5, w6, w7, w8; // 單一選項
			if (cb_w1.isChecked())
				w1 = cb_w1.getText().toString() + " ";
			else
				w1 = "";
			if (cb_w2.isChecked())
				w2 = cb_w2.getText().toString() + " ";
			else
				w2 = "";
			if (cb_w3.isChecked())
				w3 = cb_w3.getText().toString() + " ";
			else
				w3 = "";
			if (cb_w4.isChecked())
				w4 = cb_w4.getText().toString() + " ";
			else
				w4 = "";
			if (cb_w5.isChecked())
				w5 = cb_w5.getText().toString() + " ";
			else
				w5 = "";
			if (cb_w6.isChecked())
				w6 = cb_w6.getText().toString() + " ";
			else
				w6 = "";
			if (cb_w7.isChecked())
				w7 = cb_w7.getText().toString() + " ";
			else
				w7 = "";
			if (cb_holiday.isChecked())
				w8 = cb_holiday.getText().toString() + " ";
			else
				w8 = "";
			rest_day = w1 + w2 + w3 + w4 + w5 + w6 + w7 + w8;
			editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
			Editor editWriter = editItemPre.edit();
			editWriter.putString("sWeek", rest_day);
			editWriter.commit();
		}
	};

	private Button.OnClickListener btn_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			TimePickerFragment timePickerFragment = new TimePickerFragment();
			FragmentManager fm = getActivity().getSupportFragmentManager();
			switch (v.getId()) {
			case R.id.button_add_start_time2:
				switcher = 1;
				timePickerFragment.show(fm, "timePicker");
				break;
			case R.id.button_add_close_time2:
				switcher = 0;
				timePickerFragment.show(fm, "timePicker");
				break;
			}
		}
	};

	// 更新TextView的時間用的
	private static void updateDisplay(int switcher) {
		String times = pad(mHour) + ":" + pad(mMinute);
		if (switcher == 1) {
			txt_Start.setText(times);
			start_backup = times;
		} 
		else {
			txt_Close.setText(times);
			end_backup = times;
		}
	}

	// 時間補零用的
	private static String pad(int c) {
		if (c >= 10) {
			return String.valueOf(c);
		} else {
			return "0" + String.valueOf(c);
		}
	}

	// 選擇時間的視窗
	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstenceBundle) {
			// 建立TimePickerDialog物件
			// this 為 OnTimeListener 物件
			// mHour,mMinute會成為時間挑選器預選的時與分
			// false 代表不為24時制
			TimePickerDialog timePickerDialog = new TimePickerDialog(
					getActivity(), this, mHour, mMinute, false);
			return timePickerDialog;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateDisplay(switcher);
		}
	}

	private OnTouchListener layout_listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				informationGeter();
				break;
			}
			return true;
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
		startTime = txt_Start.getText().toString();
		endTime = txt_Close.getText().toString();
		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
		Editor editWriter = editItemPre.edit();
		if (!startTime.equals(getActivity().getString(R.string.Item_startTime))
				|| !endTime.equals(getActivity().getString(R.string.Item_endTime))) {
			editWriter.putString("startTime", startTime);
			editWriter.putString("closeTime", endTime);
		}
		String memo = edt_Memo.getText().toString();
		editWriter.putString("sMemo", memo);
		editWriter.commit();
	}

	/* 離開Fragment時寫入Bundle */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		informationGeter();
	}
}
