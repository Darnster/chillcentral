/*
 * 
 * ICS onwards uses CalendarContract.Calendars
 * 
 * this class uses the version introduced from ICS onwards
 *  
 * 
 * Google calendar is id #3
 * 
 * TO DO
 * 1. Update to remove "PC Sync", "Tasks", "People" calendars from returned results. Done - 
 * 
 * 12/7/13 String abstraction almost complete
 * 14/7/13 abstracted "PC Sync", "Tasks", "People" for calendarsubtract (added a constructor to acquire Context and getEvent methods)
 * Also updated CommsHandler's call to CalendarHandler
 * 2/8/13 - Dlog added
 * 
 * 
 * http://stackoverflow.com/questions/7130025/cannot-read-recurring-events-from-android-calendar-programmatically/7133990#7133990
 * duration in events table is the length of each reccurring event but cannot be used as this wouldonly pick up the first recurring event.
 * Need to go yo instances table to check whether an event is active.
 * 
 */

package uk.co.darnster.chillcentral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import uk.co.darnster.chillcentral.R;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

public class CalendarHandler {
	
	public Context ctx; 
	
	private String [] calendarsubtract;
	
	public CalendarHandler(Context context) {
		ctx = context;
		// Need to remove unwanted calendars here
		calendarsubtract = new String [] {ctx.getString(R.string.cal_pc_sync), 
											ctx.getString(R.string.cal_tasks),
											ctx.getString(R.string.cal_people)};	
	}
	
	public HashMap<String, String> getCalendars(Context context) {
		
	String[] EVENT_PROJECTION = new String[] {
	       Calendars._ID,
	       Calendars.CALENDAR_DISPLAY_NAME, 
	};
	
	ContentResolver calResolver = context.getContentResolver();
	Uri uri = Calendars.CONTENT_URI;   
	
	// Submit the query and get a Cursor object back. 
	Cursor calCursor = calResolver.query(uri, EVENT_PROJECTION, null, null, null);
	// MAx Calendars set to 20 
	HashMap<String, String> calendarIds = new HashMap<String, String>();

	List <String> calSub = Arrays.asList(calendarsubtract); 
	
	while (calCursor.moveToNext()) {

		String calendar_id = calCursor.getString(calCursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID));
		String displayName = calCursor.getString(calCursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
		
		if (Dlog.getState()) {
		Dlog.i(this.getClass().toString() + " Calendar Id: " + calendar_id 
								+ " Display Name: " + displayName
									); 
		}
		if (calSub.contains( displayName ) )
		{
		// do nothing
		}
	else
		{
		calendarIds.put(calendar_id, displayName);
		}
	}
	return calendarIds;

	}

	
	
	
	public ArrayList<String> getEvent(String calendarID , String pattern ) {
		/* ************* change to getEvent and boolean return type *****************
		 * 
		 * 
		*/
		
		String pat;
		
		// cursor return vars - for single and instance
		String event = "";
		String event_id;
		String calendar_id;
		String title;
		String begin;
		String end;
		String duration;
		
		
		pat = cleanPattern(pattern);
		ArrayList<String> events = new ArrayList<String>();
		ContentResolver contentResolver = ctx.getContentResolver();
		long now = new Date().getTime();
		
		//String[] queryArgs = (new String [] {  CalendarContract.Events.DTSTART,  CalendarContract.Events.DTEND,  CalendarContract.Events.TITLE });
		String query = "CALENDAR_ID = " + calendarID + 
					" and lower(TITLE) = '" + pat + "'";
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " query = " + query);
		}
		Cursor eventCursor = contentResolver.query(CalendarContract.Events.CONTENT_URI,
				new String[] {  CalendarContract.Events.CALENDAR_ID, 
				CalendarContract.Events._ID,
				CalendarContract.Events.TITLE, 
				CalendarContract.Events.DTSTART,
				CalendarContract.Events.DTEND, 
				CalendarContract.Events.ALL_DAY,
				CalendarContract.Events.DURATION}, 
				query,
				null, 
				null); 
		if (Dlog.getState()) {
			Dlog.i(this.getClass().toString() + " CalendarHandler - getEvents call to cursor complete");		
			Dlog.i(this.getClass().toString() + " CalendarHandler - getEvents, records = " + eventCursor.getCount() );
		}
		while (eventCursor.moveToNext()) {
			event = "";
			event_id = eventCursor.getString(eventCursor.getColumnIndexOrThrow(CalendarContract.Events._ID));
			calendar_id = eventCursor.getString(eventCursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID));
			title = eventCursor.getString(eventCursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE));
			begin = eventCursor.getString(eventCursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART));
			end = eventCursor.getString(eventCursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND));
			duration = eventCursor.getString(eventCursor.getColumnIndexOrThrow(CalendarContract.Events.DURATION));
			
			// Single instance entries (don't have "DURATION" populated)
			// if duration is null
			if (duration == null) {
				// check times and add event as usual
				long start = Long.parseLong(begin);
				long finish = Long.parseLong(end);
				
				if ( start <= now && finish >= now) {
					event += "Calender ID: " + calendar_id + "\nTitle: " + title + "\nStart: " + begin + "\nEnd: " + end + "\nDuration: " + duration + "\n\n";
					events.add(event);
					if (Dlog.getState()) {
						Dlog.i(this.getClass().toString() + " - getEvents (single) " + event);
					}
				}
			}
			
			else // if duration is not null take id and pass this into instances table and query for current entry
			{

				String instanceQuery = "EVENT_ID = " + event_id + 
						" AND begin <= " + now +
						" AND end >= " +  now;

				Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
				ContentUris.appendId(eventsUriBuilder, now);  // don't know what is going on with these - must read up
				ContentUris.appendId(eventsUriBuilder, now);  // don't know what is going on with these - must read up
				Uri eventsUri = eventsUriBuilder.build();
				Cursor instanceCursor = null; 
				instanceCursor = ctx.getContentResolver().query(eventsUri, 
														new String[] {  CalendarContract.Instances.EVENT_ID,
														CalendarContract.Instances.BEGIN,
														CalendarContract.Instances.END}, 
														instanceQuery,
														null, 
														null);
				
				while (instanceCursor.moveToNext()) {
					event_id = instanceCursor.getString(instanceCursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID));
					begin = instanceCursor.getString(instanceCursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN));
					end = instanceCursor.getString(instanceCursor.getColumnIndexOrThrow(CalendarContract.Instances.END));
					event += "Calender ID: " + calendar_id + "\nTitle: " + title + "\nStart: " + begin + "\nEnd: " + end + "\nDuration: " + duration + "\n\n";
					events.add(event);
					if (Dlog.getState()) {
						Dlog.i(this.getClass().toString() + " - getEvents (multi) " + event);
					}

				} // end instanceCursor while loop
				
			} // end of Single vs recurring instance processing
						
		} // end while statement
		return events;
	}	

	private static String cleanPattern(String pattern){
		// Converts to lower and removes whitespace
		String pat = pattern.toLowerCase();
		String cleanedPattern = pat.trim();
		return cleanedPattern;
	}

}
