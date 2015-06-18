package lcs;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;


import com.sun.org.apache.bcel.internal.generic.IUSHR;


public class LcsInput implements LcsListObject {
	public final static int TYPE_BUTTON =  0;
	public final static int TYPE_SWITCH =  1;
	public final static int TYPE_SLIDER =  2;
	public final static int TYPE_ANALOG =  3;
	public final static int TYPE_SENSOR =  4;
	
	private String name;
	private String txt;
	private String action = null;
	private int type = -1;
	private boolean visible = false;
	private int xPos = -1;
	private int yPos = -1;
	private int channel = -1;
	private int cardNo = -1;
	private int delay = -1;
	private Calendar start = null;
	private Calendar stop = null;
	private Component comp = null;
	
	public LcsInput()
	{
		start = Calendar.getInstance();
		stop = Calendar.getInstance();
	}

	
	public Component getComp()
	{
		return comp;
	}


	public void setComp(Component comp)
	{
		this.comp = comp;
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
	
	public int getDelay()
	{
		return delay;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}

	public boolean checkData()
	{
		if (type != -1 &&
		channel != -1 &&
		cardNo != -1 &&
		action != null &&
		type != TYPE_BUTTON &&
		type != TYPE_SLIDER)
		{
			return true;
		}
		else if (type != -1 &&
		action != null &&
		(type == TYPE_BUTTON || type == TYPE_SLIDER))
		{
			if (xPos != -1 &&
			yPos != -1)
			{
				return true;
			}
		}
		else if (type != -1 &&
		channel != -1 &&
		cardNo != -1 &&
		action != null &&
		type==TYPE_SENSOR)
		{
			if (start != null && stop != null)
			{
				if (start.after(stop))
				{
					return false;
				}
			}
			
			if (delay!=-1)
			{
				return true;
			}
		}
		return false;
	}
	
	public int getCardNo() 
	{
		return cardNo;
	}
	public void setCardNo(int cardNo) 
	{
		this.cardNo = cardNo;
	}
	public int getChannel() 
	{
		return channel;
	}
	public void setChannel(int channel) 
	{
		this.channel = channel;
	}
	public boolean isVisible()
	{
		return visible;
	}
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
	public String getName() 
	{
		return name;
	}
	public void setName(String name) 
	{
		this.name = name;
	}
	public String getTxt() 
	{
		return txt;
	}
	public void setTxt(String txt) 
	{
		this.txt = txt;
	}
	public int getType() 
	{
		return type;
	}
	public void setType(int type) 
	{
		this.type = type;
	}
	public String getAction()
	{
		return action;
	}
	public void setAction(String action)
	{
		this.action = action;
	}
	public int getXPos() 
	{
		return xPos;
	}
	public void setXPos(int pos) 
	{
		xPos = pos;
	}
	public int getYPos() 
	{
		return yPos;
	}
	public void setYPos(int pos) 
	{
		yPos = pos;
	}
	@Override
	public String toString() {
		String type = "";
		
		switch (getType()) {
		case TYPE_ANALOG:
			type = "Analog channel";
			break;
		case TYPE_BUTTON:
			type = "GUI button";
			break;
		case TYPE_SWITCH:
			type = "Switch";
			break;
		case TYPE_SLIDER:
			type = "GUI slider";
		case TYPE_SENSOR:
			type = "Movement sensor";
			break;
		}
		return "Input " + type + " " + getName() + " properties:\n" +
			   "--------------------------------------------------------------\n" +
			   "description: " + getTxt() + "\n" +
			   "     action: " + getAction() + "\n" +
			   "card number: " + getCardNo() + "\n" +
			   "    channel: " + getChannel() + "\n" +
			   " visibility: " + (isVisible() ? "visible" : "hidden") + "\n" +
			   "    x:y pos: " + getXPos() + ":" + getYPos() + "\n";
	}
}
