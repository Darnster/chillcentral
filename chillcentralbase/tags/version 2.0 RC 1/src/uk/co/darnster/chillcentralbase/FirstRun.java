package uk.co.darnster.chillcentralbase;

/* this class will handle anything that the App needs to set up when it is first launched
 * 
 * Likely to be bypassed after install - apart from changelog dialog.
 * 
 * 
 * 
 * 
 * 
 * 
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class FirstRun {
	
	public static Context ctx;
	public SharedPreferences appSettings;
	// shared prefs for App Settings
	public static final String APP_SETTINGS = "AppSettings"; // String to hold
	// using this to set App Prefs in config screen
	public static final String INIT = "Init"; // String
	public static final String BLOCK = "Block"; // String
	public static final String NOTIFY = "Notify"; // String
	public static final String REMOVE_CALLS = "RemoveCalls"; // String
	public static final String TEST = "TestNumber"; // String
	public static final String CHANGELOG = "ChangeLog"; // String
	public static final String RULES_EXIST = "RulesExist"; // set and updated as and when rules are added/removed

	


public FirstRun(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
	ctx = mainActivity;
	
	}



public void runInit() {
	if (checkAPIVersion())
		{
		Initialse();
		}
	else
	{
		ctx.startActivity(new Intent(ctx, AndroidVersionFailActivity.class));
		} 
	}


public boolean checkAPIVersion() {
	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	
	if (Dlog.getState()) {
		Dlog.i(this.getClass().toString() + " apiVersion = " + android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH);
		Dlog.i(this.getClass().toString() + " currentapiVersion = " + currentapiVersion);
	}
	
	return (currentapiVersion > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH);
	}

public void Initialse() {
   
    	// set the defaults
    			        	
        // initialise App Prefs
        /*
         * Read a shared pref "init"
         * If it doesn't exist
         * 		Set the initial values when the screen is loaded
         * 		Then create "init" and set it to true
         */
		appSettings = ctx.getSharedPreferences(APP_SETTINGS, 0);
		SharedPreferences.Editor prefEditor = appSettings.edit();
		prefEditor.putBoolean(BLOCK, true); // master switch
		prefEditor.putBoolean(NOTIFY, true); // controls whether a notification appears in status bar ans notifications lit
		prefEditor.putBoolean(REMOVE_CALLS, false); // determines whether calls are removed form the call log
		// add INIT to prevent this code from being called again
		prefEditor.putBoolean(INIT, true); // flag to allow subsequent loads to recognise that defaults are set
		prefEditor.putString(TEST, ctx.getString(R.string.test_number));
		prefEditor.putBoolean(RULES_EXIST, false); // added to control whether app kicks off commshandler
		
		prefEditor.commit();
   
	}

}
	
	
	
	


