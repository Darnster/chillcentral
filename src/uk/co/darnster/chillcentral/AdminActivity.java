package uk.co.darnster.chillcentral;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AdminActivity extends BlankActivity {
	
	public static final String APP_SETTINGS = "AppSettings"; // String to hold
	// using this to set App Prefs in config screen
	public static final String INIT = "Init"; // String
	public static final String BLOCK = "Block"; // String
	public static final String NOTIFY = "Notify"; // String
	public static final String REMOVE_CALLS = "RemoveCalls"; // String
	public static final String TEST = "TestNumber"; // String
	public static final String CHANGELOG = "ChangeLog"; // String
	public static final String EXPIRY = "expiry"; // String
	public static final String EXPIRY_PERIOD = "expiry_period"; // String
	public static final String EXPIRY_NOTIFY = "expiry_notify"; // String 
	public static final String EXPIRED = "expired"; // expired status (will use for temporary passes)
	public static final String RULES_EXIST = "RulesExist"; // expired status (will use for temporary passes)

	private SharedPreferences appSettings;
	private SharedPreferences.Editor prefEditor; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin);
		
		appSettings = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
		prefEditor = appSettings.edit();
		 
		// Get all config items
		//expiry period
		EditText expiryPeriodText = (EditText) findViewById( R.id.expiryPeriodText);
    	if (appSettings.contains(EXPIRY_PERIOD)) {
			Integer expiryPeriod = appSettings.getInt(EXPIRY_PERIOD, 5);
			expiryPeriodText.setText(expiryPeriod.toString());
		 }
		else
		{ expiryPeriodText.setText("n/a"); }
		
    	// expiry notification starts x days
		EditText expiryNotifyText = (EditText) findViewById( R.id.expiryNotifyText);		
		if (appSettings.contains(EXPIRY_NOTIFY)) {
			Integer expiryNotify = appSettings.getInt(EXPIRY_NOTIFY, 6);
			expiryNotifyText.setText(expiryNotify.toString());
		 }
		else
		{ expiryNotifyText.setText("n/a"); }

		// has app expired
		EditText expiredText = (EditText) findViewById( R.id.expiredText);
		if (appSettings.contains(EXPIRED)) {
			boolean expired = appSettings.getBoolean(EXPIRED, true);
			String expiredString;
			if (expired == true) {
				expiredString = "yes";
			}
			else
			{ expiredString = "no"; }
			expiredText.setText(expiredString);
		 }
		else
		{ expiredText.setText("n/a"); }
		
		// do rules exist
		EditText rulesExistText = (EditText) findViewById( R.id.rulesExistText);	
		if (appSettings.contains(RULES_EXIST)) {
			boolean expired = appSettings.getBoolean(RULES_EXIST, true);
			String rulesExistString;
			if (expired == true) {
				rulesExistString = "yes";
			}
			else
			{	rulesExistString = "no"; }
			rulesExistText.setText(rulesExistString);
		 }
		
		EditText expiryText = (EditText) findViewById( R.id.expiryTimeText);
		long expiryTime = 0;
		if (appSettings.contains(RULES_EXIST)) {
			expiryTime = appSettings.getLong(EXPIRY,0);
			expiryText.setText( getHumanDateTime(expiryTime) );
			if (Dlog.getState()){
				Dlog.i(this.getClass().toString() + " expiryTime = " + expiryTime);
			}
		}
		else {
			expiryText.setText("could not determine");
		}
		
		

		
		} // end onCreate
	
	
	public String getHumanDateTime(long DateTime) {
		// GET CURRENT TIME
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(DateTime * 1000);
		WeekDays wD = new WeekDays();
		  String am_pm, minuteString;
		  
		  String weekDay = wD.getDay(Integer.toString(calendar.get(Calendar.DAY_OF_WEEK)), this);
		  
		  if (Dlog.getState()) {
			  Dlog.i(this.getClass().toString() + " weekDay = " + weekDay );
		  }
		  String day = weekDay;
		  int dateDay = calendar.get(Calendar.DAY_OF_MONTH);
		  int month = calendar.get(Calendar.MONTH) + 1; // not sure why this is needed - might be zero based
		  int year = calendar.get(Calendar.YEAR);
		  int hour = calendar.get(Calendar.HOUR);
		  int minute = calendar.get(Calendar.MINUTE);
		  minuteString = "";
		  if (minute < 10 || minute == 0) {
			  minuteString = "0" + Integer.toString(minute); 
		  }
		  else
		  {
			  minuteString = Integer.toString(minute);
		  }
		  if(calendar.get(Calendar.AM_PM) == 0) am_pm = "AM"; else am_pm = "PM";
		  if(calendar.get(Calendar.AM_PM) != 0 && hour == 0) hour = 12; // required because Calendar.HOUR is a 12 hour clock therefore 12:01 is shown as 0:01PM  !!!! 
		  String currentTime = day +", " + dateDay + "/" + month + "/" + year + "--" + hour + ":" + minuteString + " " + am_pm;

		return currentTime;
		
		
	}
/*
 * Need to think about what I want to do here
 * Extending the expiry time is one thing that I need to do, by passing in (days to expiry, days to notificication)
 * and then setting that in the app settings (nice if it shows a formatted date in the UI)
 * 
 * Turning Dlog on and off
 * Exporting debug information (from database?)
 * 
 */
	
	public void saveSettings(View v) {
		
		Toast.makeText(this, "Still need to implement update of App Settings in here", Toast.LENGTH_SHORT).show(); //\nPlease press the back button.

		/*prefEditor.putInt(EXPIRY_PERIOD, 3); // # days before app expires
		prefEditor.putInt(EXPIRY_NOTIFY, 1); // period at which user is notified
		prefEditor.putString(EXPIRY_NOTIFY, ""); // expiry time in EPOC
		prefEditor.putBoolean(EXPIRED, false); // flag to use to determine whether the app has expired
		// also need to do something with Dlog
		
		prefEditor.commit();
*/
	}
	
	

	public void onSearchClick(View v) {
		EditText editTextInput = (EditText) findViewById(R.id.editTextInput);
		try {
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			String term = editTextInput.getText().toString();
			intent.putExtra(SearchManager.SUGGEST_URI_PATH_QUERY, term);
			startActivity(intent);
			} catch (Exception e) {
			// TODO: handle exception
		}

	}
	
	public void writeContactDetail(View v) {
		ContactsHandler contH = new ContactsHandler();
		try {
			contH.getAllContacts(this);
			Toast.makeText(this, "Request to save contact data issued", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
