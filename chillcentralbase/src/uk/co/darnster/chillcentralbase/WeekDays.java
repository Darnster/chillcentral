package uk.co.darnster.chillcentralbase;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;

public class WeekDays {

	public WeekDays() {
	}

	public String getDay(String weekDay, Context ctx) {
		Resources res = ctx.getResources();
		
		String Sunday = res.getString(R.string.sunday);
		String Monday = res.getString(R.string.monday);
		String Tuesday = res.getString(R.string.tuesday);
		String Wednesday = res.getString(R.string.wednesday);
		String Thursday = res.getString(R.string.thursday);
		String Friday = res.getString(R.string.friday);
		String Saturday = res.getString(R.string.saturday);
				
		final HashMap <String, String> weekDays = new HashMap <String, String>();
		
		weekDays.put("1", Sunday);
		weekDays.put("2", Monday);
		weekDays.put("3", Tuesday);
		weekDays.put("4", Wednesday);
		weekDays.put("5", Thursday);
		weekDays.put("6", Friday);
		weekDays.put("7", Saturday);
		
		return weekDays.get(weekDay);
	}
}
