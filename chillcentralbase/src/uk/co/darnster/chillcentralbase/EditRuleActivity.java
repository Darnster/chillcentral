package uk.co.darnster.chillcentralbase;

/*
 * MUST inherit from mainActivity to get access to SharedPreferences. 
 * 
 * 15/7/13 - started to abstract strings - needs a lot more work
 * 15/7/13 - also needs validation updating with changes made to AddRule - for tag length particularly
 * 16/7/13 - need to revisit Group validation in Save Rule
 * 16/7/13 - text abstraction complete
 * 2/8/13 - Dlog added
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

public class EditRuleActivity extends BlankActivity {
	public HashMap <String,String> formDataHash = new HashMap<String,String>();
	public String ruleID;
	HashMap<String, String> groupHash = new HashMap<String,String>();
	HashMap<String, String> calHash = new HashMap<String,String>();
	private DatabaseHelper db;
	private SQLiteDatabase wpDB;
	private Cursor dbCursor;
	public String ruleNumber = null, groupName = null, calendarID = null, calendarName = null, eventTagText = null, smsBlock = "";
	
	// groupSelectHash used in groupSelect Dialog
	protected Button groupsButton;
	protected Button calendarButton;
	protected TextView groupText;
	protected TextView calendarText;
	private ArrayList <String> groupsArray = new ArrayList<String>();
	private ArrayList <String> calendarsArray = new ArrayList<String>();
	protected CharSequence[] groupSelect;
	protected CharSequence[] calendarSelect;
	protected Integer calendarSelection;
	private String calendarString = "";  // string for UI and DB
	protected boolean [] selections; // Used to maintain the state of in the Dialog (i.e. what was selected previously)
	public HashMap<String,String> groupSelectHash = new HashMap<String,String>(); //Keeps a track of what the user clicked in the Dialog
	private String groupString = "";  // string for UI and DB
	//static final int GROUP_DIALOG_ID = 0;
	
	// App shared preferences
	public static final String GLOBAL_PREFERENCES = "GlobalPrefs";
		
	// kludge to get rule_id  when calling editRuleActivity (NEEDS TO BE SHARED ACROSS MULTIPLE ACTIVITIES)
	public static final String SHARED_VARS = "propRuleToEdit"; // String
	
	// Widget for user to select SMS blocking
	public CheckBox smsEnabled, allCallersCheck;
	
	private int layoutView = R.layout.activity_edit_rule;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutView);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		TextView titleText = (TextView) findViewById( R.id.titleText);
		titleText.setText(getString(R.string.title_edit_rule).toUpperCase());
		
		/* Access shared property propRuleToEdit and store to ruleID var
		 * 
		 * Then CLEAR property in shared preferences.
		 * 
		 * feed ruleID into query
		 * 
		 * Need to call database with rule_id passed in  form
		 * 
		 * **** NEED TO PRE-POPULATE FORMDATAHASH ALSO ---- DONE ****
		 * 
		 */
		
		SharedPreferences settings = getSharedPreferences(SHARED_VARS,
				MODE_PRIVATE);
		if (settings.contains("ruleToEdit") == true) {  // should always be true!
			// We have a user name
			ruleID = settings.getString("ruleToEdit", "");
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " RuleID picked up in editRule = " + ruleID);
			}
			
			// remove the ruleToEdit
			SharedPreferences.Editor prefEditor = settings.edit();
			prefEditor.clear();
			prefEditor.commit();
			if (settings.contains("ruleToEdit") != true) {
				if (Dlog.getState()) {
					Dlog.i(this.getClass().toString() + " RuleID removed in onCreate in editRuleActivity :-)");
				}
			}
			
			/*
			 * 
			 *   Need to do all this below - even if All Callers is stored in rule
			 *   This is to allow for when if the user decides to specify groups
			 * 
			 * 
			 */
			
			// now get data from the RULE table
			String sql = "SELECT * FROM RULE WHERE RULE_ID = " + ruleID + ";";
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " Groups SQL = " + sql);
			}
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
				Dlog.d(this.getClass().toString() + " runQuery count from rule table query = " + Integer.toHexString(dbCursor.getCount()));
			}
			while (	dbCursor.moveToNext()) {	
					ruleNumber = dbCursor.getString(dbCursor.getColumnIndex("RULE_ID"));
					groupName = dbCursor.getString(dbCursor.getColumnIndex("GROUP_NAME"));
					calendarID = dbCursor.getString(dbCursor.getColumnIndex("CALENDAR_ID"));
					calendarName = dbCursor.getString(dbCursor.getColumnIndex("CALENDAR_NAME"));
					eventTagText = dbCursor.getString(dbCursor.getColumnIndex("TAG"));
					smsBlock = dbCursor.getString(dbCursor.getColumnIndex("BLOCK_SMS"));
					
					if (Dlog.getState()) {
						String sqlResult = "";
						sqlResult +=  "Rule # " + ruleNumber + "\n";
						sqlResult +=  "Group: " + groupName + "\n";
						sqlResult +=  "Calendar: " + calendarName + "\n";
						sqlResult +=  "Tag: " + eventTagText + "\n";
						sqlResult +=  "SMSBlock: " + smsBlock + "\n\n";
						Dlog.i(this.getClass().toString() + " sqlResult in editRuleActivity = " + sqlResult);
					}
					}
			
			// Assign defaults to form data hash
			formDataHash.put("group",groupName);
			formDataHash.put("calendar_id", calendarID); // calendar_id **** need to add to query ****
			formDataHash.put("calendar", calendarName); // calendar name
			formDataHash.put("smsblock", smsBlock);
			
			dbCursor.close();
			wpDB.close();
			
			

		} else  {
			Toast.makeText(this, R.string.unable_to_determine_rule, Toast.LENGTH_SHORT).show(); // needs testing
			backTarget();
		}
		
		
		/* ******************************************************
		 * 
		 * Group Dialog
		 * 
		 ***********************************************************/
		
		//get groups array
		GroupHandler groupH = new GroupHandler(this);
		groupHash = groupH.getGroups(this);
		// array to hold values to pass in to the dialog
		// ### removed declaration here as it was declared as a class variable
		//ArrayList <String> groupsArray = new ArrayList<String>();
		//groupsArray.add("Select a Group");  // not required for edit
		for (String key : groupHash.keySet()) {
			groupsArray.add(key);
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " returned groups = " + key.toString());
			}
		}
		
		groupsArray.add("Unknown Callers");
		
		// remove any groups which have a rule (OTHER THAN THE ONE WE ARE EDITING) defined against them here :-)	
		
		String activeGroups = checkActiveGroups(ruleID);
		String [] activeItems = activeGroups.split(", ");
						
		List <String> activeGroupList = Arrays.asList(activeItems);
		
				
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
		
		
		/*
		 * 
		 *  4/12/13 - modified the section below to account for "All Callers"
		 * 
		 */
		
		groupText = ( TextView ) findViewById( R.id.groupsTextView);
		
		allCallersCheck = ( CheckBox ) findViewById( R.id.allCallersCheck);
		
		if (groupName.equals(getString(R.string.allCallers))) {
			
			allCallersCheck.setChecked(true);
			View callers = allCallersCheck;
			allCallersHandler(callers);
			
			
			} 
		else
			{	
			groupText.requestFocus();
			groupText.setText(groupName);
			}
		
		groupSelect = groupsArray.toArray(new CharSequence[groupsArray.size()]);
		selections = new boolean[ groupSelect.length ];
		
		/*
		 * Now need to set the selected Groups for the Dialog (selections var)
		 * 
		 */
		
		// parse groupName (can be multi-valued), split on ", "
		String [] items = groupName.split(", ");
		List <String> grpList = Arrays.asList(items);
		// Loop over groupsArray

		for (int i = 0; i < groupsArray.size(); i++ )
			{
				if (grpList.contains( groupsArray.get(i) ) )
				{
					selections[i] = true;
					groupSelectHash.put(groupsArray.get(i), "0");
				}
			}
		
		//////// set groupSelectHash at the same time
		
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + groupsArray.toString());
		}
		
		groupsButton = ( Button ) findViewById( R.id.groupsButton );
		
		groupsButton.setOnClickListener(new Button.OnClickListener() {
			
			 @Override
			   public void onClick(View arg0) {
			    OpenGroupDialog();
			   }});	
	    
		/* ******************************************************
		 * 
		 * Calendar Dialog
		 * 
		 ***********************************************************/
	   
	    CalendarHandler calH = new CalendarHandler(this);
	    calHash = calH.getCalendars(this);
		
		
		// calendarsArray.add("Select a Calendar");  // not required for edit
		for (String key : calHash.keySet()) {
			calendarsArray.add(calHash.get(key));
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " returned calendars = " + calHash.get(key).toString());
			}
		}
		
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " ###### calendarsArray ###### " + calendarsArray.toString());
		}
		


	    calendarSelection = 0;
	    
	    // Determine the position of the data returned from the DB in groupsArray
	    for (int i = 0; i < calendarsArray.size(); i++) {
	    	
	        if(calendarsArray.get(i).equals(calendarName)){
	        	if (Dlog.getState()) {
	        		Dlog.i(this.getClass().toString() + " returned calendar = " + calendarsArray.get(i) + " and expected = " + calendarName );
	        		Dlog.i(this.getClass().toString() + " Calendar item = " + calendarsArray.get(i) + " and expected = " + calendarName + "position = " + i );
	        	}
	        	calendarSelection = i; // spinner index is 0 based
	        	break;
	        }
	    }
	       
	    ///######
	    /*
	     * don't forget to set selection in dialog to value of calendarPosition
	     *  
	     */
	    
	    ///######
	    calendarText = ( TextView ) findViewById( R.id.calendarTextView);
		
	    calendarText.requestFocus();
	    calendarText.setText(calendarName);

		calendarSelect = calendarsArray.toArray(new CharSequence[calendarsArray.size()]);
		
		calendarButton = ( Button ) findViewById( R.id.calendarButton );
		calendarButton.setOnClickListener(new Button.OnClickListener() {
			
			 @Override
			   public void onClick(View arg0) {
			    OpenCalendarDialog();
			   }});	
	    
	    
	    // Event Tag
	    EditText eventTag = (EditText) findViewById( R.id.editRuleTagText ); 
	    eventTag.setText(eventTagText);
	    
	    
	    // Set here whether SMS needs to be blocked too
	    if (Dlog.getState()) {
	    	Dlog.i(this.getClass().toString() + " smsblock in formDataHash = " + formDataHash.get("smsblock") );
	    }
	    smsEnabled = (CheckBox) findViewById( R.id.smsCheckBox );
	    String smsBlock = formDataHash.get("smsblock").toString();
	    if (smsBlock.equals("BLOCK") ) {
	    	if (Dlog.getState()) {
	    		Dlog.i(this.getClass().toString() + " smsBlock in OnCreate = " + smsBlock );
	    	}
	    	smsEnabled.setChecked(true);
	    } else {
	    	
	    	smsEnabled.setChecked(false);
	    	
	    }
	    		
	} // end onCreate
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use an inflater to populate the ActionBar with items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_edit_menu, menu);
        return true;
    }
     
	
	public void allCallersHandler(View view) {
		groupsButton = ( Button ) findViewById( R.id.groupsButton );
		if (allCallersCheck.isChecked()) {
			// hide Choose Groups button		
			groupsButton.setVisibility(view.GONE);  // Gone
			// hide groupsText
			groupText.requestFocus();
			//groupText.setText("");
			groupText.setVisibility(view.GONE);
			// set var for rule entry to "All Callers"
			formDataHash.put("group", getString(R.string.allCallers));
		}
		else
		{
			// make Choose Groups button visible
			groupsButton.setVisibility(view.VISIBLE);  // Visible
			groupText.setVisibility(view.VISIBLE);
		}
		
	}
	
	// DialogFragment stuff - Groups
		public void OpenGroupDialog(){
			 for (int i = 0; i < groupSelect.length; i++) {
				 if (Dlog.getState()) {
					 Dlog.i( this.getClass().toString() + " groupSelect: " + groupSelect[i].toString());
				 }
		        }
		        
		        for (int i = 0; i < selections.length; i++) {
		        	if (Dlog.getState()) {
		        		Dlog.i(this.getClass().toString() + " groups Array: " + selections[i]);
		        	}
		        }
		        
		     GroupDialogFragment myDialogFragment = GroupDialogFragment.newInstance(
		    		 groupSelect,
		    		 selections,
		    		 groupSelectHash,
		    		 "edit"
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
						Dlog.i(this.getClass().toString() + " groupSelectHash size else clause: " + groupSelectHash.size());
					}
					groupString = this.getString(R.string.no_group_selected);
				}
			 groupText.requestFocus();
			 groupText.setText(""); // clear what is there first
			 groupText.setText(groupString);
		 }

		 public void cancelClicked( boolean [] selectionsRestored, HashMap <String, String> groupSelectIfCancelled ) {
			 selections = selectionsRestored;  //Restores original values if user clicked and unclicked groups in the Dialog and then pressed cancel
			 groupSelectHash = groupSelectIfCancelled;
			 // check the groupTextView widget to see if it is populated, if not:
			 // Nothing to do
		 }
		
		
		//######################################## end groups stuff #######################################
	
		 public void OpenCalendarDialog(){      
		     CalendarDialogFragment myDialogFragment = CalendarDialogFragment.newInstance(
		    		 calendarSelect,
		    		 calendarSelection,
		    		 "edit"
		    		 );
		     myDialogFragment.show(getFragmentManager(), "myDialogFragment");
		    }
	
	
	//DialogFragment stuff - Calendar
		 public void okClickedCalendar( int selection) {
			 if (Dlog.getState()) {
				 Dlog.i(this.getClass().toString() + " wp ###### calendarsArray ###### " + calendarsArray.toString());
			 }
			 calendarSelection = selection; // required to ensure correct selection is selected in dialog
			 calendarString = calendarsArray.get(selection);
			 
			//###########################
			String uiPosition = Integer.toString(selection);
			formDataHash.put("calendar_pos_ui", uiPosition);
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " calendar pos in ui = " + uiPosition);
			}
			// get Calandar name displayed in Dialog
			String calDisplayName = calendarsArray.get(selection);  // name of calendar
			// need to get calendar_id for rules table - for event lookup when call comes in - grrrr!
			for (String key : calHash.keySet()) {
				String strCalendar = calHash.get(key).toString();
				//if(strCalendar.equals(calendarsArray.get(selectedItemPosition))) {
				if(strCalendar.equals(calDisplayName)) {
					if (Dlog.getState()) {
						Dlog.i(this.getClass().toString() + " #### calDisplayName ####" + calDisplayName);
					}
					String calendarID = key.toString();
					formDataHash.put("calendar_id", calendarID); // calendar_id
					formDataHash.put("calendar", strCalendar); // calendar name
					if (Dlog.getState()) {
						Dlog.i(this.getClass().toString() + " returned calendar_id = " + calendarID);
						Dlog.i(this.getClass().toString() + " returned calendar = " + strCalendar);
						Dlog.i(this.getClass().toString() + " formDataHash - Calendar = " + formDataHash.get("calendar") );
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
	

	// Check if SMS blocking is also required
	public void SMSHandler(View view) {
		smsEnabled = (CheckBox) findViewById( R.id.smsCheckBox ); 
		if (smsEnabled.isChecked()){
			formDataHash.put("smsblock", "BLOCK");
		} else {
			formDataHash.put("smsblock", "ALLOW");
		}
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " formDataHash.get(smsblock) = " + formDataHash.get("smsblock"));
		}
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
			Log.i("wp", "formDataHash - blockAction = " + formDataHash.get("blockaction"));
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
			Log.i("wp", "formDataHash - blockAction = " + formDataHash.get("blockaction"));
			blockOnlyButton.setChecked(false);
		} 
	}*/


	/* ******************************************************
	 * 
	 * Calls Mandatory checkbox handler
	 * 
	 * 
	 ***********************************************************/
	/*public void oofHandler(View view) {
		CheckBox oofCheckBox = (CheckBox) findViewById(R.id.actionOutofOffice);
		Toast.makeText(this, "This option is not available in the free version", Toast.LENGTH_SHORT).show();
		oofCheckBox.setChecked(false);
	}*/

	
	
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
	
	public void saveRule(View v) {
				
		// form validation
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " formDataHash - group = " + formDataHash.get("group"));
			Dlog.i(this.getClass().toString() + " formDataHash - group = " + formDataHash.get("calendar"));
		}
		
		// validate each widget
		
		// Tag is find byID
		EditText eventTag = (EditText) findViewById( R.id.editRuleTagText );
		if (eventTag.getText().length() == 0 ) {
			Toast.makeText(this, R.string.rule_no_event_tag, Toast.LENGTH_SHORT).show();
			return; // exit and allow user to correct
		}
		
		// check for an entry full of spaces http://stackoverflow.com/questions/3247067/how-to-check-that-java-string-is-not-all-whitespaces
		if (eventTag.getText().toString().trim().length() == 0 ) {
			Toast.makeText(this, R.string.rule_event_tag_white_space, Toast.LENGTH_SHORT).show();
			return; // exit and allow user to correct
		}
		
		if ( eventTag.getText().length() < 3 ) {
			Toast.makeText(this, R.string.rule_event_tag_too_short, Toast.LENGTH_SHORT).show();
			return; // exit and allow user to correct
		}
		// add wildcard check - anything with * get's blocked for free version.
		 
		// mandatory elements all OK
		
		/* loop over FormDataHash
		*for (String key : formDataHash.keySet()) {
		*	Log.i("wp", "formDataHash Save Rule key:" + key + " = "  + formDataHash.get(key));
		*	
		*}
		*/
		
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
		
		String sqlSMS = formDataHash.get("smsblock");
		ruleCV.put("BLOCK_SMS", sqlSMS ); // need to read from a variable in UI
		
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " group before update " + sqlGrp );
			Dlog.i(this.getClass().toString() + " tag before update " + sqlTag );
			Dlog.i(this.getClass().toString() + " calender_id before update " + sqlCalID );
			Dlog.i(this.getClass().toString() + " calender before update " + sqlCal );
			Dlog.i(this.getClass().toString() + " smsblock before insert " + sqlSMS );
		}
		
		db = new DatabaseHelper(this);
		
		try {
			wpDB = db.getWritableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
		try {
			// need to try more generic approach rather than API
			wpDB.update("RULE", ruleCV, "RULE.RULE_ID" + "=" + ruleID, null);
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " rule updated in RULE table");
			}
			Toast.makeText(this, R.string.rule_update_success, Toast.LENGTH_SHORT).show(); // \nPlease press the back button.
			wpDB.close();
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " rule commited in RULE table");
			}
			backTarget();
					
		}
		finally {
			
		// now hide the Add Button
		Button btnSaveRule = (Button) findViewById( R.id.editRuleSaveRule );
		btnSaveRule.setVisibility(View.GONE);
		finish();  // kills this activity
		}
		
	}
	
	public void cancelEdit(View v) {
		backTarget();
		finish();  // kills this activity
		
	}
	
	public void deleteRule(View v) {
		// POP UP DIALOG HERE FOR USER TO CONFIRM DELETION
		OpenDeleteRuleDialog();
		// THEN DELETE THE RECORD		
	}
	
	
	public void OpenDeleteRuleDialog(){
		 	        
	     DeleteRuleDialogFragment deleteDialogFragment = DeleteRuleDialogFragment.newInstance("editRuleActivity", "");
	     deleteDialogFragment.show(getFragmentManager(), "deleteDialogFragment");
	    }
	
	public void deleteClicked() {
		String whereClause = "RULE_ID = '" + ruleID + "'";
		db = new DatabaseHelper(this);
		wpDB = db.getWritableDatabase();
		wpDB.delete("RULE", whereClause, null);
		wpDB.close();
		
		//THEN CALL MAINACTIVITY 
		backTarget();
		finish();  // kills this activity
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
			Dlog.d(this.getClass().toString() + " runQuery " +Integer.toHexString(dbCursor.getCount()));
		}
		String groupName = null;
		while (	dbCursor.moveToNext()) {	
				groupName = dbCursor.getString(dbCursor.getColumnIndex("GROUP_NAME"));
				if (Dlog.getState()) {
					Dlog.i(this.getClass().toString() + " groupName +++++ = " + groupName);
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

	
	public void onBackPressed() {
		backTarget();
		} 

	private void backTarget() {
		// Used to redirect to appropraiate class
		startActivity(new Intent(this, MainActivity.class));
	}
}