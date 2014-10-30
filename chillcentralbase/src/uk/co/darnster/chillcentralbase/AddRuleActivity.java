package uk.co.darnster.chillcentralbase;

/*
 * Plan as follows:
 * 
 * Need to hold class var hash maps to reference groups and calendars queried from phone (id, name)
 * When the user selects a group or calendar, capture the itemselectedindex (think this is 1 based)
 * Then compare this with the the incoming hashmap which is 0 based.
 * 
 * Perform validation
 * 
 * build insert statement and commit.
 * 
 * TO DO
 * 1. convert Calendar dropdown to dialog. DONE
 * 2. Remove previously allocated groups from the list presented to the user - Done
 * 3. Include a configurable list of Groups Done
 * 4. Include a configurable list and Calendars (e.g. PC Sync, Tasks, Frequent Contacts)
 * 4. Include "Unknown Callers" group
 * 
 * 12/7/13 String abstraction complete - not quite!
 * 16/7/13 - String abstraction complete
 * 1/8/13 - Dlog integration complete
 * 2/8/13 Dlog with '''this.getClass().toString() + "''' added
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uk.co.darnster.chillcentralbase.R;

import Database.DatabaseHelper;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
//import android.util.Log;

// dialog imports

public class AddRuleActivity extends BlankActivity {

	public HashMap <String,String> formDataHash = new HashMap<String,String>();
	private HashMap<String, String> groupHash = new HashMap<String,String>();
	private HashMap<String, String> calHash = new HashMap<String,String>();
	private DatabaseHelper db;
	private SQLiteDatabase wpDB;	
	private Cursor dbCursor;
	protected Button groupsButton;
	protected TextView groupText;
	protected Button calendarButton;
	protected TextView calendarText;
	private ArrayList <String> groupsArray = new ArrayList<String>();
	protected CharSequence[] groupSelect, calendarSelect;
	protected boolean [] selections; 
	// groupSelectHash used in groupSelect Dialog
	public HashMap<String,String> groupSelectHash = new HashMap<String,String>();
	public int calendarSelection;
	private String groupString = "";  // string for UI and DB
	private String calendarString = "";  // string for UI and DB
	private ArrayList <String> calendarsArray = new ArrayList<String>();
	static final int GROUP_DIALOG_ID = 0;
	static final int CALENDAR_DIALOG_ID = 1;
	
	// Widget for user to select SMS blocking and All Groups
	public CheckBox smsEnabled, allCallersCheck;
	public static final String RULES_EXIST = "RulesExist"; // String to inform the phone receiver whether rules exist
	private int layoutView = R.layout.activity_add_rule;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutView);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		// set title here
		
		TextView titleText = (TextView) findViewById( R.id.titleText);
		titleText.setText(getString(R.string.title_add_rule).toUpperCase());
		
		//Log.d(String.valueOf(Dlog.getState()));  //used to check functionality - left it in for trouble shooting
		
		/* ******************************************************
		 * 
		 * Group Dialog Calling code
		 * 
		 ***********************************************************/
		
		//get groups array
		GroupHandler groupH = new GroupHandler(this);
		groupHash = groupH.getGroups(this);
		//groupsArray.add("Select a Group");
		for (String key : groupHash.keySet()) {
			groupsArray.add(key);
			if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " returned groups = " + key.toString());
			}
		}
		groupsArray.add("Unknown Callers");  // i18n abstract
		
		// remove any groups which have a rule (FOR ALL RULES) defined against them here :-)	
				
		String activeGroups = checkActiveGroups("");
		String [] activeItems = activeGroups.split(", ");
						
		List <String> activeGroupList = Arrays.asList(activeItems);
		
		// Prevent groups in other rules from appearing in the UI for this rule		
		for (int i = 0; i < groupsArray.size(); i++)
		{
			if (activeGroupList.contains(groupsArray.get(i)))
			{
				if (Dlog.getState()) {
					Dlog.i(this.getClass().toString() + " removed item = " + groupsArray.get(i));
				}
				groupsArray.remove(groupsArray.get(i));
				i--;  //need to decrement as array is re-indexed
			}
		}
		
		// initialise group in formDataHash
		formDataHash.put("group", "0");
		
		groupText = ( TextView ) findViewById( R.id.groupsTextView);
		
		groupSelect = groupsArray.toArray(new CharSequence[groupsArray.size()]);
		selections = new boolean[ groupSelect.length ];
				
		groupsButton = ( Button ) findViewById( R.id.groupsButton );
		groupsButton.setOnClickListener(new Button.OnClickListener() {
			
			 @Override
			   public void onClick(View arg0) {
			    OpenDialog();
			   }});	
		
		
		/* ******************************************************
		 * 
		 * Calendar Dialog
		 * 
		 ***********************************************************/

	    CalendarHandler calH = new CalendarHandler(this);
    	calHash = calH.getCalendars(this);   	
		
		for (String key : calHash.keySet()) {
			calendarsArray.add(calHash.get(key));
			if (Dlog.getState()) {
				Dlog.i("returned calendars = " + calHash.get(key).toString());
			}
		}
		
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + calendarsArray.toString());
		}

		// initialise group in formDataHash
		formDataHash.put("calendar", "0");
		
		calendarText = ( TextView ) findViewById( R.id.calendarTextView);
		
		calendarSelect = calendarsArray.toArray(new CharSequence[calendarsArray.size()]);
		
		calendarButton = ( Button ) findViewById( R.id.calendarButton );
		calendarButton.setOnClickListener(new Button.OnClickListener() {
			
			 @Override
			   public void onClick(View arg0) {
			    OpenCalendarDialog();
			   }});	
	    
	} // end onCreate
	

	//############# All Groups   #############
	
	@SuppressWarnings("static-access")
	public void allCallersHandler(View view) {
		allCallersCheck = (CheckBox) findViewById( R.id.allCallersCheck );
		groupsButton = ( Button ) findViewById( R.id.groupsButton );

		if (allCallersCheck.isChecked()) {
			// hide Choose Groups button
			groupsButton.setVisibility(view.GONE);  // Gone
			// hide groupsText
			groupText.requestFocus();
			groupText.setText("");
			groupText.setVisibility(view.GONE);
			// set var for rule entry to "All"
			formDataHash.put("group", getString(R.string.allCallers));
		}
		else
		{
			// make Choose Groups button visible
			groupsButton.setVisibility(view.VISIBLE);  // Visible
			groupText.setVisibility(view.VISIBLE);
		}
		
	}
	
	
    /////// navigation
	// Action bar Menu stuff
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use an inflater to populate the ActionBar with items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_edit_menu, menu);
        return true;
    }
	 
	// DialogFragment stuff *** GROUPS ***
	public void OpenDialog(){
		if (Dlog.getState()) {
		 for (int i = 0; i < groupSelect.length; i++) {
	        	Dlog.i( this.getClass().toString() + " groupSelect: " + groupSelect[i].toString());
	        }
	        
	        for (int i = 0; i < selections.length; i++) {
	        	Dlog.i( this.getClass().toString() + " groups Array: " + selections[i]);
	        }
		}
	     GroupDialogFragment myDialogFragment = GroupDialogFragment.newInstance(
	    		 groupSelect,
	    		 selections,
	    		 groupSelectHash,
	    		 "add"
	    		 );
	     myDialogFragment.show(getFragmentManager(), "myDialogFragment");
	     
	    }

	 public void okClicked( HashMap<String, String> groups) {
		 groupSelectHash = groups;
		 groupString = "";
		 if (Dlog.getState()) {
			 Dlog.i( this.getClass().toString() + " groupSelectHash size: " + groupSelectHash.size());
		 }
		 if (groupSelectHash.size() > 0) {
			 if (Dlog.getState()) {
				 Dlog.i( this.getClass().toString() + " groupSelectHash size if clause: " + groupSelectHash.size());
			 }
				// loop over hashmap keys and save to a comma separated string
					for (String key : groupSelectHash.keySet()) {
						groupString += key + ", ";  // build string for UI
					}
				// remove to last comma
				groupString = groupString.substring(0, groupString.length() - 2);
				// write to frormdatahash assign to textView label in ui 
				formDataHash.put("group", groupString);
			}
			else
			{
				if (Dlog.getState()) {
					Dlog.i( this.getClass().toString() + " groupSelectHash size else clause: " + groupSelectHash.size());
				}
				groupString = this.getString(R.string.no_group_selected);
			}
		 groupText.requestFocus();
		 groupText.setText(""); // clear what is there first
		 groupText.setText(groupString);
	 }

	 public void cancelClicked( boolean [] selectionsRestored, HashMap <String, String> groupSelectIfCancelled ) {
		 // check the groupTextView widget to see if it is populated, if not:
		 selections = selectionsRestored;  //Restores original values if user clicked and unclicked groups in the Dialog and then pressed cancel
		 groupSelectHash = groupSelectIfCancelled;
		 if (Dlog.getState()) {
			 for (int i = 0; i < selections.length; i++) {
		        	Dlog.i( this.getClass().toString() + " Selections After Cancel: " + selections[i]);
		        }
		 }
		 if (groupText.length() == 0)
			 {
			 groupString = "No group selected - please try again.";
			 groupText.requestFocus();
			 groupText.setText(groupString);
			 }
	 }
	

	 
