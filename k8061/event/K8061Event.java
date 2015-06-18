package k8061.event;


public class K8061Event
{
	private boolean powerPresent;
	
	private byte digitalChannelsIn;
	private byte digitalChannelsOut;
	private short[] analogChannelsIn;
	private short[] analogChannelsOut;
	
	private short pwmOut;

	private short eventType;

	private int channel;

	public short[] getAnalogChannelsIn()
	{
		return analogChannelsIn;
	}

	public void setAnalogChannelsIn(short[] analogChannelsIn)
	{
		this.analogChannelsIn = analogChannelsIn;
	}

	public short[] getAnalogChannelsOut()
	{
		return analogChannelsOut;
	}

	public void setAnalogChannelsOut(short[] analogChannelsOut)
	{
		this.analogChannelsOut = analogChannelsOut;
	}

	public byte getDigitalChannelsIn()
	{
		return digitalChannelsIn;
	}

	public void setDigitalChannelsIn(byte digitalChannelsIn)
	{
		this.digitalChannelsIn = digitalChannelsIn;
	}

	public byte getDigitalChannelsOut()
	{
		return digitalChannelsOut;
	}

	public void setDigitalChannelsOut(byte digitalChannelsOut)
	{
		this.digitalChannelsOut = digitalChannelsOut;
	}

	public boolean isPowerPresent()
	{
		return powerPresent;
	}

	public void setPowerPresent(boolean powerPresent)
	{
		this.powerPresent = powerPresent;
	}

	public short getPwmOut()
	{
		return pwmOut;
	}

	public void setPwmOut(short pwmOut)
	{
		this.pwmOut = pwmOut;
	}

	public short getEventType()
	{
		return eventType;
	}

	public void setEventType(short eventType)
	{
		this.eventType = eventType;
	}

	public void setChannel(int channel)
	{
		this.channel = channel;
	}
	
	public int getChannel() {
		return channel;
	}
}
