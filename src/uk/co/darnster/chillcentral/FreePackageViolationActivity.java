package uk.co.darnster.chillcentral;

/*
 * Need to comment in try and catch before this goes Live
 * 
 * 12/7/13 String abstraction complete
 * 2/8/13 - Dlog added
 */


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

public class FreePackageViolationActivity extends BlankActivity {

	LayoutInflater inflater;
	
	@SuppressLint("ResourceAsColor")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_package_violation);
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
        ////
        
        // 1) How to replace link by text like "Click Here to visit Google" and
        /* the text is linked with the website url ?
        TextView link = (TextView) findViewById(R.id.textView1);
        String linkText = "Visit the <a href='http://stackoverflow.com'>StackOverflow</a> web page.";
        link.setText(Html.fromHtml(linkText));
        link.setMovementMethod(LinkMovementMethod.getInstance());
        */
        // 2) How to place email address
        TextView email = (TextView) findViewById(R.id.mailto);
        String emailText = getString(R.string.free_package_violation_mailto_message);
        email.setText(Html.fromHtml(emailText));
        email.setLinkTextColor(R.color.darkblue);
        email.setMovementMethod(LinkMovementMethod.getInstance());
        
        TextView sla = (TextView) findViewById(R.id.sla_message);
        String slaText = getString(R.string.free_package_violation_sla_message);
        sla.setText(Html.fromHtml(slaText));
        sla.setLinkTextColor(R.color.darkblue);
        sla.setMovementMethod(LinkMovementMethod.getInstance());
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
