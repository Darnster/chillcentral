package uk.co.darnster.chillcentral;

/*
* 15/7/13 - No strings to externalise.  Maybe need to think about a toast for caught exception when sleep called on thread.
*
*/

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
//import android.util.Log;
import uk.co.darnster.chillcentral.Dlog;

public class DelayClearCallLog extends Thread {
	
	public Context context;
	public String phoneNumber;

	 public DelayClearCallLog(Context ctx, String pNumber){
		 
	        context = ctx;
	        phoneNumber = pNumber;
	    }
	
	 public void run() {
         try {
             sleep(3000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }

         clearCallLog(context, phoneNumber);
     }
	 
	 
	 public void clearCallLog(Context context, String phoneNumber) { 
		Long curTimeVal =  System.currentTimeMillis()/1000;
		Long lookBackTime = (long) 3000;  // used to subtract from now()
		Long nowMinuslookBackTime =  curTimeVal - lookBackTime;
		 
		Cursor c = context.getContentResolver().query(Calls.CONTENT_URI, 
							null, 
							Calls.NUMBER + " = '" + phoneNumber + "'" +
							" and " + Calls.TYPE + " = '" + Calls.MISSED_TYPE + "'" +
							" and " + Calls.DATE + " >= " +  Long.toString(nowMinuslookBackTime), 
							null,
							Calls.DATE + " DESC limit 1");  // get last entry
			
	 	if (c != null) {
	 		if (c.moveToFirst()) { // should only be one entry
	 			String call_id = c.getString(c.getColumnIndexOrThrow(Calls._ID));
				String phone_number = c.getString(c.getColumnIndexOrThrow(Calls.NUMBER));
				if (Dlog.getState()) {
					Dlog.d(this.getClass().toString() + " Call_ID: " + call_id);
					Dlog.d(this.getClass().toString() + " NUMBER: " + phone_number);
				}
				context.getContentResolver().delete(
	                    CallLog.Calls.CONTENT_URI,
	                    CallLog.Calls._ID + "= ? ",
	                    new String[] { call_id });
			
				} //end c.moveToFirst
			} // end if c != null
		}
	 
 
}
