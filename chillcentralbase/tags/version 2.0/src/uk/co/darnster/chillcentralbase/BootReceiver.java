package uk.co.darnster.chillcentralbase;

/*
 * Plan here is as follows:
 * use AlarmManager to cache data in rules database at a defined interval
 * 
 * Note that I removed the sanity bootservice class as this doesn't look future proof according to pp 587 of MM book
 * 
 * 1/8/13 - Dlog integration - still need do think about how to pass global TAG into this! Sorted
 * 
 */



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.util.Log;
import uk.co.darnster.chillcentralbase.Dlog;
  

public class BootReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(Context contex, Intent i)
	{
		if (Dlog.getState()) {
			Dlog.d(this.getClass().toString() + " call to StartMonitor");
		}
		startMonitor(contex);
	}
	

	 static void startMonitor(Context ctxt) {

		    Intent i=new Intent(ctxt, MonitorService.class);
		    ctxt.startService(i);
		    // commented code out below as this really needs to be moved elsewwhere
		    
		  }
	 
}
