package com.example.mysql_tester;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.internal.br;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditItem extends SherlockFragment {
	LinearLayout editItem_layout1,editItem_layout1_inner;
	Spinner country;
	EditText edt_township,edt_location;
	TextView txt_name;
	String country_now,current_name;
	String[] current_country;
	int switch_loading = 0;
  	SharedPreferences editItemPre;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
  	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
  		View view = inflater.inflate(R.layout.edit_item, container,false);
  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
  		//�q�ù����o����
  		country = (Spinner)view.findViewById(R.id.spinner_country_editItem);
  		txt_name = (TextView)view.findViewById(R.id.textView_name_editItem);
  		edt_township = (EditText)view.findViewById(R.id.editText_township_editItem);
  		edt_location = (EditText)view.findViewById(R.id.editText_address_editItem);
  		edt_township.setOnFocusChangeListener(focusChangeListener);
  		edt_location.setOnFocusChangeListener(focusChangeListener);
  		//LinearLayout
  		editItem_layout1 = (LinearLayout)view.findViewById(R.id.editItem_layout_p1);
  		//editItem_layout1_inner = (LinearLayout)view.findViewById(R.id.editItem_layout_p1_inner);
  		editItem_layout1.setOnTouchListener(layout_listener);
  		//editItem_layout1_inner.setOnTouchListener(layout_listener);
  		if (switch_loading == 0) {
  			country_now = getActivity().getString(R.string.editItemP1_currentCountry);
			current_name = editItemPre.getString("sName", "");
	  		getActivity().setTitle(current_name);
	  		txt_name.setText(current_name);
	  		edt_township.setText(editItemPre.getString("sTownship", getActivity().getString(R.string.editItemP1_countryError)));
	  		edt_location.setText(editItemPre.getString("sLocation", getActivity().getString(R.string.editItemP1_countryError)));
	  		country_now = editItemPre.getString("sCountry", getActivity().getString(R.string.editItemP1_countryError));
	  		//current_country = new String[] {country_now,"�O�_��","�s�_��","�O����","�O�n��","������","�򶩥�","��鿤","�s�˿�","�s�˥�","�]�߿�","���ƿ�","�n�뿤","���L��","�Ÿq��","�Ÿq��","�̪F��","�y����","�Ὤ��","�O�F��","���","������","�s����"};
		    current_country = getActivity().getResources().getStringArray(R.array.country);
		    current_country[0] = country_now;
	  		spinnerBuilder();
  	  		switch_loading = 1;
  		}
  		else {
	  		//�q Bundle ���o���
	  		current_name = editItemPre.getString("sName", "");
	  		getActivity().setTitle(current_name);
	  		txt_name.setText(current_name);
	  		edt_township.setText(editItemPre.getString("sTownship", ""));
	  		edt_location.setText(editItemPre.getString("sLocation", ""));
	  		spinnerBuilder();
  		}
        return view;
  	}
  	
  	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
			Editor putWriter = editItemPre.edit();
			switch (v.getId()) {
			case R.id.editText_township_editItem:
				if (!hasFocus)
					putWriter.putString("sTownship", edt_township.getText().toString());
				break;
			case R.id.editText_address_editItem:
				if (!hasFocus)
					putWriter.putString("sLocation", edt_location.getText().toString());
				break;
			}
			putWriter.commit();    //�g�J
		}
	};
  	
  	public void spinnerBuilder() {
	 		//�إ� ArrayAdapter
	  		ArrayAdapter<String> adapter_country = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, current_country);
	  		//�]�wSpinner����ܮ榡
	  		adapter_country.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	  		//�]�wSpinner����ƨӷ�
	  		country.setAdapter(adapter_country);
	  		//�]�wSpinner��ť
	  		country.setOnItemSelectedListener(spin_listener);
  	}
  	
  	private Spinner.OnItemSelectedListener spin_listener = new Spinner.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
			Editor putWriter = editItemPre.edit();
			if (switch_loading == 1){
				if (position == 0) {
					putWriter.putString("sCountry", country_now);
				}
				else {
					country_now = parent.getSelectedItem().toString();	
					putWriter.putString("sCountry", country_now);
				}		
				putWriter.commit();    //�g�J
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
  	};
  	
  	private OnTouchListener layout_listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (switch_loading == 1){
					informationGeter();
				}
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
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.action_forceinput:
			informationGeter();
			break;
		}
		return true;
	}
	
	public void informationGeter() {
		String township,location;
		township = edt_township.getText().toString();
		location = edt_location.getText().toString();
		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
		Editor putWriter = editItemPre.edit();
		putWriter.putString("sTownship", township);
		putWriter.putString("sLocation", location);
		putWriter.putString("sCountry", country_now);
		putWriter.commit();
	}
  	
	/*���}Fragment�ɼg�JBundle*/
    @Override
	public void onDestroyView(){
        super.onDestroyView();
		if (switch_loading == 1){
            informationGeter();
        }
    }
}
