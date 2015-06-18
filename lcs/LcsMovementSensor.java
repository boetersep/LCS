package lcs;
public class LcsMovementSensor implements Runnable
{
	/**
	 * True if the sensor is activated during 
	 * the last number of seconds indicated 
	 * by the delay variable.
	 */
	private boolean active = false;
	
	/**
	 * The time in seconds the 'active' 
	 * variable is true after a movement is detected.
	 */
	private int delay = 10;
	
	private long timeStateChanged = 0;
	
	public void setDelay(int delay)
	{
		this.delay = delay;
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(50);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			if ((System.currentTimeMillis() - timeStateChanged) < (delay * 1000))
			{
				active = true;
			}
			else
			{
				active = false;
			}
		}
	}

	public void movementDetected()
	{
		timeStateChanged = System.currentTimeMillis();
	}

	public boolean isActive()
	{
		return active;
	}
}
