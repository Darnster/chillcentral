package uk.co.darnster.chillcentral;

/*
 * 29/7/13 - strings externalised
 * 2/8/13 - Dlog added
 */

import uk.co.darnster.chillcentral.R;
import Database.DatabaseHelper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class TipsTricksActivity extends BlankActivity {
	/** Called when the activity is first created. */
	private DatabaseHelper db;
	private SQLiteDatabase wpDB;
	private Cursor dbCursor;
	public ListView tipList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tips_tricks);
		db = new DatabaseHelper(this);
		processTips();		
	}
	
	
	public void processTips() {
		
		tipList = (ListView) findViewById(R.id.tips_list);	
		String sql = "SELECT  rowid _id,* FROM TIPS ORDER BY TIP_ID ASC;";
	
		try {
			wpDB = db.getReadableDatabase();
		}
		catch (Exception e) {
			Toast.makeText(this, R.string.err_unable_to_connect_to_database, Toast.LENGTH_LONG).show();
				e.printStackTrace();
		  }
		
		dbCursor = wpDB.rawQuery(sql, null);
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " tips cursor count = " + dbCursor.getCount() );  // returns 3 records
		}
		
		ListAdapter adapter = new SimpleCursorAdapter(this, // Context.
                R.layout.tips_row,
                dbCursor, // Pass in the cursor to bind to.
                // Array of cursor columns to bind to.
                new String[] { "TIP_TITLE","TIP_DETAIL" },
                // Parallel array of which template objects to bind to those
                // columns.
                new int[] { R.id.tip_title,R.id.tip_detail });
		
		tipList.setAdapter(adapter);	
			}
	      
	
   @Override
	public void onBackPressed() {
	    	dbCursor.close();
			wpDB.close();
	    	finish();
			startActivity(new Intent(this,
			MainActivity.class));
			
		}
    
}