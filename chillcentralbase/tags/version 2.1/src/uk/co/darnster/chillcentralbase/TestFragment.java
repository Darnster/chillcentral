package uk.co.darnster.chillcentralbase;

/*
 * Need to comment in try and catch before this goes Live. Done 29/7/13
 * 
 * 29/7/13 - string externalisation complete.
 * 2/8/13 - Dlog added
 * 
 */

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.support.v4.app.Fragment;

import android.view.View.OnClickListener;

public class TestFragment extends Fragment {
	
	View feedbackView;
	View groupView;
	View calView;
	View tagView;
	TextView groupText;
	TextView calText;
	TextView tagText;
	
	public static final String TEST = "TestNumber"; // String
	String stringTest;
	
	public SharedPreferences appSettings;
	
	public static final String APP_SETTINGS = "AppSettings"; // String to hold
	public static Context ctx;
	private int layoutView = R.layout.frag_test;
	public static View vFrag;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		ctx = getActivity();
		
		View v = inflater.inflate(layoutView, container, false);
		
		// read default phone number form SharePreferences
		appSettings = ctx.getSharedPreferences(APP_SETTINGS, 0);
        try {
        	stringTest = appSettings.getString(TEST, "");        
        	} 
        catch (NullPointerException e) {
        	stringTest = "";        	
        }
        EditText testNumber = (EditText) v.findViewById( R.id.phone_number );       
        testNumber.setText(stringTest);
        
        Button testButton = (Button) v.findViewById( R.id.testRule );
        testButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	testCall(v);
            }
         });
        
		return v;
	}
	

	/*public void onResume() {
		
		
	}*/
		//showUpgradeDialog = true; // used to control whether a particular screen should show the upgrade Dialog
		
	
	 public static final TestFragment newInstance(Context context) {
		 ctx = context;
		 TestFragment f = new TestFragment();
		   return f;

		 }
	 
	public void testCall(View view) {
		
		EditText phoneNumberEditText = (EditText) getView().findViewById( R.id.phone_number );
		
		String phoneNumber = phoneNumberEditText.getText().toString();
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + "test phoneNumber: " + phoneNumber);
		}
		
		//################################### load admin #####################################
		if (phoneNumber.equals("#0#1#2")) {
			/*finish();
	    	startActivity(new Intent(this, AdminActivity.class));*/
			
		}
		else
		{
		
		CommsHandler cH = new CommsHandler();
		try {
			// need to check boolean return value and block the call
			  	if (cH.handleEvent(phoneNumber, "CALL", "", ctx)){
				// http://androidsourcecode.blogspot.in/ thank you very much for the line below:
			  	//Toast.makeText(this, "-----Call blocked------", Toast.LENGTH_LONG).show();
			  	ArrayList<String> resultArray = cH.getActiveRule();
			  	
			  	/*ruleInfo.add(activeRule.get("group").toString());
				ruleInfo.add(activeRule.get("calendar").toString());
				ruleInfo.add(activeRule.get("tag").toString());
				ruleInfo.add(activeRule.get("callBlock").toString());
				ruleInfo.add(activeRule.get("smsBlock").toString());
				*/
			  	
			  	groupText = ( TextView ) getView().findViewById( R.id.rule_group);
			  	groupText.setText(resultArray.get(0));
			  	calText = ( TextView ) getView().findViewById( R.id.rule_calendar);
			  	calText.setText(resultArray.get(1));
			  	tagText = ( TextView ) getView().findViewById( R.id.tag_text );
			  	tagText.setText(resultArray.get(2));
			  	
			  	
			  	feedbackView = ( View ) getView().findViewById( R.id.feedback_row);
			  	feedbackView.setVisibility(View.VISIBLE);
			  	groupView = ( View ) getView().findViewById( R.id.group_row );
			  	groupView.setVisibility(View.VISIBLE);
			  	calView = ( View ) getView().findViewById( R.id.calendar_row );
			  	calView.setVisibility(View.VISIBLE);
			  	tagView = ( View ) getView().findViewById( R.id.tag_row );
			  	tagView.setVisibility(View.VISIBLE);
			  	
			  	// read audit and report back??
			  	// get RuleID
			  	// Get Calendar, Group and Tag :-)
		  }
			  	else
			  	{
			  		feedbackView = ( View ) getView().findViewById( R.id.feedback_row);
			  		feedbackView.setVisibility(View.GONE);
				  	groupView = ( View ) getView().findViewById( R.id.group_row );
				  	groupView.setVisibility(View.GONE);
				  	calView = ( View ) getView().findViewById( R.id.calendar_row );
				  	calView.setVisibility(View.GONE);
				  	tagView = ( View ) getView().findViewById( R.id.tag_row );
				  	tagView.setVisibility(View.GONE);
			  	Toast.makeText(ctx, R.string.test_no_matching_rules, Toast.LENGTH_LONG).show();
			  	}
		  
		  
		  } catch (Exception e) {
			Toast.makeText(ctx, R.string.unable_to_pass_call_to_handler, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
	}
	}  // end Admin 
}
