package uk.co.darnster.chillcentralbase;

/*
 * Decided to do away with group_map table in db and instead query the groups on the phone directly in groupLookup
 * 
 * 12/7/13 String abstraction complete (no action required)
 * 2/8/13 - Dlog added
 * 18/12/13 - modified indexing on %number in getContact to prevent Index Out Of bounds Exception
 * 28/2/14 - updated contactDetail so that origonal phone number is stored in audit
 */


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
//import android.util.Log;
import uk.co.darnster.chillcentralbase.Dlog;


// extends Activity so it can call getContentResolver
public class ContactsHandler extends Activity {

	public HashMap<String, String> contactDetail = new HashMap<String, String>();
	public ArrayList<String> contactGroupNames = new ArrayList<String>();
	

	
	public ContactsHandler() {
		// TODO Auto-generated constructor stub
		// see no need to add anything here
	}

	
	public HashMap<String, String> getContact(String phoneNumber, Context ctx)
    {
		// probably need to just return ID at this point as a different content uri is required to get groups.
		String capturedPhoneNumber = phoneNumber;
		String contactDetails  = "";  // used to write all data to file #####
		contactDetails += "Number passed in " + phoneNumber + "\n";  //####
	    // substring of phoneNumber - last 10 digits
		try {
			if (phoneNumber.length() > 10 ) {
				phoneNumber = phoneNumber.substring(phoneNumber.length() - 10); // remove +440, 440 or (44)0
			}
		} catch (IndexOutOfBoundsException e) {
			// don't need to do anything - id number is shorter just use it
			e.printStackTrace();
		} 
		
		contactDetail = new HashMap<String , String>();
		ContentResolver localContentResolver = ctx.getContentResolver();
		Cursor contactLookupCursor =  
		   localContentResolver.query(
		            Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)), 
		            new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID, PhoneLookup.NUMBER }, 
		            PhoneLookup.NUMBER + " LIKE ? AND " + PhoneLookup.IN_VISIBLE_GROUP + " =?",
		            new String[] { "%" + phoneNumber, "1" }, // like wildcard, visible_group - assumed 1 = true
		            null);
		try {
		while(contactLookupCursor.moveToNext()){
		    String contactId = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
		    String contactName = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
		    String returnedPhonNumber = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup.NUMBER));
		    
		    contactDetail.put("phoneNumber",capturedPhoneNumber); // capture original number not the substring version
		    contactDetail.put("contactID",contactId);
		    contactDetail.put("contactName", contactName);
		    
		    contactDetails += phoneNumber + ",";  //#### 
		    contactDetails += returnedPhonNumber + ",";
		    contactDetails += contactId + ","; //####
		    contactDetails += contactName + "\n"; //####
		    
		    
		    if (Dlog.getState()) {
		    	Dlog.d(this.getClass().toString() + " getContact - phoneNumber" + phoneNumber);
		    	Dlog.d(this.getClass().toString() + " getContact - contactID" + contactId);
		    	Dlog.d(this.getClass().toString() + " getContact - contactName" + contactName);
		    	}


		    }
		
		} 
		finally 
		{
		contactLookupCursor.close();
		}
		
		if (Dlog.getState() ) {
			File contactsOutput = getTarget("retrievedContact.csv"); // ####
			try {
				save(contactDetails, contactsOutput); // ####
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // ####
		}
		return contactDetail;
				
    }
		

	public void getAllContacts(Context ctx) throws IOException
    {
		// this method gets all phone number, contact_id and display name and writes to a file in external storge (SD card)
			
		String contactDetails  = "";  // used to write all data to file
		String sortOrder = "ORDER BY NUMBER ASC";
		ContentResolver localContentResolver = ctx.getContentResolver();
		Cursor contactLookupCursor =  
		   localContentResolver.query(
				   Contacts.CONTENT_URI,
		            new String[] {Contacts.DISPLAY_NAME, Contacts._ID, Contacts.HAS_PHONE_NUMBER }, 
		            null, 
		            null, 
		            null);
		try {
			if(contactLookupCursor.moveToFirst()){
				do {
					
					if(contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(Contacts.HAS_PHONE_NUMBER)).equals("1")) {
						String contactName = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
						String contactID = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(Contacts._ID));
										    
					    Cursor pCur = localContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ contactID }, null);
			            while (pCur.moveToNext())
			            	{
							String phoneNumber = pCur.getString(pCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
							contactDetails += contactName + ",";
						    contactDetails += contactID + ",";
						    contactDetails += phoneNumber + "\n";
							//break;
			            	}
			            pCur.close();
					}
				    
				    /*if (Dlog.getState()) {
				    	Dlog.d(this.getClass().toString() + " getContact - phoneNumber" + phoneNumber);
				    	Dlog.d(this.getClass().toString() + " getContact - contactID" + contactId);
				    	Dlog.d(this.getClass().toString() + " getContact - contactName" + contactName);
				    	} */
				    
				} while (contactLookupCursor.moveToNext()) ;
				
			}
		}
		finally 
		{
		contactLookupCursor.close();
		}
		if (Dlog.getState() ) {			
			File contactsOutput = getTarget("allContacts3.csv");
			save(contactDetails, contactsOutput);
		}
     }
		
	
	private File getTarget(String fileName) {
		File root=null;
		root = Environment.getExternalStorageDirectory();
		return(new File(root, fileName));
	}

	private void save(String text, File target) throws IOException {
	    FileOutputStream fos=new FileOutputStream(target);
	    OutputStreamWriter out=new OutputStreamWriter(fos);
	    out.write(text);
	    out.flush();
	    fos.getFD().sync();
	    out.close();
	  }
	
	public boolean checkGroupMembership(String contactID, Context ctx) {
		/*
		 * mapping table of id -> group created to cater for ids with same group name
		 * 
		 * Pass contact_id in here 
		 * Then get all the groups for this user (GROUP_SOURCE_ID)
		 * Get result(s) and do lookup to mapping table in DB to get the groupNames
		 * Add matching groupNames to contactDetail:
		 * 		contactGroup0 to contactGroupx
		 * return boolean true if user is a member of 0 or more groups
		 * 
		 */
		
		ArrayList<String> groupIDList = new ArrayList<String>();
		boolean isMemberOfGroup = false;  //default set here and only updated to true if match is found
		
		ContentResolver groupContentResolver = ctx.getContentResolver();
		String where = "contact_id=" + contactID;
		Cursor contactGroupCursor =  
				groupContentResolver.query(
					Data.CONTENT_URI, 
		            new String[] {  GroupMembership.CONTACT_ID,  GroupMembership.GROUP_ROW_ID, GroupMembership.GROUP_SOURCE_ID, GroupMembership.IN_VISIBLE_GROUP }, 
		            where,
		            null, 
		            null);
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " count of records in checkGroupMembership: " + contactGroupCursor.getCount() + "\n");
		}
		try {
		while(contactGroupCursor.moveToNext()){
		    String groupRowId = contactGroupCursor.getString(contactGroupCursor.getColumnIndexOrThrow(GroupMembership.GROUP_ROW_ID));
		    contactDetail.put("groupID", groupRowId);
		    groupIDList.add(groupRowId);
		    if (Dlog.getState()) {
		    	Dlog.d(this.getClass().toString() + " checkGroupMembership - groupRowId : " + groupRowId);
		    	}
		    }

		
		} 
		finally 
		{
			contactGroupCursor.close();
		}
		
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " checkGroupMembership, groupIDList " + groupIDList.toString());
		}
		
		int records = groupIDList.size();

		if (records > 0) {	
			//  **** filter out those which aren't numeric (some entries are email addresses) ****
			GroupHandler groupH = new GroupHandler(ctx);
			groupIDList = groupH.filterGroupIDs(groupIDList);			
			// get the group names that the user is a member of
			ArrayList<String> groupNames = new ArrayList<String>();
			groupNames = groupH.groupLookup(groupIDList, ctx);
			//  before returning data - add groups to contact detail hash map?
			contactDetail.put("contactGroups", groupNames.toString());
			
			if (groupNames.size() > 0) {
				isMemberOfGroup = true;
			
				// loop over array and see if groupName passed in is in the groupNames array returned from groupLookup
				for (int i = 0; i < groupNames.size(); i++)
					{
					if (Dlog.getState()) {
						Dlog.d(this.getClass().toString() + " checkGroupMembership matching groupName = " +  groupNames.get(i));
					}
						contactGroupNames.add(groupNames.get(i));
					}
				}
			}
		
		return isMemberOfGroup;
	}
	

	public ArrayList<String> getcontactGroupNames() {
		/* utility method
		 * 
		 * checkGroupMembership must have been called first though!
		 * 
		 */
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " getcontactGroupNames +++getcontactGroupNames called here+++");
		}

		return contactGroupNames;
	}
	
	
/*
 * 
 *   ***** UTILITY / PRIVATE METHODS ****
 * 
 */
	
	
	public HashMap<String, String> getContactDetail() {
		// should only call this after getContact and checkGroupMembership  been called
		return contactDetail;
	}

	
}
