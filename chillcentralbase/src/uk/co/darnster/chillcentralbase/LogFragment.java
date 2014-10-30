package uk.co.darnster.chillcentralbase;

/*
* 16/7/13 - String abstraction complete - no work required (deleted some unused Toast log messages)
* 2/8/13 - Dlog added
*
*/


import uk.co.darnster.chillcentralbase.R;
import Database.DatabaseHelper;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
//import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;
import android.widget.Button;

public class LogFragment extends Fragment {
	/** Called when the activity is first created. */
	private DatabaseHelper db;
	private SQLiteDatabase wpDB;
	private Cursor dbCursor;
	public ListView logList;
	public TabHost host; 
	public static Context ctx;
	private int layoutView = R.layout.frag_log;
	public View vFrag;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		
		ctx = getActivity();
		
		vFrag = inflater.inflate(layoutView, container, false);
		
		db = new DatabaseHelper(ctx);
		////////////////////////////////	
		
		host = (TabHost) vFrag.findViewById(R.id.logtab);

		host.setup();
		host.getTabWidget().setBackgroundResource(R.drawable.divider_narrow_light);
		
		TabSpec callsTab = host.newTabSpec("callTab");
		//callsTab.setIndicator(getResources().getString(R.string.call_logs));
		callsTab.setIndicator("", getResources().getDrawable(android.R.drawable.sym_action_call));
		callsTab.setContent(R.id.call_list);
		host.addTab(callsTab);
		
		
		TabSpec smsTab = host.newTabSpec("smsTab");
		//smsTab.setIndicator(getResources().getString(R.string.sms_logs));
		smsTab.setIndicator("", getResources().getDrawable(android.R.drawable.sym_action_chat));
		smsTab.setContent(R.id.sms_list);
		host.addTab(smsTab);
		// add a listener here

		
		// default tab
		host.setCurrentTabByTag("callTab");
		// Retrieve the TableLayout references
		// Give each TableLayout a blue header row with the column names
				
		host.setOnTabChangedListener(new OnTabChangeListener(){
			@Override
			public void onTabChanged(String tabId) {
				
			    if("smsTab".equals(tabId)) {
			    	processLogs("SMS");
			    }
			    
			    if ("callTab".equals(tabId)) {
			    	processLogs("CALL");
			    }
			    
			}});
		
		
	// get the logs - for calls by default when initialised

		processLogs("SMS");
		processLogs("CALL");
		
		
		//dbCursor.close();
		//wpDB.close();
	//return v;
	//return super.onCreateView(inflater, container, savedInstance);
	return vFrag;
	} // end onCreateView
	

	 public static final LogFragment newInstance(Context context) {
		 ctx = context;
		 LogFragment f = new LogFragment();
		 return f;
	 }
	
	public View processLogs(String log_type) {
		// Filter by log type (VOICE || SMS)
		
		try {
			wpDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(ctx, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
		String sql = "SELECT  rowid _id,* FROM AUDIT WHERE EVENT_TYPE = '" + log_type + "' ORDER BY AUDIT_ID DESC;";
		dbCursor = wpDB.rawQuery(sql, null);
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " SQL - log size = " + dbCursor.getCount() );
		}
		
		//logList = (ListView) findViewById(R.id.call_list);
		
		
		if (log_type == "SMS") {
			logList = (ListView) vFrag.findViewById(R.id.sms_list);
			ListAdapter adapter = new SimpleCursorAdapter(ctx, // Context.
	                R.layout.log_row_sms,
	                dbCursor, // Pass in the cursor to bind to.
	                // Array of cursor columns to bind to.
	                new String[] { "CONTACT_NAME","PHONE_NUMBER","DATE_TIME", "CONTENT" },
	                // Parallel array of which template objects to bind to those
	                // columns.
	                new int[] { R.id.name_entry,R.id.phone_number,R.id.block_time, R.id.msg_content});
			
			logList.setAdapter(adapter);	
			
			// do button UI stuff here :-)
			
			Button btnClearSMS = (Button) vFrag.findViewById (R.id.btnClear); 
			btnClearSMS.setText(R.string.clear_sms_log);    
			btnClearSMS.setOnClickListener(new View.OnClickListener() {
		        public void onClick(View view) {
		            clearSMSLog(view);
		        }
		    });
			
		} else {
			logList = (ListView) vFrag.findViewById(R.id.call_list);
			ListAdapter adapter = new SimpleCursorAdapter(ctx, // Context.
	                R.layout.log_row_calls,
	                dbCursor, // Pass in the cursor to bind to.
	                // Array of cursor columns to bind to.
	                new String[] { "CONTACT_NAME","PHONE_NUMBER","DATE_TIME" },
	                // Parallel array of which template objects to bind to those
	                // columns.
	                new int[] { R.id.name_entry,R.id.phone_number,R.id.block_time});
			
			logList.setAdapter(adapter);	
			
			// do button UI stuff here :-)
			
			Button btnClearCalls = (Button) vFrag.findViewById (R.id.btnClear); 
			btnClearCalls.setText(R.string.clear_call_log);    
			btnClearCalls.setOnClickListener(new View.OnClickListener() {
		        public void onClick(View view) {
		            clearCallLog(view);
		        }
		    });

			
			}
		return vFrag;
		}
	    
	
	public OnItemLongClickListener listener = new OnItemLongClickListener() {

       	@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
       		// need to get id of row selected and home in on the text I want to copy !!!
			 EditText messageText = (EditText) vFrag.findViewById(R.id.msg_content);
			 String message = messageText.getText().toString();
			 if (Dlog.getState()) {
				 Dlog.i(this.getClass().toString() + " message = " + message);
			 }
			 //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			return true;
		}

    };
    
    public void clearCallLog(View view) {
    	// TODO
    	((MainActivity) getActivity()).OpenDeleteLogDialog("CALL");
    }
	
    public void clearSMSLog(View view) {
    	// TODO
    	((MainActivity) getActivity()).OpenDeleteLogDialog("SMS");
    }
    

}