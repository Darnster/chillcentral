package uk.co.darnster.chillcentralbase;

/*
 * 
 * http://www.quietlycoding.com/?p=9
 * 
 * TODO
 * 
 * implement methods to log to database - for support calls
 * 
 */
import android.util.Log;


public class Dlog {

private static boolean debug;
public static String TAG = "chill central";

public static boolean getState() {
	return debug;
}

public static void setState(boolean state) {
	debug = state;
}

public static void d( String msg) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
		Log.d(TAG, msg);
		}
	}

public static void i( String msg) {
		if (Log.isLoggable(TAG, Log.INFO)) {
		Log.i(TAG, msg);
		}
	}

public static void e( String msg) {
		if (Log.isLoggable(TAG, Log.ERROR)) {
		Log.e(TAG, msg);
		}
	}

public static void v( String msg) {
		if (Log.isLoggable(TAG, Log.VERBOSE)) {
		Log.v(TAG, msg);
		}
	}

public static void w( String msg) {
		if (Log.isLoggable(TAG, Log.WARN)) {
		Log.w(TAG, msg);
		}
	}

// still to implement

public void logToDB(){
	//TODO
	}
}