//######################################## end Groups Dialog Handler stuff #######################################

	 
	// DialogFragment stuff *** CALENDAR ***
	public void OpenCalendarDialog(){      
	     CalendarDialogFragment myDialogFragment = CalendarDialogFragment.newInstance(
	    		 calendarSelect,
	    		 calendarSelection,
	    		 "add"
	    		 );
	     myDialogFragment.show(getFragmentManager(), "myDialogFragment");
	    }

	 public void okClickedCalendar( int selection) {
		 calendarSelection = selection; // required to ensure correct selction is seleceted in dialog
		 calendarString = calendarsArray.get(selection);
		 
		 //###########################
		 String uiPosition = Integer.toString(selection);
		formDataHash.put("calendar_pos_ui", uiPosition);
		if (Dlog.getState()) {
			Dlog.i( this.getClass().toString() + " calendar pos in ui = " + uiPosition);
		}
		// get Calandar name displayed in Dialog
		String calDisplayName = calendarsArray.get(selection);  // name of calendar
		// need to get calendar_id for rules table - for event lookup when call comes in - grrrr!
		for (String key : calHash.keySet()) {
			String strCalendar = calHash.get(key).toString();
			//if(strCalendar.equals(calendarsArray.get(selectedItemPosition))) {
			if(strCalendar.equals(calDisplayName)) {
				String calendarID = key.toString();
				formDataHash.put("calendar_id", calendarID); // calendar_id
				formDataHash.put("calendar", strCalendar); // calendar name
				if (Dlog.getState()) {
					Dlog.i( this.getClass().toString() + " #### calDisplayName ####" + calDisplayName);
					Dlog.i( this.getClass().toString() + " returned calendar_id = " + calendarID);
					Dlog.i( this.getClass().toString() + " returned calendar = " + strCalendar);
					Dlog.i( this.getClass().toString() + " formDataHash - Calendar = " + formDataHash.get("calendar") );
				}
				break;
			}			
		 
		 
		}
		 
		 //###########################
		 
		 calendarText.requestFocus();
		 calendarText.setText(""); // clear what is there first
		 calendarText.setText(calendarString);
	 }

	 public void cancelClickedCalendar() {
		 // nothing to do here - calendar will be set to first Calendar in the list
		 if (calendarText.getText() == ""){
			 calendarText.requestFocus();
			 calendarText.setText(R.string.no_calendar_selected);
		 }
		 
	}
	 
	 
	 
	 	//######################################## end calendar Dialog Handler stuff #######################################
	
	
	/* ******************************************************
	 * 
	 * Calls Mandatory checkbox handler
	 * 
	 * 
	 ***********************************************************/
	public void callsHandler(View view) {
		CheckBox callsCheckBox = (CheckBox) findViewById(R.id.callsCheckBox);
		Toast.makeText(this, R.string.rule_call_block_not_checked, Toast.LENGTH_SHORT).show();
		callsCheckBox.setChecked(true);
		
	}
	
	
	/* ******************************************************
	 * 
	 * Block Action
	 * 
	 * 
	 ***********************************************************/	
	
	// radio button handler for block options
	// Note did try to make this work in a single method but it failed (logic bomb)
	/*public void blockOnlyHandler(View view) {
		RadioButton blockOnlyButton = (RadioButton) findViewById(R.id.actionBlockOnly);
		RadioButton blockVoicemailButton = (RadioButton) findViewById(R.id.actionBlockVoicemail);
		if (blockOnlyButton.isChecked()) {
			blockOnlyButton.setChecked(true);
			// assign formDataHash value here
			formDataHash.put("blockaction", "BlockOnly");
			Log.i( "formDataHash - blockAction = " + formDataHash.get("blockaction"));
			blockVoicemailButton.setChecked(false);
		} 
	}
	
	public void blockVoicemailHandler(View view) {
		RadioButton blockVoicemailButton = (RadioButton) findViewById(R.id.actionBlockVoicemail);
		RadioButton blockOnlyButton = (RadioButton) findViewById(R.id.actionBlockOnly);
		if (blockVoicemailButton.isChecked()) {
			blockVoicemailButton.setChecked(true);
			// assign formDataHash value here
			formDataHash.put("blockaction", "VoiceMail");
			Log.i( "formDataHash - blockAction = " + formDataHash.get("blockaction"));
			blockOnlyButton.setChecked(false);
		} 
		
	}*/


	/* ******************************************************
	 * 
	 * Out of office SMS checkbox handler
	 * 
	 * 
	 ***********************************************************/
	/*public void oofHandler(View view) {
		CheckBox oofCheckBox = (CheckBox) findViewById(R.id.actionOutofOffice);
		Toast.makeText(this, "This option is not available in the free version", Toast.LENGTH_SHORT).show();
		oofCheckBox.setChecked(false);
	}
	*/

	
	
	/* ******************************************************
	 * 
	 * Add Rule - validation
	 * 
	 * Group and Calendar not set to 1 / first item in the list
	 * Event Tag is not null
	 * Calls MUST be checked
	 * 
	 * Make Toast and report validation failures
	 * 
	 ***********************************************************/
	
	public void addRule(View v) {
		
		// form validation
		
		if (Dlog.getState()) {
			Dlog.i( this.getClass().toString() + " formDataHash - group = " + formDataHash.get("group"));
			Dlog.i( this.getClass().toString() + " formDataHash - group = " + formDataHash.get("calendar"));
		}
		// validate each widget
		//Groups
		if (formDataHash.get("group").equals("0") || formDataHash.get("group").equals("") ) {
			Toast.makeText(this, R.string.rule_no_group_selected,Toast.LENGTH_SHORT).show();	
			return; // exit and allow user to correct
		}
		////***** need to rethink this - but need to set calandarTest widget to "No Calendar selected". Please try again.
		if (calendarText.getText() == "No calendar selected - please try again."  || calendarText.getText() == "" ){
			Toast.makeText(this, R.string.rule_no_calendar_selected, Toast.LENGTH_SHORT).show();
			return; // exit and allow user to correct
		}
		
		// Tag is find byID
		EditText eventTag = (EditText) findViewById( R.id.addRuleTagText );
		if (eventTag.getText().length() == 0 ) {
			Toast.makeText(this, R.string.rule_no_event_tag, Toast.LENGTH_SHORT).show();
			return; // exit and allow user to correct
		}
		
		// check for an entry full of spaces http://stackoverflow.com/questions/3247067/how-to-check-that-java-string-is-not-all-whitespaces
		if (eventTag.getText().toString().trim().length() == 0 ) {
			Toast.makeText(this, R.string.rule_event_tag_white_space, Toast.LENGTH_SHORT).show();
			return; // exit and allow user to correct
		}
		
		if ( eventTag.getText().toString().trim().length() < 3 ) {
			Toast.makeText(this, R.string.rule_event_tag_too_short, Toast.LENGTH_SHORT).show();
			return; // exit and allow user to correct
		}
		// add wildcard check - anything with * get's blocked for free version.
		 
		// mandatory elements all OK
		
		// Check if SMS blocking is also required
		smsEnabled = (CheckBox) findViewById( R.id.smsCheckBox ); 
		if (smsEnabled.isChecked()){
			formDataHash.put("smsEnabled", "BLOCK");
		} else {
			formDataHash.put("smsEnabled", "ALLOW");
		}
		
		// loop over FormDataHash
		if (Dlog.getState()) {
			for (String key : formDataHash.keySet()) {
				Dlog.i( this.getClass().toString() + " key:" + key + " = "  + formDataHash.get(key).toString());			
			}
		}
		
		ContentValues ruleCV = new ContentValues();
		// GROUP_NAME TEXT, TAG TEXT, CALENDAR_ID TEXT, CALENDAR_NAME TEXT, ACTION_NAME TEXT, ACTION_SCOPE NUMBER
		//group, calendar, calender_id, 
		String sqlGrp = formDataHash.get("group");
		ruleCV.put("GROUP_NAME", sqlGrp);
		
		String sqlTag = eventTag.getText().toString();
		ruleCV.put("TAG", sqlTag);
		
		String sqlCalID = formDataHash.get("calendar_id");
		ruleCV.put("CALENDAR_ID", sqlCalID);
		
		String sqlCal = formDataHash.get("calendar");
		ruleCV.put("CALENDAR_NAME", sqlCal);
		ruleCV.put("BLOCK_CALL","BLOCK"); // mandatory, so always checked
		
		String sqlSMS = formDataHash.get("smsEnabled").toString();
		ruleCV.put("BLOCK_SMS", sqlSMS ); // need to read from a variable in UI
		
		//log here
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " group before insert " + sqlGrp );
			Dlog.i(this.getClass().toString() + " tag before insert " + sqlTag );
			Dlog.i(this.getClass().toString() + " calender_id before insert " + sqlCalID );
			Dlog.i(this.getClass().toString() + " calender before insert " + sqlCal );
			Dlog.i(this.getClass().toString() + " smsBlock before insert " + sqlSMS );
		}
		db = new DatabaseHelper(this);
		try {
			wpDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		try {
			wpDB.beginTransaction();
			wpDB.insert("RULE", "GROUP_NAME", ruleCV);
			wpDB.setTransactionSuccessful();	
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " rule added to RULE table");
			}
			Toast.makeText(this, R.string.rule_added, Toast.LENGTH_SHORT).show(); //\nPlease press the back button.
			SharedPreferences.Editor prefEditor = appSettings.edit();
			prefEditor.putBoolean(RULES_EXIST, true);
			prefEditor.commit();
			
			
			backTarget(); // redirect to calling page
			
		}
		finally {
			wpDB.endTransaction();
			wpDB.close();
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " rule added to RULE table");
			}
			Toast.makeText(this, R.string.rule_added, Toast.LENGTH_SHORT).show(); //\nPlease press the back button.
			backTarget();

		// now hide the Add Button
		Button btnAaddRule = (Button) findViewById( R.id.addRuleAddRule );
		btnAaddRule.setVisibility(View.GONE);
		finish();  // kills this activity
		}
		
	}
	
	public String checkActiveGroups(String ruleID){
    	// GROUP_NAME TEXT, TAG TEXT, CALENDAR_ID TEXT, CALENDAR_NAME TEXT, ACTION_NAME TEXT, ACTION_SCOPE NUMBER

		String sql = "SELECT * FROM RULE WHERE RULE_ID <> '" + ruleID + "';";
		String sqlResult = "";
		db = new DatabaseHelper(this);
		try {
			wpDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		dbCursor = wpDB.rawQuery(sql, null);
		if (Dlog.getState()) {
			Dlog.d( this.getClass().toString() + " -runQuery" + Integer.toHexString(dbCursor.getCount()));
		}
		String groupName = null;
		while (	dbCursor.moveToNext()) {	
				groupName = dbCursor.getString(dbCursor.getColumnIndex("GROUP_NAME"));
				if (Dlog.getState()) {
					Dlog.i( this.getClass().toString() + " groupName +++++ = " + groupName);
				}
				sqlResult +=  groupName + ", ";
				}
		dbCursor.close();
		wpDB.close();
		if (sqlResult != "") {
			sqlResult = sqlResult.substring(0, sqlResult.length() - 2);
		}
		if (Dlog.getState()) {
			Dlog.i( this.getClass().toString() + " sqlResult +++++ = " + sqlResult);
		}
		return sqlResult;
		
		}
	
	public void backTarget() {
		// Used to redirect to appropraiate class
		startActivity(new Intent(this,MainActivity.class));
	}

}