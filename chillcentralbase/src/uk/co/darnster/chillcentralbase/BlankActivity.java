package uk.co.darnster.chillcentralbase;


/*
 * 
 * Created to avoid having to copy and paste Action Bar, options menu and on back pressed methods. 
 * 
 * Need to call BootReceiver from UI once this is built
 * only building for ICS and above where code is more stable for areas I need to access
 * 
 * Need to verify rules against actual calendars and groups each time the app is loaded - just in case they have changed.

 * 12/7/13 String abstraction complete
 * 2/8/13 - Dlog added
 */

import uk.co.darnster.chillcentralbase.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

public abstract class BlankActivity extends Activity {
	
	public SharedPreferences appSettings;
	
	public static final String APP_SETTINGS = "AppSettings"; // String to hold

	// using this to set App Prefs in config screen
	public static String INIT = "Init"; // String
	public static final String BLOCK = "Block"; // String
	public static final String NOTIFY = "Notify"; // String
	public static final String REMOVE_CALLS = "RemoveCalls"; // String
	public static final String TEST = "TestNumber"; // String
	public static final String EULA = "Eula"; // String
	public static final String TAG = "chill central"; // String for Dlog debug statements

	public boolean boolBlock, boolNotify, boolRemoveCalls;
	public String stringTest;
	public CheckBox cbBlock, cbNotify, cbRemoveCall, cbDevLogging;
	public EditText txtTestNumber;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	// App Config - Shared Prefs
	appSettings = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
	
	// Action Bar
    ActionBar actionBar = getActionBar();
    actionBar.show();
    actionBar.setDisplayHomeAsUpEnabled(true);
    ColorDrawable colorDrawable = new ColorDrawable();
    colorDrawable.setColor(getResources().getColor(R.color.darkgrey));
    actionBar.setTitle(R.string.app_name_action_bar);
    actionBar.setBackgroundDrawable(colorDrawable);
    
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
        int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			finish();
			backTarget();
		} else if (itemId == R.id.add_rule) {
			//finish();
        	startActivity(new Intent(ManageFragment.ctx, AddRuleActivity.class));
		} else if (itemId == R.id.about) {
			//AboutDialogFragment myDialogFragment = new AboutDialogFragment(this);
	    	//myDialogFragment.show(getFragmentManager(), "myDialogFragment");
	    	ChangeLog cl = new ChangeLog(ManageFragment.ctx, this.getString(R.string.about));
			cl.getFullLogDialog( false).show();
		} else if (itemId == R.id.tips) {
			//finish();
	    	TipsTricksDialog TTDlg = new TipsTricksDialog(ManageFragment.ctx, this.getString(R.string.title_tips_tricks));
			TTDlg.getFullTipsDialog( false).show();
		}

            return true;
        }

	
	
	
	@Override
	public void onBackPressed() {
			backTarget();
	    	finish();			
		} 

	private void backTarget() {
		// Used to redirect to appropraiate class
		startActivity(new Intent(this, MainActivity.class));
	}
	 
}
