package uk.co.darnster.chillcentral;

/*
 * Need to comment in try and catch before this goes Live. Done 29/7/13
 * 
 * 29/7/13 - string externalisation complete.
 * 2/8/13 - Dlog added
 * 
 */

import java.util.ArrayList;

import uk.co.darnster.chillcentral.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends BlankActivity {

	View feedbackView;
	View groupView;
	View calView;
	View tagView;
	TextView groupText;
	TextView calText;
	TextView tagText;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        // read default phone number form SharePreferences
        stringTest = appSettings.getString(TEST, "");        
        EditText testNumber = (EditText) findViewById( R.id.phone_number );       
        testNumber.setText(stringTest);  
	    }
	
	 @Override
		public void onBackPressed() {
	    	finish();
			startActivity(new Intent(TestActivity.this,
			MainActivity.class));
			
		}
	 
	public void testCall(View view) {
		
		EditText phoneNumberEditText = (EditText) findViewById( R.id.phone_number );
		
		String phoneNumber = phoneNumberEditText.getText().toString();
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + "test phoneNumber: " + phoneNumber);
		}
		
		//################################### load admin #####################################
		if (phoneNumber.equals("#0#1#2")) {
			finish();
	    	startActivity(new Intent(this, AdminActivity.class));
			
		}
		else
		{
		
		CommsHandler cH = new CommsHandler();
		try {
			// need to check boolean return value and block the call
			  	if (cH.handleEvent(phoneNumber, "CALL", "", this)){
				// http://androidsourcecode.blogspot.in/ thank you very much for the line below:
			  	//Toast.makeText(this, "-----Call blocked------", Toast.LENGTH_LONG).show();
			  	ArrayList<String> resultArray = cH.getActiveRule();
			  	
			  	/*ruleInfo.add(activeRule.get("group").toString());
				ruleInfo.add(activeRule.get("calendar").toString());
				ruleInfo.add(activeRule.get("tag").toString());
				ruleInfo.add(activeRule.get("callBlock").toString());
				ruleInfo.add(activeRule.get("smsBlock").toString());
				*/
			  	
			  	groupText = ( TextView ) findViewById( R.id.rule_group);
			  	groupText.setText(resultArray.get(0));
			  	calText = ( TextView ) findViewById( R.id.rule_calendar);
			  	calText.setText(resultArray.get(1));
			  	tagText = ( TextView ) findViewById( R.id.tag_text );
			  	tagText.setText(resultArray.get(2));
			  	
			  	
			  	feedbackView = ( View ) findViewById( R.id.feedback_row);
			  	feedbackView.setVisibility(View.VISIBLE);
			  	groupView = ( View ) findViewById( R.id.group_row );
			  	groupView.setVisibility(View.VISIBLE);
			  	calView = ( View ) findViewById( R.id.calendar_row );
			  	calView.setVisibility(View.VISIBLE);
			  	tagView = ( View ) findViewById( R.id.tag_row );
			  	tagView.setVisibility(View.VISIBLE);
			  	
			  	// read audit and report back??
			  	// get RuleID
			  	// Get Calendar, Group and Tag :-)
		  }
			  	else
			  	{
			  		feedbackView = ( View ) findViewById( R.id.feedback_row);
			  		feedbackView.setVisibility(View.GONE);
				  	groupView = ( View ) findViewById( R.id.group_row );
				  	groupView.setVisibility(View.GONE);
				  	calView = ( View ) findViewById( R.id.calendar_row );
				  	calView.setVisibility(View.GONE);
				  	tagView = ( View ) findViewById( R.id.tag_row );
				  	tagView.setVisibility(View.GONE);
			  	Toast.makeText(this, R.string.test_no_matching_rules, Toast.LENGTH_LONG).show();
			  	}
		  
		  
		  } catch (Exception e) {
			Toast.makeText(this, R.string.unable_to_pass_call_to_handler, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
	}
	}  // end Admin 
}
