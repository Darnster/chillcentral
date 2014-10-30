package uk.co.darnster.chillcentralbase;

/*
 * Need to comment in try and catch before this goes Live
 * 
 * 12/7/13 String abstraction complete
 * 2/8/13 - Dlog added
 */



import uk.co.darnster.chillcentralbase.R;
import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class AndroidVersionFailActivity extends BlankActivity {
	private int layoutView = R.layout.activity_android_version_fail;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutView);
        // read default phone number form SharePreferences
        /* Action Bar - added to disable "Home" navigation as this was preventing process from ending
         * if this was pressed before the "back" button
        */
        ActionBar actionBar = getActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.app_name_action_bar);
        ColorDrawable colorDrawable = new ColorDrawable();
        colorDrawable.setColor(getResources().getColor(R.color.darkgrey));
        actionBar.setBackgroundDrawable(colorDrawable);
	    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use an inflater to populate the ActionBar with items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home_only, menu);
        return true;
    }
	
	 @Override
		public void onBackPressed() {
	    	android.os.Process.killProcess( android.os.Process.myPid() ) ;
			
		}
	 

	 public void closeWP(View view) {
	    	android.os.Process.killProcess( android.os.Process.myPid() ) ;
	    	
	 }
}
