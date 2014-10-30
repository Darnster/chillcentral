/**
 * @author Danny
 * @version 0.1
 *
 * To Do
 * 
 * Changed to names of entries instead of ids to RULES table
 * ACTION_SCOPE in temporary state - needs changing once UI is plugged in 
 * 
 * 5/1/2014 Removed creation of entries for Tips and Tricks as this is now managed via a Raw text entry
 * 
 * Don't change Log statements to DLog - want to see these at all times!
 * 
 */


package Database;

import uk.co.darnster.chillcentralbase.R;
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
