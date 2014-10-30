/*
 * @author Danny
 * @version 0.1
 *
 *
 * Note that major breakthrough in this area when context passed in to CommsHandler object :-)
 *
 * TO DO
 * unknown number is passed through to CommsHandler class - still need to add something to handle this. 
 * Done - "Unknown Callers" group resolves this - I think! 19/7/13
 * 
 * Need to consider where process is terminated if the application is disabled or blocking phone calls only is disabled.
 * 
 * Need to work out how to disable global notification - see code at:
 *  http://stackoverflow.com/questions/2334465/block-incoming-calls   and
 *  http://stackoverflow.com/questions/12038856/how-to-stop-telephone-ringing-when-get-call-but-not-mute-it-globally
 *  
 *  29/7/13 - String externalisation complete
 * 
 *  1/8/13 - Noticed that the audiomanager code was setting to NORMAL RINGER MODE rather than what it was set to before it was set to silent - fixed, not tested.
 *  1/8/13 - Also removed getSystemService method as Eclipse spotted this wan't being used.
 *  2/8/13 - Dlog added
 */


package uk.co.darnster.chillcentralbase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import java.lang.reflect.Method;

import uk.co.darnster.chillcentralbase.R;

import com.android.internal.telephony.ITelephony; 

public class PhoneReceiver extends BroadcastReceiver {

	 Context context = null;
	 private String phoneNumber = "";
	 private ITelephony telephonyService;
	 public SharedPreferences appSettings;
		
	 public static final String APP_SETTINGS = "AppSettings"; // String to hold
	 public static final String REMOVE_CALLS = "RemoveCalls"; // String
	 public static final String RULES_EXIST = "RulesExist"; // String
	 
	 public AudioManager aManager;
	 
	 private boolean clearLog;
	 private boolean rulesExist = false; // default is not intercept
	 
	 @Override
	 public void onReceive(Context context, Intent intent) {
		 
		// check ShardPrefs to see if rules exist
		appSettings = context.getSharedPreferences(APP_SETTINGS, 0);
		if(appSettings.contains(RULES_EXIST)) {
		rulesExist = appSettings.getBoolean(RULES_EXIST, false);  // default to block???
		}
		String state;
		if (rulesExist == true) {
	    Bundle extras = intent.getExtras();
	    if (extras != null) {
	      state = extras.getString(TelephonyManager.EXTRA_STATE);
	      if (Dlog.getState()) {
	    	  Dlog.d(this.getClass().toString() + " phone state " + state);
	      }
	      if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
	        try {
	        	phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
	        }
	        catch (Exception e) {
     		   e.printStackTrace();
     		  Toast.makeText(context,R.string.unable_to_access_incomming_number , Toast.LENGTH_LONG).show();
     		 }
	        // substitute a string for unknown/witheld numbers
	        if (Dlog.getState()) {
	        	Dlog.d(this.getClass().toString() + " Phone number = " + phoneNumber);
	        }
	        CommsHandler cH = new CommsHandler();
	        
	        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
	        		  try {
	        		   Class<?> c = Class.forName(telephony.getClass().getName());
	        		   Method m = c.getDeclaredMethod("getITelephony");
	        		   m.setAccessible(true);
	        		   telephonyService = (ITelephony) m.invoke(telephony);
	        		  } catch (Exception e) {
	        		   e.printStackTrace();
	        		   Toast.makeText(context, R.string.unable_to_access_telephony_service , Toast.LENGTH_LONG).show();
	        		  }
	        		  AudioManager aManage = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
      			  	  int phoneRingerMode = aManage.getRingerMode();
      			  	  if (Dlog.getState()) {
      			  		Dlog.d(this.getClass().toString() + " ringer mode before block: " + Integer.toString(phoneRingerMode));
      			  	}
	        		  try {
	        			// need to check boolean return value and block the call
	        			  	if (cH.handleEvent(phoneNumber, "CALL", "", context)){
	        			  		aManage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		        				// http://androidsourcecode.blogspot.in/ thank you very much for the line below:	        			  		
		        			  	telephonyService.endCall();
		        			  	// check share prefs to see if blocking is enabled (at long last!)
		        		        // App Config - Shared Prefs
		        		    	clearLog = appSettings.getBoolean(REMOVE_CALLS, false);  // default to block???
		        		    	if ( clearLog ) {
				        			  	// call object to perform delete query (subclass of Thread)
				        			  	DelayClearCallLog DelayClear = new DelayClearCallLog(context, phoneNumber);
				        			  	DelayClear.start();
		        		    		}
		        		    	// now reset ringer mode
		        		    	aManage.setRingerMode(phoneRingerMode);
		        		    	if (Dlog.getState()) {
		        		    		Dlog.d(this.getClass().toString() + " ringer mode after block: " + Integer.toString(phoneRingerMode));
		        		    	}

	        			  	}
	        			  	else
	        			  	{
	        			  		aManage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	        			  		if (Dlog.getState()) {
	        			  			Dlog.d(this.getClass().toString() + " ringer mode no block: " + Integer.toString(phoneRingerMode));
	        			  		}
	        			  	}
	        		  
	        		  
	        		  } catch (Exception e) {
	        			Toast.makeText(context, R.string.unable_to_pass_call_to_handler, Toast.LENGTH_LONG).show();
	      				e.printStackTrace();
	        		  }
	      }
	    }
	  } // end Rules Exist check
	 }
	 
} 

