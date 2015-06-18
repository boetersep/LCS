package k8061;

import javax.usb.util.UsbUtil;

public class K8061Dimmer implements Runnable 
{
	private double speed;
	private int stopValue;
	private int channel;
	private short value;
	private boolean dimDown;
	private boolean dimUp;

	public K8061 k8061InterfaceCard;
	
	public K8061Dimmer(K8061 k8061InterfaceCard)
	{
		this.k8061InterfaceCard = k8061InterfaceCard;
	}
	
	public void run()
	{
		while(true)
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (dimUp)
			{
				for (double i = value ; value <= stopValue ;)
				{
					try {
						Thread.sleep(11);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	
					k8061InterfaceCard.sendCommand(new byte[]{K8061.COMMAND_WRITE_ANALOG, (byte) channel,
							 (byte) value,
							 (byte) ((value >>> 8) & 0xFF)});
					System.out.println(value);
					i = i + speed;
					value = (short) i;
				}
				value = (short) stopValue;
				dimUp = false;
			}
			if (dimDown)
			{
				for (double i = value ; value >= stopValue ;)
				{
					try {
						Thread.sleep(11);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					k8061InterfaceCard.sendCommand(new byte[]{K8061.COMMAND_WRITE_ANALOG, (byte) channel,
							 (byte) value,
							 (byte) ((value >>> 8) & 0xFF)});
					System.out.println(value);
					
					i = i - speed;
					value = (short) i;
				}
				value = (short) stopValue;
				dimDown = false;
			}
		}
	}
	
	public void setChannel(int channel)
	{
		this.channel = channel;
	}
	
	public void dimUp(int stopValue, double speed)
	{
		dimDown = false;
		this.stopValue = stopValue;
		this.speed = speed;
		dimUp = true;
	}

	public void dimDown(int stopValue, double speed)
	{
		dimUp = false;
		this.stopValue = stopValue;
		this.speed = speed;
		dimDown = true;
	}
}
