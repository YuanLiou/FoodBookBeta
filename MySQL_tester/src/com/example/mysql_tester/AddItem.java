package com.example.mysql_tester;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddItem extends SherlockFragment {
	ActionBar actionbar;
	EditText input_Name,input_Address,input_Township;
	TextView address_sample;
	LinearLayout layout1,layout1_inner;
	String input_country = null , current = "";
	int sample_loaded = 0;
	//�U�Ԧ���檫��
	Spinner countrySpinner;
	String[] country;
	SharedPreferences addItemPre;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
		View view = inflater.inflate(R.layout.add_item, container,false);
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		//�q�������o����
		input_Name = (EditText)view.findViewById(R.id.editText_name);
		input_Address = (EditText)view.findViewById(R.id.editText_address);
		input_Township = (EditText)view.findViewById(R.id.editText_township);
		countrySpinner = (Spinner)view.findViewById(R.id.spinner_country);
		address_sample = (TextView)view.findViewById(R.id.addItem_address_sample);
        //Layout��ť
        layout1 = (LinearLayout)view.findViewById(R.id.additemp1_layout);
        //layout1_inner = (LinearLayout)view.findViewById(R.id.additemp1_layout_inner);
        layout1.setOnTouchListener(layoutlistener);
        //layout1_inner.setOnTouchListener(layoutlistener);
        input_Name.setOnFocusChangeListener(focusChangeListener);
        input_Township.setOnFocusChangeListener(focusChangeListener);
        input_Address.setOnFocusChangeListener(focusChangeListener);
        //�]�w����X��AŪ��Bundle
        if (sample_loaded == 0) {
        	Handler myhandler = new Handler();
  	  		myhandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					current = getActivity().getString(R.string.ItemP1_currentCountry);
					String txt_AddressSample = addItemPre.getString("sampleAdd1", "");
					String txt_Township = addItemPre.getString("sampleAdd2","");
					String txt_Address = addItemPre.getString("sampleAdd3","");
					String txt_Country = addItemPre.getString("sampleAdd4","");
					if (txt_AddressSample != null && !txt_AddressSample.equals(""))
						address_sample.setText(txt_AddressSample);
					else
						address_sample.setText(getActivity().getString(R.string.ItemTab_dataGetError));
					if (txt_Township != null && !txt_Township.equals(""))
						input_Township.setText(txt_Township);
					if (txt_Address != null && !txt_Address.equals(""))
						input_Address.setText(txt_Address);
					if (txt_Country != null && !txt_Country.equals(""))
						current = txt_Country;
					else
						current = getActivity().getString(R.string.ItemP1_plzChooseCountry);
					//country = new String[] {current,"�O�_��","�s�_��","�O����","�O�n��","������","�򶩥�","��鿤","�s�˿�","�s�˥�","�]�߿�","���ƿ�","�n�뿤","���L��","�Ÿq��","�Ÿq��","�̪F��","�y����","�Ὤ��","�O�F��","���","������","�s����"};
					country = getActivity().getResources().getStringArray(R.array.country);
					country[0] = current;
					spinnerBulider();
				}
			}, 500);
        	sample_loaded = 1;
        }
        else {
			address_sample.setText(addItemPre.getString("sampleAdd1", getActivity().getString(R.string.ItemTab_dataGetError)));
        	spinnerBulider();
        }
        return view;
	}

	public void spinnerBulider() {
	    //�إ�ArrayAdapter
        ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, country);
        //�]�wSpinner����ܮ榡
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //�]�wSpinner����ƨӷ�
        countrySpinner.setAdapter(adapterCountry);
        //�]�wSpinner���󪺺�ť
        countrySpinner.setOnItemSelectedListener(spinnerlistener);
	}

	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			switch (v.getId()) {
			case R.id.editText_name:
				if (!hasFocus)
					inputWorker.putString("sName", input_Name.getText().toString());
				break;
			case R.id.editText_address:
				if (!hasFocus)
					inputWorker.putString("sLocation", input_Address.getText().toString());
				break;
			case R.id.editText_township:
				if (!hasFocus)
					inputWorker.putString("sTownship", input_Township.getText().toString());
				break;
			}
			inputWorker.commit();    //�g�J
		}
	};

	private OnTouchListener layoutlistener = new OnTouchListener() {
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

	//Spinner����ťMethod
	private Spinner.OnItemSelectedListener spinnerlistener = new Spinner.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			input_country = parent.getSelectedItem().toString();
			if (position == 0 && input_country.equals(getActivity().getString(R.string.ItemP1_plzChooseCountry)))
				Toast.makeText(getActivity(), getActivity().getString(R.string.ItemP1_plzChooseCountryToast), Toast.LENGTH_SHORT).show();
			else {
				addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
				Editor inputWorker = addItemPre.edit();
				inputWorker.putString("sCountry", input_country);
				inputWorker.commit();
			}
			    
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	/*�T���m�J�p����*/
	public void informationGeter() {
        String store_name,store_sTownship,store_sLocation;
        store_name = input_Name.getText().toString();
        store_sTownship = input_Township.getText().toString();
        store_sLocation = input_Address.getText().toString();
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		Editor inputWorker = addItemPre.edit();
		inputWorker.putString("sName", store_name);
		inputWorker.putString("sTownship", store_sTownship);
		inputWorker.putString("sLocation", store_sLocation);
		inputWorker.commit();
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
			//Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*���}Fragment�ɼg�JBundle*/
    @Override
	public void onDestroyView(){
        super.onDestroyView();
        informationGeter();
    }
}
