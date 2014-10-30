package uk.co.darnster.chillcentralbase;

/*
 * 
 *  Still need to add a listener to"Enable Blocking" checkbox to disable "Notify Blocking" and "Remove Calls from Log" (DONE)
 *  12/7/13 String abstraction complete
  * 2/8/13 - Dlog added
 * 
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

// public class ConfigActivity extends BlankActivity {
	

public class ConfigFragment extends Fragment implements OnClickListener {
	public static final String BLOCK = "Block"; // String
	public static final String NOTIFY = "Notify"; // String
	public static final String REMOVE_CALLS = "RemoveCalls"; // String
	public static final String TEST = "TestNumber"; // String
	public static final String EULA = "Eula"; // String
	public static final String TAG = "chill central"; // String for Dlog debug statements

	public boolean boolBlock, boolNotify, boolRemoveCalls;
	public String stringTest;
	public CheckBox cbBlock, cbNotify, cbRemoveCall, cbDevLogging;
	public EditText txtTestNumber;
	public Button saveButton;
	public SharedPreferences appSettings;
	
	public static final String APP_SETTINGS = "AppSettings"; // String to hold
	public static Context ctx;
	private int layoutView = R.layout.frag_config;
	public static View vFrag;
	
	//constructor
	//public ConfigActivity(Context context) {
	//	ctx = context;
	//}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		ctx = getActivity();
		vFrag = inflater.inflate(layoutView, container, false);
		
				
		// find UI widgets		
		cbBlock = (CheckBox) vFrag.findViewById( R.id.masterEnable );
		cbNotify = (CheckBox) vFrag.findViewById( R.id.masterNotify );
		cbDevLogging = (CheckBox) vFrag.findViewById( R.id.enableDeviceLogging );
		txtTestNumber = (EditText) vFrag.findViewById( R.id.testNumber );
		cbRemoveCall = (CheckBox) vFrag.findViewById(R.id.removeFromCallLog);
		saveButton = (Button) vFrag.findViewById(R.id.saveConfig);

		// Read preferences
		appSettings = ctx.getSharedPreferences(APP_SETTINGS, 0);
		boolBlock = appSettings.getBoolean(BLOCK, false);
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " boolBlock in ConfigActivity = " + boolBlock);
		}
		boolNotify = appSettings.getBoolean(NOTIFY, false);
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " boolNotify in ConfigActivity = " + boolNotify);
		}
		boolRemoveCalls = appSettings.getBoolean(REMOVE_CALLS, false);
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " boolRemoveCalls in ConfigActivity = " + boolRemoveCalls);
		}
		if (Dlog.getState()){  // not a persisted preference
			cbDevLogging.setChecked(true);
		}
		else
		{
			cbDevLogging.setChecked(false);
		}
		stringTest = appSettings.getString(TEST, "");
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " boolInit in ConfigActivity = " + stringTest);
		}
		
		// set the UI widgets
		cbBlock.setChecked(boolBlock);
		cbNotify.setChecked(boolNotify);
		cbRemoveCall.setChecked(boolRemoveCalls);
		txtTestNumber.setText(stringTest);
		
		
		//Disable other checkboxes if app is not enabled
		View vBlock = cbBlock;
		handleEnable(vBlock);
		
		// Add Listeners
		// masterEnable
        cbBlock.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	handleEnable(v);
            }
         });

        // Save Config
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	saveConfig(v);
            }
         });

        
        
		return vFrag;
	}
	
	 public static ConfigFragment newInstance(Context context) {
		 ctx = context;
		 ConfigFragment f = new ConfigFragment();
	   return f;

	 }
	 
	 public void saveConfig(View vFrag) {
		 // read all edits from the UI
		 // master enable
		 if ( cbBlock.isChecked() ) { boolBlock = true; } else { boolBlock = false; }
		 
		 //Notify 
		 if ( cbNotify.isChecked() ) { boolNotify = true; } else { boolNotify = false; }
		 
		 // Remove Call Logs
		 if ( cbRemoveCall.isChecked() ) { boolRemoveCalls = true; } else { boolRemoveCalls = false; }
		 
		 // Enable device logging
	     if ( cbDevLogging.isChecked() ) 
	     	{ Dlog.setState( true ); } 
	     else 
	       { Dlog.setState( false ); }
		 
		 // Test Phone Number
		 stringTest = txtTestNumber.getText().toString();
		 
		 // write to SharedPrefs
		 SharedPreferences appSettings = ctx.getSharedPreferences(APP_SETTINGS, 0);
		 SharedPreferences.Editor prefEditor = appSettings.edit();
		 prefEditor.putBoolean(BLOCK, boolBlock);
		 prefEditor.putBoolean(NOTIFY, boolNotify);
		 prefEditor.putBoolean(REMOVE_CALLS, boolRemoveCalls);
		 prefEditor.putString(TEST, stringTest);
		 prefEditor.commit();
		 
		 Toast.makeText(getActivity(), R.string.config_settings_saved,Toast.LENGTH_LONG).show();
		
		 startActivity(new Intent(ConfigFragment.ctx,MainActivity.class));
	 }
	 
	 public void handleEnable(View v) {
		 if ( cbBlock.isChecked() ) { 
			 cbNotify.setEnabled(true);
			 cbRemoveCall.setEnabled(true);
		 } else { 
			 cbNotify.setEnabled(false);
			 cbRemoveCall.setEnabled(false);
		 }
	 }
	 


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
}
