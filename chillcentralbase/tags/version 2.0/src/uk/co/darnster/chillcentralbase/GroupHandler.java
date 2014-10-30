package uk.co.darnster.chillcentralbase;

/* Rewrite to use ArrayList? Nope ID and Display Name required
 * 
 * 1. Query Groups from phone (filter out only those with numeric IDs)
 * 
 * 2. Select * from GROUP_MAP and store in an ArrayList
 * 
 * 3. Delete * GROUP_MAP
 * 
 * 4. Insert all groups returned by 1. above
 * 
 * 
 * 16/7/13 - No strings to externalise :-)
 * 2/8/13 - Dlog added
 * 18/11/13 - Added "DELETED = 0" to getGroups method to omot groups deleted by the user
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uk.co.darnster.chillcentralbase.R;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Groups;


public class GroupHandler {
	
	String group_uri = "content://com.android.contacts/groups";
	
	public Context ctx;
	
	private String [] groupsubtract;
	
		
	public GroupHandler(Context context) {
		ctx = context;
		// Array of groups not to display:
		groupsubtract = new String [] {ctx.getString(R.string.grp_my_contacts),
										ctx.getString(R.string.grp_frequent_contacts),
										ctx.getString(R.string.grp_my_contacts)};
		
	}

	@SuppressLint("ParserError")
	public HashMap<String, String> getGroups(Context context) {
		/*
		 * Need to decide if we are only interested in
		 */
		ContentResolver contentResolver = context.getContentResolver();
		// http://developer.android.com/reference/android/database/Cursor.html

		final Cursor groupCursor = contentResolver.query(Uri.parse(group_uri),
				(new String[] { Groups._ID, Groups.TITLE}), 
				"DELETED = 0", 
				null, null);
		
		
		HashMap<String, String> groupDetail = new HashMap<String, String>(); 
		
		List <String> grpSub = Arrays.asList(groupsubtract); 
		
		// query active groups and append to array here
			
		
		while (groupCursor.moveToNext()) {

			String groupID = groupCursor.getString(groupCursor.getColumnIndexOrThrow(Groups._ID));
			String groupTitle = groupCursor.getString(groupCursor.getColumnIndexOrThrow(Groups.TITLE));
			
			
			if (Dlog.getState()) {
				Dlog.i(this.getClass().toString() + " Group Id: " + groupID  + " Group Title: " + groupTitle ); 
			}
			
			if (grpSub.contains( groupTitle ) )
				{
				// do nothing
				}
			else
				{
				if (filterGroupID(groupID)) {				
				groupDetail.put(groupTitle, groupID);	
				}
			}	
			
		}
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + "-groupLookup groups = " + groupDetail.toString() );
		}
		//Log.i("wp_POC","before loop over events");
		// For each calendar, display all the events from the previous week to the end of next week.
		return groupDetail;
	}

	
	
	public ArrayList<String> groupLookup(ArrayList<String> groupIDs, Context ctx) {
		/*
		 * @ in - array if groupIDs stored against contact
		 * @ out - array of de-duped group names 
		 * 
		 *
		 * For each groupID:
		 * 	Get the name from the cached table (GROUP_MAP?)
		 * 	Add name to groups array (maybe use a hash map to de-dupe)
		 *  
		 *  
		 */
		
		ArrayList<String> groupNames = new ArrayList<String>(); // will be returned to calling method	
		String groupMembership = joinGroupIDs(groupIDs); // stitch group ids into valid sql substring
		        
		ContentResolver contentResolver = ctx.getContentResolver();
		final Cursor groupCursor = contentResolver.query(Uri.parse(group_uri),
				(new String[] { Groups.TITLE}), 
				Groups._ID + " IN (" + groupMembership + ")",
				null, null);

		
		while (	groupCursor.moveToNext()) {
			String groupName = groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE));
			groupNames.add(groupName);
			if (Dlog.getState()) {
				Dlog.d(this.getClass().toString() + " groupLookup groupName = " + groupName);
			}
			
		}
		
		return groupNames;
	}

	
	
	private String joinGroupIDs (ArrayList<String> groupIDs) {
		/*
		 * Utility to join the strings in an ArrayList using the delimiter ','
		 * Should be easier than this - surely
		 * 
		 */
		
		String sqlMembershipString = "'";
		if (groupIDs.size() > 1) { 
			 
			for(int i = 0; i < groupIDs.size(); i++)  {
				String groupItem = groupIDs.get(i);	
				if (Dlog.getState()) {
					Dlog.d(this.getClass().toString() + "-joinGroupIDs -- groupItem = "  + groupItem);
				}
				// avoid additional comma at end!!
				if (i == groupIDs.size() -1 ) {
					
					sqlMembershipString = sqlMembershipString + groupItem  + "'";
				}
				else
				{
					// remove empty values!
					
					try {
						if (groupItem != null) {
							if (groupItem.length() > 0) {
						
							
							sqlMembershipString = sqlMembershipString + groupItem + "','";
							}
						}
							
						} finally {
								// ignore silently
						}
				}
			}
		}
		else
		{
			sqlMembershipString = sqlMembershipString + groupIDs.get(0);
			sqlMembershipString = sqlMembershipString + "'";
		}
		
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + "-joinGroupIDs sql_membership string = " + sqlMembershipString);
		}
		return sqlMembershipString;
	}
	
	private boolean filterGroupID(String groupID){
		/*
		 * Returns false when entries aren't an integer, therefore only getting the raw group ids.
		 * Returns false for phone number entries, e.g. 01484456456 with no std separator
		 * Also removes zero length entries returned from ContactGroups!
		 * 
		 * some entries are returned with no value but can't work out how to get shut here
		 */
		boolean isValidGroup = true;
		try  
		   {  
			
			if (groupID.length() > 3 || groupID.length() == 0 ) {
				isValidGroup = false;
			}
			
		   }  
		   catch( Exception e )  
		   {  
			   isValidGroup = false;  
		   }  
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " " + groupID + ": isValidGroup = "+ isValidGroup);
		}
		return isValidGroup;
	}	
	
	public ArrayList<String> filterGroupIDs(ArrayList<String> groupIDList){
		/*
		 * Removes entries which aren't an integer, therefore only getting the raw group ids.
		 * 
		 * some entries are returned with no value but can't work out how to get shut here
		 */
		
		for(int i = 0; i < groupIDList.size(); i++)  {
			try  
			   {  
			    String entry = groupIDList.get(i);
				Integer.parseInt( (String) entry );
				/* Removes phone number entries, e.g. 01484456456 with no std separator
				 * Also removes zero length entries returned from ContactGroups!
				 */
				
				if (entry.length() > 3 || entry.length() == 0 ) {
					groupIDList.remove(i);
				}
				
			   }  
			   catch( Exception e )  
			   {  
				   groupIDList.remove(i) ;  
			   }  
		}
		return groupIDList;
	}
	
    

}
