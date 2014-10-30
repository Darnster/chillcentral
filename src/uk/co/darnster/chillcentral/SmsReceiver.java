package uk.co.darnster.chillcentral;
/*
 * Add the context of this class to the call to CommsHandler context in order for the db call to work !!!!
 * 
 * Need to consider where process is terminated if the application is disabled or SMS blocking is disabled.
 * 
 * 29/7/13 - externlisation of strings complete
 * 2/8/13 - Dlog added
 * 
 */


import uk.co.darnster.chillcentral.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	//may not need var below......
	//private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	
	 public SharedPreferences appSettings;
		
	 public static final String APP_SETTINGS = "AppSettings"; // String to hold
	 public static final String RULES_EXIST = "RulesExist"; // String
	 private boolean rulesExist = false; // default is not intercept

	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//---get the SMS message passed in---
		// check ShardPrefs to see if rules exist
		appSettings = context.getSharedPreferences(APP_SETTINGS, 0);
		if(appSettings.contains(RULES_EXIST)) {
		rulesExist = appSettings.getBoolean(RULES_EXIST, false);  // default to block???
		}
		
		if (rulesExist == true) {
	        Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
	        String action ="";
	        action = intent.getAction();
	        
	        if (Dlog.getState()) {
	        	Dlog.d(this.getClass().toString() + " action = " + action);
	        }
	               
	        
	        if (bundle != null)
	        {
	            //---retrieve the SMS message received---
	            Object[] pdus = (Object[]) bundle.get("pdus");
	            msgs = new SmsMessage[pdus.length]; 
	            // create instance of CommsHandler here so we don't have to create for multiple records
	            CommsHandler cH = new CommsHandler();
	            for (int i=0; i<msgs.length; i++){
	            	String strSMS = ""; String phoneNumber = "";
	                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);   
	                // pick out the phne Number here
	                phoneNumber = msgs[i].getOriginatingAddress();
	                // substitute a string for unknown/witheld numbers
	                if (phoneNumber == ""){
	    	        	phoneNumber = "unknown";
	    	        }
	                String message = msgs[i].getMessageBody().toString();
	                // handle empty message
	                if (message =="") {
	                	message = "empty message block";
	                }
	                strSMS += "SMS from " +  phoneNumber;                  
	                strSMS += " :";
	                strSMS += msgs[i].getMessageBody().toString();
	                strSMS += "\n"; 
	                
	                if (Dlog.getState()) {
	                	Dlog.d(this.getClass().toString() + " strSMS = " + strSMS);
	                }
	                
	                try 
	                {
	                // need to check boolean return value and block the text
	                if (cH.handleEvent(phoneNumber, "SMS", message, context)){
	                	//Toast.makeText(context, strSMS, Toast.LENGTH_LONG).show(); 
	                    // http://stackoverflow.com/questions/1741628/can-we-delete-an-sms-in-android-before-it-reaches-the-inbox/2566199#2566199
	                    // thank you for the command below
	                    abortBroadcast();
	                }
	                } catch (Exception e) {
	        			Toast.makeText(context, R.string.unable_to_pass_sms_to_handler, Toast.LENGTH_LONG).show();
	      				e.printStackTrace();
	        		}
	            }
	        }

		} // end Rules Exist check
	}

}

