/**
 * @author Danny
 * @version 0.1
 *
 * To Do
 * 
 * Changed to names of entries instead of ids to RULES table
 * ACTION_SCOPE in temporary state - needs changing once UI is plugged in 
 * 
 */


package Database;

import uk.co.darnster.chillcentral.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME="wpv1.db";
	private static final int SCHEMA=1;
	private Context strContext;
	public static Resources resources;
	
	

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA);
		strContext = context;
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
		db.beginTransaction();
		ContentValues cv=new ContentValues();
		//resources = strContext.getResources();
				
		/*
		 * CREATE RULE TABLE
		 */

		//db.execSQL("CREATE TABLE RULE (RULE_ID INTEGER PRIMARY KEY AUTOINCREMENT, GROUP_NAME TEXT, TAG TEXT, CALENDAR_ID TEXT, CALENDAR_NAME TEXT, ACTION_NAME TEXT, ACTION_SCOPE NUMBER);");
		/*
		 * Table sctructure above is for fully normalised solution, where more than call and SMS need to be managed.
		 * 
		 */
		
		db.execSQL("CREATE TABLE RULE (RULE_ID INTEGER PRIMARY KEY AUTOINCREMENT, GROUP_NAME TEXT, TAG TEXT, CALENDAR_ID TEXT, CALENDAR_NAME TEXT, BLOCK_CALL TEXT, BLOCK_SMS TEXT);");
		db.execSQL("CREATE UNIQUE INDEX RULE_GROUP_IDX ON RULE(GROUP_NAME);");
		Log.i("schema_creator","create_table RULE successful");
		
		/*cv.put("GROUP_NAME","Coworkers");
		cv.put("TAG","get lost");
		cv.put("CALENDAR_ID","3");
		cv.put("CALENDAR_NAME","danny.ruttle@gmail.com");
		cv.put("ACTION_NAME","BLOCK");
		cv.put("ACTION_SCOPE",1);
						
		db.insert("RULE", "GROUP_ID", cv);
		
		cv.clear();
		cv.put("GROUP_NAME","Family");
		cv.put("TAG","holiday");
		cv.put("CALENDAR_ID","3");
		cv.put("CALENDAR_NAME","danny.ruttle@gmail.com");
		cv.put("ACTION_NAME","BLOCK");
		cv.put("ACTION_SCOPE",1);
						
		db.insert("RULE", "GROUP_ID", cv);
		
		Log.i("wp","record inserted into RULE table");*/
		
		/*
		 * CREATE ACTION TABLE
		 */

		db.execSQL("CREATE TABLE SCOPE (ACTION TEXT);");
		cv.clear();
		cv.put("SCOPE", "CALLS");
		cv.put("SCOPE", "SMS");
		cv.put("SCOPE", "EMAIL");
		cv.put("SCOPE", "CALENDAR_REMINDERS");
		cv.clear();
		
		Log.i("wp","create_table SCOPE successful");

		/*
		 * CREATE RULE_SCOPE TABLE
		 */
		
		db.execSQL("CREATE TABLE RULE_SCOPE (RULE_ID INTEGER, ACTION TEXT);");
		
		Log.i("wp","create_table RULE_SCOPE successful");
		
		/*
		 * CREATE AUDIT TABLE
		 */

		db.execSQL("CREATE TABLE AUDIT (AUDIT_ID INTEGER PRIMARY KEY AUTOINCREMENT, PHONE_NUMBER TEXT, CONTACT_NAME TEXT, RULE_ID NUMBER, EVENT_TYPE TEXT, CONTENT TEXT, DATE_TIME TEXT);");
		
		Log.i("schema_creator","create_table AUDIT successful");

		
		/*
		 * CREATE TIPS AND TRICKS TABLE ( strings held in Sting resources so they can be picked up per language ;-) )
		 */
		
		db.execSQL("CREATE TABLE TIPS (TIP_ID INTEGER PRIMARY KEY AUTOINCREMENT, TIP_TITLE TEXT, TIP_DETAIL TEXT);");
		
		// tip #1
		String tip1title  = strContext.getString(R.string.tip1title);
		String tip1detail  = strContext.getString(R.string.tip1detail);
		cv.put("TIP_TITLE",tip1title);
		cv.put("TIP_DETAIL",tip1detail);			
		db.insert("TIPS", "TIP_ID", cv);
		cv.clear();
		
		// tip #2
		String tip2title  = strContext.getString(R.string.tip2title);
		String tip2detail  = strContext.getString(R.string.tip2detail);
		cv.put("TIP_TITLE",tip2title);
		cv.put("TIP_DETAIL",tip2detail);			
		db.insert("TIPS", "TIP_ID", cv);
		cv.clear();
		
		// tip #3
		String tip3title  = strContext.getString(R.string.tip3title);
		String tip3detail  = strContext.getString(R.string.tip3detail);
		cv.put("TIP_TITLE",tip3title);
		cv.put("TIP_DETAIL",tip3detail);			
		db.insert("TIPS", "TIP_ID", cv);
		cv.clear();
		
		// tip #4
		String tip4title  = strContext.getString(R.string.tip4title);
		String tip4detail  = strContext.getString(R.string.tip4detail);
		cv.put("TIP_TITLE",tip4title);
		cv.put("TIP_DETAIL",tip4detail);			
		db.insert("TIPS", "TIP_ID", cv);
		cv.clear();
		
		// tip #5
		String tip5title  = strContext.getString(R.string.tip5title);
		String tip5detail  = strContext.getString(R.string.tip5detail);
		cv.put("TIP_TITLE",tip5title);
		cv.put("TIP_DETAIL",tip5detail);			
		db.insert("TIPS", "TIP_ID", cv);
		cv.clear();
		
		// tip #6
		String tip6title  = strContext.getString(R.string.tip6title);
		String tip6detail  = strContext.getString(R.string.tip6detail);
		cv.put("TIP_TITLE",tip6title);
		cv.put("TIP_DETAIL",tip6detail);			
		db.insert("TIPS", "TIP_ID", cv);
		cv.clear();
		
		Log.i("schema_creator","create_table TIPS and content added successfully");		
		
		db.setTransactionSuccessful();
		}
		finally {
		db.endTransaction();
		Log.i("wp","database initiated");
				
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new RuntimeException("How did we get here?");
	}

}
