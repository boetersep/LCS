package lcs;

import java.util.ArrayList;
import java.util.Calendar;


public class LcsTimetrigger implements LcsListObject {
	private String name;
	private Calendar start = null;
	private Calendar stop = null;
	
	private String action;
	public boolean checkData()
	{
		if (start != null &&
			stop != null &&
			start.before(stop))
		{
			return true;
		}
		return false;
	}
	
	public String getAction() 
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public LcsTimetrigger()
	{
		start = Calendar.getInstance();
		stop = Calendar.getInstance();
	}
	
	public void setStartTime(int hour, int minute)
	{
		start.setTimeInMillis(0);
		start.set(Calendar.HOUR_OF_DAY, hour);
		start.set(Calendar.MINUTE, minute);
		start.set(Calendar.SECOND, 0);
	}
	
	public void setStopTime(int hour, int minute)
	{
		stop.setTimeInMillis(0);
		stop.set(Calendar.HOUR_OF_DAY, hour);
		stop.set(Calendar.MINUTE, minute);
		stop.set(Calendar.SECOND, 0);
	}

	public Calendar getStartTime()
	{
		return start;
	}
	
	public Calendar getStopTime()
	{
		return stop;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	@Override
	public String toString() {
		return "Timetrigger " + getName() + " properties:\n" +
			   "---------------------------------------------\n" +
			   "     action: " + getAction() + "\n" +
			   "         on: " + getStartTime().getTime() + "\n" +
			   "        off: " + getStopTime().getTime() + "\n";
	}

}
