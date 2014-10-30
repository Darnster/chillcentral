package uk.co.darnster.chillcentralbase;

import java.util.ArrayList;
import java.util.HashMap;

import Database.DatabaseHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class ManageFragment extends ListFragment implements AdapterView.OnItemClickListener, View.OnClickListener {
	/** Called when the activity is first created. */
	private DatabaseHelper db;
	private SQLiteDatabase wpDB;
	private Cursor dbCursor;
	private final HashMap<Integer, String> RuleHash = new HashMap<Integer,String>();
	private String RuleToDelete;
	// Rules object initiated for ......
	private ArrayList<Rules> rule_parts;
	private RulesAdapter r_adapter;
	private int layoutView = R.layout.frag_manage;
	public static Context ctx;
	
	
	// kludge to get rule_id  when calling editRuleActivity (NEEDS TO BE SHARED ACROSS MULTIPLE ACTIVITIES)
	public static final String SHARED_VARS = "propRuleToEdit"; // String
    	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		
		ctx = getActivity();
		
		View v = inflater.inflate(layoutView, container, false);
						
		// Action Bar
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.show();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        ColorDrawable colorDrawable = new ColorDrawable();
        actionBar.setTitle(R.string.app_name_action_bar);
        colorDrawable.setColor(getResources().getColor(R.color.darkgrey));
        actionBar.setBackgroundDrawable(colorDrawable);		        
                
		ListView menuList = (ListView) v.findViewById(android.R.id.list);
		rule_parts = new ArrayList<Rules>();
		
		// need to abstract the DB call below - to make it easier to follow
		db = new DatabaseHelper(ctx); // need to call this because Activity not created on resume...
		
		String sql = "SELECT * FROM RULE ORDER BY RULE_ID ASC;";
		
		//Data structure to hold multidimensional array before calling RulesAdapter
		
		try {
			wpDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			try {
			Toast.makeText(ctx, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
		  }
		
		dbCursor = wpDB.rawQuery(sql, null);
		String ruleNumber = null, groupName = null, calendarName = null, eventTagText = null;
		Integer count = 1;
		
		
		
		/*if (dbCursor.getCount() == 0) {
			if (Dlog.getState()) {
				Dlog.d(this.getClass().toString() + "Rules in MainFragment = 0");
			}
			// load different fragment for layout here
			rule_parts.add(new Rules("Testing","Testing", "Testing"));
			RuleHash.put(1, "1");
		}
		
		else {*/
			
			
		
		while (	dbCursor.moveToNext()) {
				ruleNumber = dbCursor.getString(dbCursor.getColumnIndex("RULE_ID"));
				groupName = dbCursor.getString(dbCursor.getColumnIndex("GROUP_NAME"));
				calendarName = dbCursor.getString(dbCursor.getColumnIndex("CALENDAR_NAME"));
				eventTagText = dbCursor.getString(dbCursor.getColumnIndex("TAG"));

				if (Dlog.getState()) {
					Dlog.d(this.getClass().toString() + "Rules in MainFragment : ruleNumber: " + ruleNumber + "eventTagText: " + eventTagText);
				}
				
				rule_parts.add(new Rules(groupName,calendarName, eventTagText));
				
				RuleHash.put(count, ruleNumber); 
				count++;
				
				}

		//}		
		dbCursor.close();
		wpDB.close(); 
		
		
		
		// http://www.ezzylearning.com/tutorial.aspx?tid=1763429
		// Need to insert theRules Adapter here
		r_adapter = new RulesAdapter(inflater.getContext(), R.layout.rule_menu , rule_parts){
			public View getView(int position, View convertView, ViewGroup parent) {
	            View row =  super.getView(position, convertView, parent);
	            
	            View edit = row.findViewById(R.id.EditRule);
	                        
	            edit.setTag(position);
	            edit.setOnClickListener(ManageFragment.this);
	            
	            View delete = row.findViewById(R.id.DeleteRule);
	            delete.setTag(position);
	            delete.setOnClickListener(ManageFragment.this);
	            
	            return row;
			}
			};
		
		setListAdapter(r_adapter);
		menuList.setAdapter(r_adapter);
		menuList.setOnItemClickListener(this);
		//return super.onCreateView(inflater, container, savedInstance);
		return v;
	 
	} // end onCreateView
	
	@Override
    public void onClick(View v) {
		int ruleClicked;
		String ruleID;
        int id = v.getId();
		if (id == R.id.EditRule) {
			// do the normal thing 
        	// get ruleID from hashmap
        	ruleClicked = (Integer) v.getTag();
			ruleID = RuleHash.get( ruleClicked + 1); // ListView has index starting with 1 not zero
			SharedPreferences settings = ctx.getSharedPreferences(SHARED_VARS, 0);
			SharedPreferences.Editor prefEditor = settings.edit();
			prefEditor.putString("ruleToEdit", ruleID);
			prefEditor.commit();
			if (Dlog.getState()) {
    			Dlog.d(this.getClass().toString() + " ruleID set in mainActivity : " + ruleID);
    		}
			//finish();	
			startActivity(new Intent(ManageFragment.ctx, EditRuleActivity.class));
		} else if (id == R.id.DeleteRule) {
			ruleClicked = (Integer) v.getTag();
			ruleID = RuleHash.get( ruleClicked + 1); // ListView has index starting with 1 not zero
			RuleToDelete = ruleID;
			((MainActivity) getActivity()).OpenDeleteRuleDialog(RuleToDelete);
		} else {
			// ignore
		}	
        
        

	}
	

	 public static ManageFragment newInstance(Context context) {
		 ctx = context;
		 ManageFragment f = new ManageFragment();
		   return f;

		 }

 

	
	/////////////////////////////////////////////////////
    @Override
	public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long arg3) {	
    	// not implemented	
	}
    	
	public boolean onCreateOptionsMenu(Menu menu) {
        // use an inflater to populate the ActionBar with items
        MenuInflater inflater = ((Activity) ctx).getMenuInflater();
        inflater.inflate(R.menu.global_menu, menu);
  
	        return true;
    	}

}
