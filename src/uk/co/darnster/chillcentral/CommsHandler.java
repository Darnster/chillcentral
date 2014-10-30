/**
 *
 * @author Danny
 * @version 0.1
 *
 * Not sure how the event that needs to be handled will be passed in yet
 * 
 * slightly bodged version - need to sort out data between calls to checkCalaendar
 * 
 * 
 * This will be called once the app has picked up an event (phone ringing or SMS)
 * 
 */
package uk.co.darnster.chillcentral;

// Imports for Contacts query 

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import uk.co.darnster.chillcentral.R;


import Database.DatabaseHelper;
import android.app.Notification; // leave in for now
import android.app.NotificationManager; // leave in for now
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.widget.Toast;

/*
 * @author Danny
 * @version 0.1
 *
 * 
 * TO DO
 * 
 * Need to consider where process is terminated if the application is disabled - Done
 * 
 * Removed checkActive rules constants and replace with field names instead
 * e.g. private static final int RULE_ID = 0; etc..
 * 
 * 20/1/13
 * Updated checkActiveRules to exit loops when an ActiveRule is encountered
 * 
 * Added some audit stuff in there - will need to revisit prior to GoLive
 * 
 * Still need to handle truly unknown/witheld numbers
 * 12/7/13 String abstraction complete
 * 14/7/13 Also updated calls to CalendarHandler and getEvents
 * 2/8/13 Dlog added
 */

public class CommsHandler {

	public String commsType; // cell|sms .... |email|calendar reminder
	//private String groupDetail; // used to hold all groups a user is associated with - may cast to array
	//private String tagDetail; // used to hold any tags for a given group - may cast to array
	//private String activeRules; // eventually will be a nested array of data from a db cursor
	
	// database vars
	private DatabaseHelper db;
	private SQLiteDatabase wpDB;
	private Cursor dbCursor;
	private String sqlResult;
	
	// checkActive rules constants (maps to field positions in RULES table)
	private static final int RULE_ID = 0;
	private static final int GROUP_NAME = 1;
	private static final int TAG = 2;
	private static final int CALENDAR_ID = 3;
	private static final int CALENDAR_NAME = 4;
	private static final int BLOCK_CALL = 5;
	private static final int BLOCK_SMS = 6;

	public HashMap<String, String> activeRule = new HashMap<String, String>();
	
	// defined here so the contact details can be retrieved following the check to see if the contact exists - if required
	private ContactsHandler contH = new ContactsHandler();
	
	// Has to store contact_id and contact name
	private HashMap<String, String> contactDetail = new HashMap<String, String>();
	
	// Used to store contact number and name from contactsHandler class
	private HashMap<String, String> cDetail;
	
	// HashMap to store full records against 
	//private HashMap<Integer, HashMap> activeRules = new HashMap<Integer, HashMap>();  // ? may not be needed !!??
	
	private String eventString;  // defined to allow the type of activity to be audited
	private String ruleID;
	private String messageString; // used to store SMS
	
	public SharedPreferences appSettings;
	
	public static final String APP_SETTINGS = "AppSettings"; // String to hold 
	
	public static final String BLOCK = "Block"; // String to identify in shared prefs
	public static final String NOTIFY = "Notify"; // String to identify in shared prefs
	
