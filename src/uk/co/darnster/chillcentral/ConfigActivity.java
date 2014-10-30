package uk.co.darnster.chillcentral;

/*
 * 
 *  Still need to add a listener to"Enable Blocking" checkbox to disable "Notify Blocking" and "Remove Calls from Log" (DONE)
 *  12/7/13 String abstraction complete
  * 2/8/13 - Dlog added
 * 
 */

import android.os.Bundle;
import android.view.View;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;


public class ConfigActivity extends BlankActivity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config); 

		// find UI widgets		
		cbBlock = (CheckBox) findViewById( R.id.masterEnable );
		cbNotify = (CheckBox) findViewById( R.id.masterNotify );
		cbRemoveCall = (CheckBox) findViewById( R.id.removeFromCallLog );
		cbDevLogging = (CheckBox) findViewById( R.id.enableDeviceLogging );
		txtTestNumber = (EditText) findViewById( R.id.testNumber );

		
		// Read preferences
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
		View v = cbBlock;
		handleEnable(v);
		
       }
	 
	 public void saveConfig(View v) {
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
		 SharedPreferences appSettings = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
		 SharedPreferences.Editor prefEditor = appSettings.edit();
		 prefEditor.putBoolean(BLOCK, boolBlock);
		 prefEditor.putBoolean(NOTIFY, boolNotify);
		 prefEditor.putBoolean(REMOVE_CALLS, boolRemoveCalls);
		 prefEditor.putString(TEST, stringTest);
		 prefEditor.commit();
		 
		 Toast.makeText(this, R.string.config_settings_saved,Toast.LENGTH_LONG).show();
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
	 
}
