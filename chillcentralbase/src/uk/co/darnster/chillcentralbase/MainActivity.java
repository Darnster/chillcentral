package uk.co.darnster.chillcentralbase;

/*
 * Hosts main navigation options + child Fragments
 * 
 * 
 */

import java.util.Locale;
import java.util.Random;

import Database.DatabaseHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import uk.co.darnster.chillcentralbase.Dlog;


public class MainActivity extends FragmentActivity {
	
	SectionsPagerAdapter mSectionsPagerAdapter;

	public static final String BLOCK = "Block"; // String for disabling whole app

	//static 
	ViewPager mViewPager;
	private DatabaseHelper db;
	private SQLiteDatabase ccDB;
	private Cursor dbCursor;
	private static Integer currentFrag; // controls what is displayed by default
	private static Context ctx;
	
	public SharedPreferences appSettings;
	// shared prefs for App Settings
	public static final String APP_SETTINGS = "AppSettings"; // String to hold
	// using this to set App Prefs in config screen
	public static final String INIT = "Init"; // String

	public static boolean uninstallShown = false;  // used to prevent the please dialog from appearing > 1 during each session

	public FirstRun fRun;
	
	private int layoutView = R.layout.activity_page_view;
	
	public static final String RULES_EXIST = "RulesExist"; // set and updated as and when rules are added/removed


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showUninstallDialog();
		setContentView(layoutView);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		// lets see if we have any rules first so we can load out whether to load the Splash or Manage Fragment:
		// need to abstract the DB call below - to make it easier to follow
		db = new DatabaseHelper(this); // need to call this because Activity not created on resume...
		
		String sql = "SELECT * FROM RULE ORDER BY RULE_ID ASC;";
			