	// using this to set App Prefs in config screen
	public Boolean isEnabled;
	public Boolean notifyUser;
	
	
	
	
	public boolean handleEvent(String phoneNumber, String eventType, String message, Context ctx) {
		/* Code here will be handling:
		 * - phone calls
		 * - SMS messages
		 * 
		 * In future may be handling:
		 * - Calendar Events
		 * - Email notifications
		 * - Facebook or Linked in messaging
		 * 
		 * unknown users are passed through - need to consider rules for these.
		 * 
		 * Also need to check for witheld numbers???
		 * 
		 * Decided to change the processing order to look for active tags before deciding on querying user groups
		 * 
		 * 
		 * **** RETURN TYPE NEEDS TO BE BOOLEAN - SO PHONE/SMS RECEIVERS CAN BLOCK THE COMMS ****
		 */
		// set class var eventString for audit
		eventString = eventType; messageString = message;
		// log incoming data
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " handleEvent " + phoneNumber + " " + eventType);
		}
    	
        appSettings = ctx.getSharedPreferences(APP_SETTINGS, 0);
        // check share prefs to see if blocking is enabled (at long last!)
        // App Config - Shared Prefs
    	isEnabled = appSettings.getBoolean(BLOCK, true);  // default to block???
    	notifyUser = appSettings.getBoolean(NOTIFY, false); // default don't notify??? 

    	
    	if (isEnabled == true) {  
			
			if (contactExists(phoneNumber, ctx)) {
				// get groups for user
				if (getContactdetail(ctx)) 
					{
					if (checkActiverules(ctx, false)) // false arg means user exists
						{
							// If tags defined <for each to be added later> check Calendar(s)
							// need to document what data IS actually required!!!!
							// List<String> groupDetail = new ArrayList<String>();
							// for each tag stored:
							// see if the user is in the contacts database on the phone
							
							// At this point (version 1) Just need to see if SMS needs blocking too
							if (applyRule(eventString)) {
								message += ". " +  R.string.active_rule_found;
								notifyTheUser( ctx );
								cDetail = contH.getContactDetail();
								logDetail( message, ctx );
								
								//############################ VOICEMAIL
	        			  		// read sendToVoiceMail (from rule)
	        			  		boolean sendToVoiceMail = true;  // set so it steps over for now
	        			  		if (!sendToVoiceMail) {
	        			  			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	        			  			ops.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
	        			  			    .withSelection(ContactsContract.RawContacts._ID + "=?", new String[]{cDetail.get("contactID").toString()})
	        			  			    .withValue(ContactsContract.RawContacts.SEND_TO_VOICEMAIL, 1)
	        			  			    .build());

	        			  			try { 
	        			  			    ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
	        			  			} catch (Exception e) { 
	        			  			    Dlog.e("Exception: " + e.getMessage()); 
	        			  			}
	        			  			
	        			  			return false;  // allow call to go to voice mail
	        			  		}	        			  		
	        								
								//############################
	        			  		else
	        			  		{
	        			  			return true;
	        			  		}
							} else {
								return false;
							}
						}  
						else {
							//logDetail("No active rules found in database.", ctx);
							// only log if wp takes action!
							return false;
							} // end of check for checkActiverules
						}
				else
					{
						return false;
					}
				}
				else 
				{
					
				 /* Need to consider how to handle unknown contacts
				 * 
				 * For the moment we can log that it's not of interest using the log/audit class
				 */
				//logDetail("Incomming: " + phoneNumber + ", contact not known", ctx);
				// only log if wp takes action!
				if (checkActiverules(ctx, true) ) {
					if (applyRule(eventString)) {
						message += ". " +  R.string.active_rule_found;
						notifyTheUser( ctx );
						// Get contact details from contactsHandler
						cDetail = contH.getContactDetail();  //initialise here (will have blank entries)
						// populate here
						cDetail.put("phoneNumber", phoneNumber);
						cDetail.put("contactName", "unknown caller");  // this appears in the log
						/*if (messageString.equals("") ) {
							messageString= "Unknown";
						}*/
						logDetail( message, ctx );
						return true;
					} else {
						return false;
					}
				}
				else {
					return false;  //no Active Rules with Unknown Callers
				}
				} // end of check for contactExists
			}
			else {
			return false;
			} // end of check for isEnabled
		
	}

	
	
	
	private boolean contactExists(String phoneNumber, Context ctx) {
		/*
		 * Query Contact Resolver.contacts
		 * 
		 */
		contactDetail = contH.getContact(phoneNumber, ctx);
		
		if (contactDetail.size() > 0) {
			return true;	
		}
		else {
			return false;
		}
		
	}
	
	
	private boolean getContactdetail(Context ctx) {
		/*	 
		 * Group membership required to determine whether a user should be blocked
		 * Gets user info for user notification and logging
		 * Need to think about precedence here for >1 rule / group (Pro version)
		 * 
		 * Hang on - major change in approach 2/12/12
		 * 
		 * Just need to get the groups then pass back the boolean result to the main process 
		 * Then use class groupMember variable within checkActiveRules
		 * 
		 */
		boolean hasGroupMembership = false; //default
		String contactID = contactDetail.get("contactID");
		
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " getContactdetail contactID = " + contactID);
			}
		if (contH.checkGroupMembership(contactID, ctx))  // will evaluate to true/false
			{
			hasGroupMembership = true;
			if (Dlog.getState()) {
				Dlog.d(this.getClass().toString() + " getContactdetail hasGroupMembership = True");
				}
			}

		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " getContactdetail hasGroupMembership = False");
		}
			return hasGroupMembership;
	}
	

	private boolean checkActiverules(Context ctx, boolean unKnown) {
		/* 
		 * Call to Database of configured groups and tags
		 * need to return boolean value to feed into flow 
		 * The rules store in the database needs to be stored in a class variable 
		 * and re-use cache the data returned for user notification and audit
		 * 
		 * Major design change 2/12/12
		 * 
		 * need to get getontactGroupNames from contH
		 * Need to import CalendarHandler to check for active tags
		 * 
		 * Loop over rules from DB and then loop over contactGroupNames to build call to Calendar handler
		 * 
		 * Updated to handle unknown callers
		*/
		
		boolean groupMatch = false;
		boolean activeRulesExist = false;
	
		String sql = "select * from RULE;";
		String matchedGroup = "";  // holds the actual group that triggered the block (not all defined in the rule)
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " checkActiverules " + sql);
		}
        db = new DatabaseHelper(ctx);
        try {
			wpDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(ctx, ctx.getString(R.string.err_unable_to_connect_to_database), Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
        
		dbCursor = wpDB.rawQuery(sql, null);
		
		//Integer recordCount = 0;  //? not needed as it breaks out of loop when it finds a match!!!!???
		
		/* 
		 *  All Callers - perform quick loop to determine if group checking should be skipped :-)  abstract method for call to calendarHandler
		 */
				
		groupMatch = checkAllCallers(ctx, dbCursor);
		if (groupMatch) {
			matchedGroup = ctx.getString(R.string.allCallers);  // set in group processing and we are skipping, so set here
		}
		dbCursor.requery();
		
		while (	dbCursor.moveToNext()) {
			// HashMap to to save individual record output
			ruleID = dbCursor.getString(RULE_ID);
			String group = dbCursor.getString(GROUP_NAME);  // can now be multi-valued - eeeek!
			String tag = dbCursor.getString(TAG);
			String calendarID = dbCursor.getString(CALENDAR_ID);
			String calendar = dbCursor.getString(CALENDAR_NAME);
			String callBlock = dbCursor.getString(BLOCK_CALL);
			String smsBlock = dbCursor.getString(BLOCK_SMS);
			
			// split groups 
			String [] groupsArray = group.split(", ");
			if (Dlog.getState()) {
				Dlog.d(this.getClass().toString() + " checkActiverules - groupsArray " + groupsArray.toString());
				}
			for(int x = 0; x < groupsArray.length; x++ )
				{			
				
								
				//***************** CHECK HERE ****** UNKNOWN CALLERS HANDLED ALSO **********
				
				if (!groupMatch) {
					if (unKnown == true) {
						String uc = ctx.getString(R.string.Unknown_Callers);
						if (groupsArray[x].toString().equals( uc )) {
							matchedGroup = uc;
							groupMatch = true;
						}
						}
						else {
							if (contH.getcontactGroupNames().contains(groupsArray[x])) {
								matchedGroup = groupsArray[x].toString();
								groupMatch = true;
							}
						}
				}		
				//
				if (groupMatch) // groupMatch is true
				{
					CalendarHandler calHandler = new CalendarHandler(ctx);
						if (calHandler.getEvent(calendarID, tag).size() > 0) 
						{
							activeRulesExist = true; // exit here as we have a match
							// add values to activeRule HashMap
							activeRule.put("id", ruleID);
							activeRule.put("group", matchedGroup);
							activeRule.put("tag", tag);
							activeRule.put("calendarID", calendarID);
							activeRule.put("calendar", calendar);
							activeRule.put("callBlock", callBlock);
							activeRule.put("smsBlock", smsBlock);
							
							
							if (Dlog.getState()) {
								// debug statements (prior to hashmap logic, but should still work)
								sqlResult +=  "id: " + ruleID + "\n";
								sqlResult +=  "group: " + group + "\n";
								sqlResult +=  "tag: " + tag + "\n";
								sqlResult +=  "calendar: " + calendar + "\n";
								sqlResult +=  "calendarID: " + calendarID + "\n";
								sqlResult +=  "callBlock: " + callBlock + "\n";
								sqlResult +=  "smsBlock: " + smsBlock + "\n";
								Dlog.d(this.getClass().toString() + " checkActiverules " + sqlResult);
								}
							
					
						break;
					}
				}
				 	
			}  // end for loop - groupArray
			
			// set the return value when loop is exited via break or not finding a match
			if (activeRulesExist == true ){
				break;  // exit here as we have a match
				}
			else  // only close cursor and db here if nothing to log
			{
				dbCursor.close();
				wpDB.close();
			}
			
		} // end while loop - dbCursor
		 
		//dbCursor.close();
		//wpDB.close();
		return activeRulesExist;
	}
	
	
	
	private boolean applyRule(String evtString) {
		/*
		 * Now reading the block preference from the RULE record
		 * 
		 */
		boolean applyBlockRule = false;
		
		////// process from here!
		
		if ("CALL".equals(evtString)) {
			applyBlockRule = true;
		} 
		else { // can only be CALLS or SMS in this version
			if ("SMS".equals(evtString) ) {
				String smsBlock = activeRule.get("smsBlock").toString();
				if ( smsBlock.equals( "BLOCK")) {
					applyBlockRule =  true;
				} else {
					applyBlockRule = false;
				}
			}
		}
		return applyBlockRule;
		
	}
	
	private void notifyTheUser(Context ctx) {
		// stubbed for now with Toasts
		
		if (notifyUser) {
			
		    //http://stackoverflow.com/questions/6391870/how-exactly-to-use-notification-builder
		    		    
		    Intent notificationIntent = new Intent(ctx, LogActivity.class);
		    PendingIntent contentIntent = PendingIntent.getActivity(ctx, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		    NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		    Resources res = ctx.getResources();
		    Notification.Builder builder = new Notification.Builder(ctx);

		    builder.setContentIntent(contentIntent)
		                .setSmallIcon(R.drawable.ic_notification)
		                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_notification))
		                .setTicker(res.getString(R.string.notification_ticker))
		                .setWhen(System.currentTimeMillis())
		                .setAutoCancel(true)
		                .setContentTitle(res.getString(R.string.notification_title))
		                .setContentText(res.getString(R.string.notification_content));
		    Notification n = builder.getNotification();
		    nm.notify(1, n);  
		} 
		
	}

	
		
	private void logDetail(String message, Context ctx) {
		/* async call to log component
		 * insert record into audit table - as follows
		 * ID INTEGER PRIMARY KEY AUTOINCREMENT,
		 *  PHONE_NUMBER TEXT, 
		 *  CONTACT_NAME TEXT, 
		 *  RULE_ID NUMBER, 
		 *  EVENT_TYPE TEXT,
		 *  CONTENT TEXT, 
		 *  DATE_TIME NUMBER);");
		 *
		 *
		 * ContactID
		 * contactName
		 * groupID
		 * contactGroups
		 * matchGroup
		 */
		
	// GET CURRENT TIME
	Calendar calendar = new GregorianCalendar();
	WeekDays wD = new WeekDays();
	  String am_pm, minuteString;
	  
	  String weekDay = wD.getDay(Integer.toString(calendar.get(Calendar.DAY_OF_WEEK)),ctx);
	  if (Dlog.getState()) {
		  Dlog.d(this.getClass().toString() + " checkActiverules " + sqlResult);
	  }
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
		  
		try {
			
			try {
				wpDB = db.getWritableDatabase();
			}
			catch (Exception e) {
				Toast.makeText(ctx, ctx.getString(R.string.err_unable_to_connect_to_database), Toast.LENGTH_LONG).show();
					e.printStackTrace();
			  }
			wpDB.beginTransaction();
			ContentValues cv=new ContentValues();
			cv.put("PHONE_NUMBER",cDetail.get("phoneNumber"));
			cv.put("CONTACT_NAME",cDetail.get("contactName"));
			cv.put("RULE_ID",ruleID); // think I've collected this somewhere?
			cv.put("EVENT_TYPE",eventString);
			cv.put("CONTENT", messageString); // need to grab SMS detail
			cv.put("DATE_TIME",currentTime); // need to generate this :-)

			wpDB.insert("AUDIT", "PHONE_NUMBER", cv);
			wpDB.setTransactionSuccessful();
		}
		finally {
			wpDB.endTransaction();
			dbCursor.close();
			wpDB.close();
		
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " event logged to audit table");
			}
		
		}
		
	}

	public ArrayList<String> getActiveRule() {
		/*
		 * Utility method added to support TestActivity
		 * 
		 */
		ArrayList<String> ruleInfo = new ArrayList<String>();
		ruleInfo.add(activeRule.get("group").toString());
		ruleInfo.add(activeRule.get("calendar").toString());
		ruleInfo.add(activeRule.get("tag").toString());
		ruleInfo.add(activeRule.get("callBlock").toString());
		ruleInfo.add(activeRule.get("smsBlock").toString());
		return ruleInfo;	
		
	}
	
	private boolean checkAllCallers(Context ctx, Cursor dbCursor) {
		boolean result = false;
		while (	dbCursor.moveToNext()) {
			// HashMap to to save individual record output
			String group = dbCursor.getString(GROUP_NAME);
			if (group.equals(ctx.getString(R.string.allCallers))) {
				result = true;
				break;
			}
		}
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " checkAllCallers " + result);
		}
		return result;
	}
}



