package uk.co.darnster.chillcentral;

/*
 * This Activity doesn't inherit from BlankActivity because it inherits from ListActivity
 * Because of this the SharedPrefs need to be maintained in 2 separate classes - not good!
 * 
 * TO DO
 * 
 * Check if calendars and groups defined in any rule are still on the device
 * Check for same phone number in >1 contact
 * Check API version and show dialog box, then exit - Done
 * Write install date to device - 30 days
 * Allow Test process back door to extend usage (must be done on handset)
 * 
 * 16/7/13 - Test Number default externalised
 * 2/8/13 - Dlog added
 * 
 * 17/8/13 - need to abstract expiry strings
 */

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.darnster.chillcentral.R;
import Database.DatabaseHelper;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ListActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
	/** Called when the activity is first created. */
	private DatabaseHelper db;
	private SQLiteDatabase wpDB;
	private Cursor dbCursor;
	private final HashMap<Integer, String> RuleHash = new HashMap<Integer,String>();
	private String RuleToDelete;
	// Rules object initiated for ......
	private ArrayList<Rules> rule_parts = new ArrayList<Rules>();
	private RulesAdapter r_adapter;
	
	// shared prefs for App Settings
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
	public static final String RULES_EXIST = "rules_exist"; // set and updated as and when rules are added/removed
	public static final String TIPS_UPDATE = "tips_update"; // will be used to store boolean matching value to do dynamic load of Tips & Tricks text
	
	public static boolean updateDBCalled = false;  //Updates DB entry for Tips and Tricks
	SharedPreferences appSettings;
	
	// kludge to get rule_id  when calling editRuleActivity (NEEDS TO BE SHARED ACROSS MULTIPLE ACTIVITIES)
	public static final String SHARED_VARS = "propRuleToEdit"; // String
    	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Dlog.setState(false); // enable logging - need to add a var to shared prefs to manage this so that BlankActivity and MainActivity pick up the same value
		//Log.d(Dlog.TAG, String.valueOf(Dlog.getState()));  //used to check functionality - left it in for trouble shooting
		// initialise appSettings here as it is used in 2 areas
		appSettings = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
		
		db = new DatabaseHelper(this); // used to query rules in DB and to update general content of DB (Tips & Tricks)
		
		if (!appSettings.contains(TIPS_UPDATE)) {
		updateDB(db);
		}

		
		// check if the users phone can run W&P!  	
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " apiVersion = " + android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH);
			Dlog.i(this.getClass().toString() + " currentapiVersion = " + currentapiVersion);
		}
		if (currentapiVersion < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			// Need to load dialogFragment where user can ONLY select "End"
			// backPressed should kill the process also
			finish();
	    	startActivity(new Intent(this, AndroidVersionFailActivity.class));
		    
			} 
		else
		{	
			// some code here to check if free version is installed
			if ( isPackageExists( "uk.co.darnster.chillcentralfree" )) {  // user needs to uninstall package
				finish();
		    	startActivity(new Intent(this, FreePackageViolationActivity.class)); // POC
			}
			else
			{
			// Check ChangeLog
			/*
			 * Read SharedPref
			 * If not present
			 * 		(Start EulaActivity)
			 * 		EulaActivity to record what the user chose to do
			 */
	        
			 ChangeLog cl = new ChangeLog(this, getResources().getString(R.string.changelog_full_title));
			 if (cl.firstRun())
			 {
				 // quick boolean flag here to force first load to reload MainActivity
		        cl.getFullLogDialog(true).show();
			 	}
			 else
			 	{
			 
		 
				// Action Bar
		        ActionBar actionBar = getActionBar();
		        actionBar.show();
		        //actionBar.setDisplayHomeAsUpEnabled(true);
		        ColorDrawable colorDrawable = new ColorDrawable();
		        actionBar.setTitle(R.string.app_name_action_bar);
		        colorDrawable.setColor(getResources().getColor(R.color.darkgrey));
		        actionBar.setBackgroundDrawable(colorDrawable);
		        
		        /////////////////////////////////////////////
		        
		        // initialise App Prefs
		        /*
		         * Read a shared pref "init"
		         * If it doesn't exist
		         * 		Set the initial values when the screen is loaded
		         * 		Then create "init" and set it to true
		         */
		        if (appSettings.contains(INIT)) {
		        	// do nothing
		        	}
		        else
			        {
		        	// set the defaults	        	
		        	SharedPreferences.Editor prefEditor = appSettings.edit();
		    		prefEditor.putBoolean(BLOCK, true); // master switch
		    		prefEditor.putBoolean(NOTIFY, true); // controls whether a notification appears in status bar ans notifications lit
		    		prefEditor.putBoolean(REMOVE_CALLS, true); // determines whether calls are removed form the call log
		    		prefEditor.putBoolean(INIT, true); // flag to allow subsequent loads to recognise that defaults are set
		    		prefEditor.putString(TEST, this.getString(R.string.test_number));
		    		prefEditor.putBoolean(RULES_EXIST, false); // added here but not referenced as at 12/9/13
		    		        		
	        		prefEditor.commit();
	        		
	        		// check value stored
	        		
	        		Long expiryTime = appSettings.getLong(EXPIRY,0) ;  // set default, but it should never get picked up
	            	if (Dlog.getState() == true){
	            		Dlog.i(this.getClass().toString() + " expiry from shared prefs (after insert) = " + expiryTime);
	            	}
		    		
		    		//#####################################################
	            	
		    		prefEditor.commit();
		        	// add INIT to prevent this code from being called again
			        }

                
		ListView menuList = (ListView) findViewById(android.R.id.list);
		
		db = new DatabaseHelper(this);
		////////////////////////////////
		
		String sql = "SELECT * FROM RULE ORDER BY RULE_ID ASC;";
		
		//Data structure to hold multidimensional array before calling RulesAdapter
		
		try {
			wpDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
		dbCursor = wpDB.rawQuery(sql, null);
		String ruleNumber = null, groupName = null, calendarName = null, eventTagText = null;
		Integer count = 1;
		
		if (dbCursor.getCount() == 0) {
			finish();
	    	startActivity(new Intent(MainActivity.this, SplashActivity.class));
		}
		
		else {
			
		while (	dbCursor.moveToNext()) {
				ruleNumber = dbCursor.getString(dbCursor.getColumnIndex("RULE_ID"));
				groupName = dbCursor.getString(dbCursor.getColumnIndex("GROUP_NAME"));
				calendarName = dbCursor.getString(dbCursor.getColumnIndex("CALENDAR_NAME"));
				eventTagText = dbCursor.getString(dbCursor.getColumnIndex("TAG"));
				
				
				
				rule_parts.add(new Rules(groupName,calendarName, eventTagText));
				
				RuleHash.put(count, ruleNumber); 
				count++;
				}

				
		dbCursor.close();
		wpDB.close();		
		
		// http://www.ezzylearning.com/tutorial.aspx?tid=1763429
		// Need to insert theRules Adapter here
		r_adapter = new RulesAdapter(this, R.layout.rule_menu , rule_parts){
			public View getView(int position, View convertView, ViewGroup parent) {
	            View row =  super.getView(position, convertView, parent);
	            
	            View edit = row.findViewById(R.id.EditRule);
	            edit.setTag(position);
	            edit.setOnClickListener(MainActivity.this);
	            
	            View delete = row.findViewById(R.id.DeleteRule);
	            delete.setTag(position);
	            delete.setOnClickListener(MainActivity.this);
	            
	            return row;
			}
			};
		
		menuList.setAdapter(r_adapter);
		menuList.setOnItemClickListener(this);
       
		
		} // end API compatibility check
		} // end changeLog check
		}  //free version check
		} // end rules count check
		


	 
	 // ###########################################################################################
	} // end onCreate
		
	@Override
    public void onClick(View v) {
		int ruleClicked;
		String ruleID;
        switch(v.getId()) {
        case R.id.EditRule:
        	
        	/////
        	// get ruleID from hashmap
        	ruleClicked = (Integer) v.getTag();
    		ruleID = RuleHash.get( ruleClicked + 1); // ListView has index starting with 1 not zero
    		SharedPreferences settings = getSharedPreferences(SHARED_VARS,
    				MODE_PRIVATE);
    		SharedPreferences.Editor prefEditor = settings.edit();
    		prefEditor.putString("ruleToEdit", ruleID);
    		prefEditor.commit();
    		
    		if (Dlog.getState()) {
    			Dlog.d(this.getClass().toString() + " ruleID set in mainActivity : " + ruleID);
    		}
    		
			finish();	
			startActivity(new Intent(MainActivity.this,
					EditRuleActivity.class));       	
			break;
			
        case R.id.DeleteRule:
        	ruleClicked = (Integer) v.getTag();
    		ruleID = RuleHash.get( ruleClicked + 1); // ListView has index starting with 1 not zero
    		RuleToDelete = ruleID;
    		OpenDeleteRuleDialog();
            break;
            
        default:
            break;        
        }	
        
        

	}
	
	public void OpenDeleteRuleDialog(){
	     DeleteRuleDialogFragment deleteDialogFragment = DeleteRuleDialogFragment.newInstance("mainActivity");
	     deleteDialogFragment.show(getFragmentManager(), "deleteDialogFragment");
	    }
	
	public void deleteClicked() {
		String whereClause = "RULE_ID = '" + RuleToDelete + "'";
		db = new DatabaseHelper(this);
		
		try {
			wpDB = db.getWritableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
		wpDB.delete("RULE", whereClause, null);
		
		//THEN REFRESH 
		startActivity(new Intent(MainActivity.this,
				MainActivity.class));
				finish();  // kills the previous instance of mainActivity
	}
 

	
	/////////////////////////////////////////////////////
    	@Override
	public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long arg3) {		
	}
    	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use an inflater to populate the ActionBar with items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.global_menu, menu);
        return true;
    }
 	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
        // same as using a normal menu
        switch(item.getItemId()) {
        case android.R.id.home:
        	finish();
            break;
        
	    case R.id.about:
	    	//AboutDialogFragment myDialogFragment = new AboutDialogFragment(this);
	    	//myDialogFragment.show(getFragmentManager(), "myDialogFragment");
	    	ChangeLog cl = new ChangeLog(this, this.getString(R.string.about));
	    	cl.getFullLogDialog( false).show();
	    	break;

	    case R.id.add_rule:
        	finish();
        	startActivity(new Intent(MainActivity.this, AddRuleActivity.class));
            break;
            
        case R.id.configure:
        	finish();
        	startActivity(new Intent(this, ConfigActivity.class));
            break;

        case R.id.logs:
        	finish();
        	startActivity(new Intent(MainActivity.this, LogActivity.class));
            break;
            
	    case R.id.test:
	    	finish();
	    	startActivity(new Intent(MainActivity.this, TestActivity.class));
	        break;
	        
	    case R.id.tips:
	    	finish();
	    	startActivity(new Intent(this, TipsTricksActivity.class));
	        break;
	        
	    /*case R.id.changelog:
	    	ChangeLog cl = new ChangeLog(this);
	    	cl.getFullLogDialog().show();
	        break;*/
	        
	    case R.id.exit:
	    	finish();
	    	// code below commented out due kill process reloading MainActivity and losing state on "ExpiryShowed"
        	//android.os.Process.killProcess( android.os.Process.myPid() ) ;
        	//KillDialogFragment myKillDialogFragment = new KillDialogFragment(this);
	    	//myKillDialogFragment.show(getFragmentManager(), "myKillDialogFragment");
	        break;

	        
        }

            return true;
        }
        
        @Override
    	public void onBackPressed() {
        	finish();
        	// code below commented out due kill process reloading MainActivity and losing state on "ExpiryShowed"
        	//android.os.Process.killProcess( android.os.Process.myPid() ) ;  
        	//KillDialogFragment killFragment = new KillDialogFragment(this);
			//killFragment.show(getFragmentManager(), "myDialogFragment");   		
    		
    	}
        
        public boolean isPackageExists(String targetPackage){
        	   PackageManager pm=getPackageManager();
        	   try {
        	    PackageInfo info = pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        	       } catch (NameNotFoundException e) {
        	    return false;
        	    }  
        	    return true;
        	   }
        

	    private void updateDB(DatabaseHelper dBH) {
	    	
	    		// update entries here
	    		try {
	    			wpDB = dBH.getWritableDatabase();
	    		}
	    		catch (Exception e) {
	    			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
	    				e.printStackTrace();
	    		  }
	    		try {
	    			wpDB.beginTransaction();
	    			ContentValues cv=new ContentValues();
	    			// tip #3
	    			String tip3title  = this.getString(R.string.tip3title);
	    			String tip3detail  = this.getString(R.string.tip3detail);
	    			cv.put("TIP_ID", 3);
	    			cv.put("TIP_TITLE",tip3title);
	    			cv.put("TIP_DETAIL",tip3detail);			
	    			wpDB.replace("TIPS", "TIP_TITLE", cv);
	    			cv.clear();
	    			wpDB.setTransactionSuccessful();
	    		}
	    		finally 
	    			{
	    			wpDB.endTransaction();
	    			//Toast.makeText(this, "Fired Tips Update", Toast.LENGTH_LONG).show();
	    			if ( Dlog.getState() ) {
	    				Dlog.i("database modified with tips and tricks update.");	
	    			}
	    		}
	    		
	    		wpDB.close();
	    		SharedPreferences.Editor prefEditor = appSettings.edit();
	    		prefEditor.putBoolean(TIPS_UPDATE, true); // master switch
	    		prefEditor.commit();	    	
	    	
	    	}
}
