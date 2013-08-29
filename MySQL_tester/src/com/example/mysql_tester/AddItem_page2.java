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
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class AddItem_page2 extends SherlockFragment {
	SharedPreferences addItemPre;
	//�]�wLinearLayout
	//Checkbox
	CheckBox cb_w1,cb_w2,cb_w3,cb_w4,cb_w5,cb_w6,cb_w7,cb_holiday,is24Hr;
	LinearLayout l_layout2;
	LinearLayout l_layout2_inner;
	int is24Hours = 0;
	private static TextView txt_Start,txt_Close;
	private Button btn_set_start,btn_set_close;
	private static int mHour;
	private static int mMinute;
	static int switcher = 0;
	EditText edt_Memo;
	String startTime,endTime,start,end;
	static String start_backup,end_backup;
	boolean isFirstTime = true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
		View view = inflater.inflate(R.layout.add_item_page2, container,false);
		//�q�������o����
		txt_Start = (TextView)view.findViewById(R.id.textView_store_start);
		txt_Close = (TextView)view.findViewById(R.id.textView_store_end);
		btn_set_start = (Button)view.findViewById(R.id.button_add_start_time);
		btn_set_close = (Button)view.findViewById(R.id.button_add_close_time);
		l_layout2 = (LinearLayout)view.findViewById(R.id.additemp2_layout);
		edt_Memo = (EditText)view.findViewById(R.id.editText_sMemo);
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		if (isFirstTime) {
			addItemPre.edit().putString("startTime", "").commit();
			addItemPre.edit().putString("closeTime", "").commit();
			isFirstTime = false;
		}
		start = addItemPre.getString("startTime","");
		end = addItemPre.getString("closeTime","");
		start_backup = start;
		end_backup = end;
		if (!start.equals("") || !end.equals("")){
			txt_Start.setText(start);
			txt_Close.setText(end);
		}
		//CheckBox
		is24Hr = (CheckBox)view.findViewById(R.id.is24hr_addItem);
		cb_w1 = (CheckBox)view.findViewById(R.id.checkBox_w1);
		cb_w2 = (CheckBox)view.findViewById(R.id.checkBox_w2);
		cb_w3 = (CheckBox)view.findViewById(R.id.checkBox_w3);
		cb_w4 = (CheckBox)view.findViewById(R.id.checkBox_w4);
		cb_w5 = (CheckBox)view.findViewById(R.id.checkBox_w5);
		cb_w6 = (CheckBox)view.findViewById(R.id.checkBox_w6);
		cb_w7 = (CheckBox)view.findViewById(R.id.checkBox_w7);
		cb_holiday = (CheckBox)view.findViewById(R.id.checkBox_holiday);
		//�]�w��ť
		is24Hr.setOnCheckedChangeListener(hours_listener);
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
	
	private CheckBox.OnCheckedChangeListener hours_listener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			if (is24Hr.isChecked()) {
				is24Hours = 1;
				txt_Start.setText("00:00");
				txt_Close.setText("24:00");
				inputWorker.putString("startTime", "00:00");
				inputWorker.putString("closeTime", "24:00");
				btn_set_start.setClickable(false);
				btn_set_close.setClickable(false);
				btn_set_start.setBackgroundColor(Color.parseColor("#808080"));
				btn_set_close.setBackgroundColor(Color.parseColor("#808080"));
			}
			else {
				is24Hours = 0;
				if (start_backup.equals("") || end_backup.equals("")) {
					final String start_word = getActivity().getResources().getString(R.string.Item_startTime);
					final String end_word = getActivity().getResources().getString(R.string.Item_endTime);
					txt_Start.setText(start_word);
					txt_Close.setText(end_word);
				}
				txt_Start.setText(start_backup);
				txt_Close.setText(end_backup);
				inputWorker.putString("startTime", start);
				inputWorker.putString("closeTime", end);
				btn_set_start.setClickable(true);
				btn_set_close.setClickable(true);
				btn_set_start.setBackgroundResource(android.R.drawable.btn_default);
				btn_set_close.setBackgroundResource(android.R.drawable.btn_default);
			}
			inputWorker.putInt("is24Hours", is24Hours);
			inputWorker.commit();
		}
	};
	
	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			String rest_day=""; //�`
			String w1,w2,w3,w4,w5,w6,w7,w8; //��@�ﶵ
			if (cb_w1.isChecked())
				w1 = cb_w1.getText().toString() +" ";
			else
				w1 = "";
			if (cb_w2.isChecked())
				w2 = cb_w2.getText().toString() +" ";
			else
				w2 = "";
			if (cb_w3.isChecked())
				w3 = cb_w3.getText().toString() +" ";
			else
				w3 = "";
			if (cb_w4.isChecked())
				w4 = cb_w4.getText().toString() +" ";
			else
				w4 = "";
			if (cb_w5.isChecked())
				w5 = cb_w5.getText().toString() +" ";
			else
				w5 = "";
			if (cb_w6.isChecked())
				w6 = cb_w6.getText().toString() +" ";
			else
				w6 = "";
			if (cb_w7.isChecked())
				w7 = cb_w7.getText().toString() +" ";
			else
				w7 = "";
			if (cb_holiday.isChecked())
				w8 = cb_holiday.getText().toString() +" ";
			else
				w8 = "";
			rest_day = w1 + w2 + w3 + w4 + w5 + w6 + w7 + w8;
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			inputWorker.putString("sWeek", rest_day);
			inputWorker.commit();
		}
	};
	
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
				Editor inputWorker = addItemPre.edit();
				inputWorker.putString("sMemo", edt_Memo.getText().toString());
				inputWorker.commit();
			}
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
			return true;
		}
	};
	
	private Button.OnClickListener btn_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			TimePickerFragment timePickerFragment = new TimePickerFragment();
			FragmentManager fm = getActivity().getSupportFragmentManager();
		    switch (v.getId()) {
		    case R.id.button_add_start_time:
		    	switcher = 1;
				timePickerFragment.show(fm, "timePicker");
			    break;
            case R.id.button_add_close_time:
            	switcher = 0;
				timePickerFragment.show(fm, "timePicker");
			    break;
		    }
		}
	};
	//��sTextView���ɶ��Ϊ�
	private static void updateDisplay(int switcher) {
		//StringBuilder sb = new StringBuilder().append(pad(mHour)).append(":").append(pad(mMinute));
		String times = pad(mHour) +":" +pad(mMinute);
		if (switcher == 1){
			txt_Start.setText(times);
			start_backup = times;
		}
		else {
			txt_Close.setText(times);
			end_backup = times;
		}
	}
	//�ɶ��ɹs�Ϊ�
	private static String pad(int c) {
		if (c >= 10) {
			return String.valueOf(c);
		}
		else {
			return "0" + String.valueOf(c);
		}
	}
	//��ܮɶ�������
	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstenceBundle) {
			//�إ�TimePickerDialog����
			//this �� OnTimeListener ����
			//mHour,mMinute�|�����ɶ��D�ﾹ�w�諸�ɻP��
			//false �N����24�ɨ�
			TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, mHour, mMinute, false);
			return timePickerDialog;
		}
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateDisplay(switcher);
		}
	}
	
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
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		Editor inputWorker = addItemPre.edit();
        if (!startTime.equals(getActivity().getString(R.string.Item_startTime)) 
        	|| !endTime.equals(getActivity().getString(R.string.Item_endTime))) {
        	inputWorker.putString("startTime", startTime);
        	inputWorker.putString("closeTime", endTime);
        }
        String memo = edt_Memo.getText().toString();
        inputWorker.putString("sMemo", memo);
        inputWorker.commit();
	}
	
	/*���}Fragment�ɼg�JBundle*/
    @Override
	public void onDestroyView(){
        super.onDestroyView();
        informationGeter();
    }
}
