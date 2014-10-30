package uk.co.darnster.chillcentralbase;

/*
 * Need to rewrite onHandleIntent method to:
 * 
 * Query Calendars against the rules table and set IS_ACTIVE
 * 
 * 
 * 29/7/13 - Not really sure if this class is required, but it is called from PhoneReceiver!
 * 
 * 29/7/13 - String externalisation complete - no action required!
 * 2/8/13 - Dlog added
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//import android.util.Log;
import uk.co.darnster.chillcentralbase.Dlog;

public class MonitorService extends Service {

	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		// this method sets up listeners
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " Service start called!");
		}
	    // need to create an instance of the service that listens for incoming calls and texts
	    //IntentFilter phoneFilter = new IntentFilter(Intent );
	    //Toast.makeText(this, R.string.intent_registered, Toast.LENGTH_LONG).show();
	    
	    return(START_NOT_STICKY);
	  }
	  
	  @Override
	  public void onDestroy() {
		  if (Dlog.getState()) {
			  Dlog.d(this.getClass().toString() + " Service stop called");
		  }
	  }
	  
	  @Override
	  public IBinder onBind(Intent intent) {
	    return(null);
	  }
}
	  