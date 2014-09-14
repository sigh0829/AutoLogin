package com.cedar.autologin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SelectSsidActivity extends ActionBarActivity implements WifiListFragment.onDlgListClick, AddDialog.onDlgListClick {
	
	ListView ssidList = null;
	SsidAdapter adapter = null;
	Button addButton = null;
	ArrayList<String> ssidArray = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_ssid);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		SharedPreferences sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		Set<String> ssidSet = sp.getStringSet("ssid", new HashSet<String>(Arrays.asList("seu-wlan")));
		ssidArray = new ArrayList<String>();
		ssidArray.addAll(ssidSet);
		
		ssidList = (ListView) findViewById(R.id.ssidList);
		if (ssidList != null) {
			adapter = new SsidAdapter(this, ssidArray);
			ssidList.setAdapter(adapter);
		}
		
		addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	AddDialog addDialog = AddDialog.newInstance();
            	AddDialog.fm = getSupportFragmentManager();
            	addDialog.show(getSupportFragmentManager(), "fragment_add_ssid");
            }
        });
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		Set<String> ssidSet = new HashSet<String>();
		for (int i=0; i<adapter.getCount(); i++) {
			ssidSet.add(adapter.getItem(i));
		}
		
	    SharedPreferences.Editor editor = getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();
	    editor.putStringSet("ssid", ssidSet);

	    editor.commit();
	    Log.d("ssidSet", ssidSet.toString());
	}

	@Override
	public void addSsid(String ssid) {
		Log.d("addSsid", ssid);
		if (ssid.equals("") || ssidArray.contains(ssid))
			return;
		ssidArray.add(ssid);
		 
		adapter = new SsidAdapter(this, ssidArray);
		ssidList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
	}

	private class SsidAdapter extends BaseAdapter {

		private List<String> listItems;
		private LayoutInflater inflater;

		public SsidAdapter(Context context, List<String> listItems) {
			inflater = LayoutInflater.from(context);
			this.listItems = listItems;
		}

		public int getCount() {
			return listItems.size();
		}

		public String getItem(int position) {
			return listItems.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final int selectID = position;
			
		    if (convertView == null) {
		    	convertView = inflater.inflate(R.layout.ssid_info, parent, false);
		    }

		    TextView ssidText = (TextView) convertView.findViewById(R.id.ssid_name);
		    Button rmButton = (Button) convertView.findViewById(R.id.button_rm);


			ssidText.setText((String) listItems.get(position));

			rmButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					rmSsid(selectID);
				}
			});

			return convertView;
		}

		private void rmSsid(int clickID) {
			Log.d("listItems", listItems.toString());
			Log.d(String.valueOf(clickID), listItems.get(clickID));
			listItems.remove(clickID);
			Log.d("listItems", listItems.toString());
			this.notifyDataSetChanged();
		}
	}


}


class AddDialog extends DialogFragment {
	
	static FragmentManager fm;
	onDlgListClick mCallback;
	
    public AddDialog() {
    }

    public static AddDialog newInstance() {
    	AddDialog frag = new AddDialog();
        return frag;
    }
    

	public interface onDlgListClick{
        public void addSsid(String ssid);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onDlgListClick) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement addSsid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ssid, container);
        
        getDialog().setTitle("����SSID");
        
        Button selectButton = (Button) view.findViewById(R.id.button_select);
		selectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				WifiListFragment wifiFragment = new WifiListFragment();
				wifiFragment.dlg = getDialog();
				wifiFragment.show(fm, "show wifi list");
			}
		});
		
		final EditText ssidText = (EditText) view.findViewById(R.id.ssidText);
		
		Button OkButton = (Button) view.findViewById(R.id.button_ok);
		OkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mCallback.addSsid(ssidText.getText().toString());
				getDialog().dismiss();
			}
		});
        
        return view;
    }
    
    @Override
    public void onStop() {
        if( getActivity() != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            View v = getActivity().getCurrentFocus();
            Window w = getActivity().getWindow();
            if (v != null) 
            	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (w != null)
            	w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onStop();
    }

}


class WifiListFragment extends DialogFragment {

	onDlgListClick mCallback;
	
	Dialog dlg = null;
	
	ArrayList<String> connections = new ArrayList<String>();

	public interface onDlgListClick{
        public void addSsid(String ssid);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onDlgListClick) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement addSsid");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

    	WifiManager mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> wifiList = mainWifi.getScanResults();
   	 	for(int i = 0; i < wifiList.size(); i++) {
   	 		String ssid = wifiList.get(i).SSID;
   	 		if (!(ssid.equals("")))
   	 			connections.add(wifiList.get(i).SSID);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("WiFi List")
                .setItems(connections.toArray(new String[connections.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                    	mCallback.addSsid(connections.get(item));
                        getDialog().dismiss(); 
                        WifiListFragment.this.dismiss();
                        if (dlg != null)
                        	dlg.dismiss();

                    }
                }).create();

    }
}