		try {
			ccDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			try {
			Toast.makeText(ctx, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
		  }
		
		dbCursor = ccDB.rawQuery(sql, null);		
		
		if (dbCursor.getCount() == 0) {
			appSettings = getSharedPreferences(APP_SETTINGS, 0);
	    	SharedPreferences.Editor prefEditor = appSettings.edit();
			prefEditor.putBoolean(RULES_EXIST, false);
			prefEditor.commit();
			}		

		//mViewPager = null;  // force refresh here?
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		setSectionPageAdapter();

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(1);
		try {
			mViewPager.setCurrentItem(getFragment()); //.getPosition set by call back
		}
		catch (Exception e) {
			mViewPager.setCurrentItem(1);
		}
		
		appSettings = getSharedPreferences(APP_SETTINGS, 0);
	    if (appSettings.contains(INIT)) {
			SharedPreferences.Editor prefEditor = appSettings.edit();
			prefEditor.putBoolean(BLOCK, true); // master switch *** now set to false by default ***
			prefEditor.commit();
	    	// do nothing
	    	}
	    else
	    {
	    	fRun = new FirstRun(this);
	    	fRun.runInit();
	    	displayChangeLog();
	    }


	}
	

	public void setSectionPageAdapter() {
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
	}
	
	public void displayChangeLog() {
		ChangeLog cl = new ChangeLog(this,getResources().getString(R.string.changelog_full_title));
		 if (cl.firstRun())
		 	{
			 cl.getFullLogDialog(true).show();
		 	}
		}
	
	
	// accessor methods to set current Fragment when Activity is called
	public void setFragment(Integer frag) {
		currentFrag = frag;
	}
	
	public Integer getFragment() {
		return currentFrag;
	}
	
	public Fragment getFragItem(Integer i) {
		return mSectionsPagerAdapter.getItem(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.global_menu, menu);
		return true;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		 public Fragment getItem(int pos) {
			switch(pos) {
            default: return ManageFragment.newInstance(MainActivity.this);
            case 0: return ConfigFragment.newInstance(MainActivity.this);
            case 1: 
            	if (appSettings.getBoolean( RULES_EXIST, true )) {
            		return ManageFragment.newInstance(MainActivity.this);
            	}
            	else 
            	{
            		return SplashFragment.newInstance(MainActivity.this);
            	}
            	
            case 2: return LogFragment.newInstance(MainActivity.this);
            case 3: return TestFragment.newInstance(MainActivity.this); 
            }
		}
            
		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_config).toUpperCase(l);				
			case 1:
				if (appSettings.getBoolean( RULES_EXIST, true )) {
					return getString(R.string.rules_main).toUpperCase(l);
            	}
            	else 
            	{
            		return getString(R.string.splash_title).toUpperCase(l);
            	}
				
			case 2:
				return getString(R.string.audit_log).toUpperCase(l);
			case 3:
				return getString(R.string.title_test).toUpperCase(l);
			}
			return getString(R.string.rules_main).toUpperCase(l);
		}	
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item){
		/*
		 * Needed to add if ... else for each menu item 
		 * to allow for when splash fragment is initiating call to Add Rule, etc.. 
		 */
		boolean rulesExist = false;  // required to branch try/catch to handle exception when app is first launched.
		
		try {
			if (appSettings.getBoolean( RULES_EXIST, true )) {
				rulesExist = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			// rulesExist remains false
		}
		
        int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
		} else if (itemId == R.id.add_rule) {
			//finish();
			if (rulesExist) {
				startActivity(new Intent(ManageFragment.ctx, AddRuleActivity.class));
			}
			else
			{
				startActivity(new Intent(SplashFragment.ctx, AddRuleActivity.class));
			}
		} else if (itemId == R.id.about) {
			ChangeLog cl;
			if (rulesExist) {
				cl = new ChangeLog(ManageFragment.ctx, this.getString(R.string.about));
			}
			else
			{
				cl = new ChangeLog(SplashFragment.ctx, this.getString(R.string.about));
			}
	    	
			cl.getFullLogDialog( false).show();
		} else if (itemId == R.id.tips) {
			TipsTricksDialog TTDlg;
			if (rulesExist) {
				 TTDlg = new TipsTricksDialog(ManageFragment.ctx, this.getString(R.string.title_tips_tricks));
			}
			else 
			{
				TTDlg = new TipsTricksDialog(SplashFragment.ctx, this.getString(R.string.title_tips_tricks));
			}
			TTDlg.getFullTipsDialog( false).show();
		}

            return true;
        }

	/*
	 * Click handlers for dialog boxes all moved to here
	 * 
	 * To Do:
	 * 
	 * ManageFragment - Edit Rule
	 * ManageFragment - Delete Rule
	 * 
	 * 
	 */
	
	public void OpenDeleteRuleDialog(String RuleToDelete){
	     DeleteRuleDialogFragment deleteDialogFragment = DeleteRuleDialogFragment.newInstance("MainActivity", RuleToDelete);
	     deleteDialogFragment.show(getFragmentManager(), "deleteDialogFragment");
	    }
	
	public void deleteClicked(String RuleToDelete) {
		String whereClause = "RULE_ID = '" + RuleToDelete + "'";
		db = new DatabaseHelper(this);
		
		try {
			ccDB = db.getWritableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
		ccDB.delete("RULE", whereClause, null);
		
		//THEN REFRESH 
		backTarget();
	}


	
	// Delete Log code move to here
	public void OpenDeleteLogDialog(String log){
	    DeleteLogDialogFragment deleteLogDialogFragment = DeleteLogDialogFragment.newInstance(log);
	    deleteLogDialogFragment.show(getFragmentManager(), "deleteLogDialogFragment");
	    }

	public void deleteLogClicked(String log){
		db = new DatabaseHelper(this);
		String whereClause = "EVENT_TYPE = '" + log + "'";
		
		try {
			ccDB = db.getWritableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
		ccDB.delete("AUDIT", whereClause, null);
		//dbCursor.close();
		ccDB.close();
		// point back to Log Fragment in position 2 when refreshing
		this.setFragment(2);
		backTarget();
		//now set it back to the default
		// need pending this.setFragment(null);
		ResetDefaultFrag resetFrag = new ResetDefaultFrag(this, 1);
		resetFrag.start();
	}
	
	 
	@Override
	public void onBackPressed() {
		 /*
		  * Need to think about identifying where we are in the app!
		  * -- if ManageFragment end App
		  * -- else, return to ManageFragment
		  * 
		  * OR don't implement anything here and instead work on Exit on menu
		  *  
		  * 
		  */
		 if (Dlog.getState() == true){
			 Dlog.i(this.getClass().toString() + " mViewPager.getCurrentItem() = " + mViewPager.getCurrentItem());
			 
		 }
		 if (mViewPager.getCurrentItem() == 1 ) {
			 if (Dlog.getState() == true){
				 Dlog.i(this.getClass().toString() + " end app option reached " + mViewPager.getCurrentItem());
			 }
				Intent i = new Intent(Intent.ACTION_MAIN);
		        i.addCategory(Intent.CATEGORY_HOME);  // move Chill Central to back of stack
		        startActivity(i);         		 
		 	} 
		 else 
		 	{
			 backTarget();
		 	}
		 
	     //	
		} 
		
	public void splashAddRule(View view) {
		startActivity(new Intent(this, AddRuleActivity.class));
	}

	public void rateApp(View view) {
		/* This code assumes you are inside an activity */
		final Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + "uk.co.darnster.chillcentral");
		final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

		if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
		{
		    startActivity(rateAppIntent);
		}
		else
		{
			Toast.makeText(this, "Could not open Android Play, please install the market app.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void backTarget() {
		// Used to redirect to appropraiate class
		finish();
		startActivity(new Intent(this,MainActivity.class));
	}

    public void showUninstallDialog() {
    	if (uninstallShown == false) {
			// get random digit
			Random rand = new Random();
		    int pickedNumber = rand.nextInt(10);
		    if (pickedNumber > 4) {  // was set to 8
			    	String dlgTitle = getString(R.string.upgrade_title);
			    	String dlgMsg = getString(R.string.upgrade_message);
			    	UninstallDialog ugDialog = new UninstallDialog(this, dlgTitle, dlgMsg);
			    	ugDialog.getFullLogDialog().show();
			    	uninstallShown = true;
    		    }
    		}
    	}

	
	
	// inner class - delays reset of currentFrag until after Activity has loaded (by setting static var)
	private class ResetDefaultFrag extends Thread {
		
		private MainActivity m;
		private Integer fragPosition;

		 public ResetDefaultFrag(MainActivity mainA, Integer Frag){
			m = mainA;
			fragPosition = Frag;
		    }
		
		 public void run() {
	         try {
	             sleep(500);
	         } catch (InterruptedException e) {
	             e.printStackTrace();
	         }

	         m.setFragment(fragPosition)  ;
	     }
	}
}
