package k8061;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import javax.usb.util.UsbUtil;

import k8061.event.K8061Event;


public class K8061 implements Runnable, K8061Constants, UsbPipeListener
{
	protected boolean emulate = false;

	private UsbHub rootHub = null;
	private UsbConfiguration config = null;
	private UsbInterface interf = null;
	private UsbPipe inPipe = null;
	private UsbPipe outPipe = null;
	private	UsbEndpoint inep;
	private UsbEndpoint outep;
	
	protected byte digitalChannelsIn = (byte) 0x00;
	protected byte digitalChannelsOut = (byte) 0x00;
	protected short[] analogChannelsIn = new short[] {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	protected short[] analogChannelsOut = new short[] {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	protected short pwmOut = 0;
	protected boolean powerPresent = false;
	protected short cardNumberIn = -1;
	protected short cardNumber = -1;
	
	protected long starttime;
	protected long uptime;
	
	private static List k8061cards;

	private EventListenerList listenersList; 
	private K8061Dimmer[] dimmers;
	private ThreadGroup dimmerThreadGroup;
	private K8061Event event;
	
	public K8061(int cardNumber)
	{
		new K8061(cardNumber, false);
	}
	
	public K8061(int cardNumber, boolean emulate)
	{
		this.cardNumber = (short)cardNumber;
		this.emulate = emulate;
		powerPresent = emulate;
		
		starttime = System.currentTimeMillis();
		k8061cards = new ArrayList(8);
		
		dimmers = new K8061Dimmer[analogChannelsOut.length];
		
		dimmerThreadGroup = new ThreadGroup("dimmerThreadGroup");
		
		listenersList = new EventListenerList();
		event = new K8061Event();

		if (!emulate) findRootHub();
		if (!emulate) findVellemanBoards(rootHub);
		if (!emulate) config = ((UsbDevice)k8061cards.get(0)).getUsbConfiguration((byte) 1);
		if (!emulate) interf = config.getUsbInterface((byte) 0x00);
		
		try
		{
			if (!emulate) interf.claim();
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		if (!emulate) outep = interf.getUsbEndpoint(USB_WRITE_ENDPOINT);
		if (!emulate) inep = interf.getUsbEndpoint(USB_READ_ENDPOINT);
		
		if (!emulate) outPipe = outep.getUsbPipe();
		if (!emulate) inPipe = inep.getUsbPipe();
		
		try
		{
			if (!emulate) outPipe.open();
			if (!emulate) inPipe.open();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!emulate) inPipe.addUsbPipeListener(this);
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(30);
			} catch (InterruptedException e)
			{
				System.err.println("Can't get no sleep.");
			}
			
			uptime = System.currentTimeMillis() - starttime;
	
			sendCommand(new byte[]{COMMAND_READ_12V});
			sendCommand(new byte[]{COMMAND_READ_ANALOG});
			sendCommand(new byte[]{COMMAND_READ_DIGITAL});
			
			event.setPowerPresent(powerPresent);
			event.setAnalogChannelsIn(analogChannelsIn);
			event.setDigitalChannelsIn(digitalChannelsIn);
			event.setAnalogChannelsOut(analogChannelsOut);
			event.setDigitalChannelsOut(digitalChannelsOut);
			event.setPwmOut(pwmOut);
		}
	}
	
	private synchronized void findVellemanBoards(UsbDevice device)
	{
		List attachedUsbDevices = ((UsbHub) device).getAttachedUsbDevices();

		for (int i = 0; i < attachedUsbDevices.size(); i++)
		{
			UsbDevice leafDevice = (UsbDevice) attachedUsbDevices.get(i);

			if (leafDevice.isUsbHub())
			{
				findVellemanBoards(leafDevice);
			}

			short vendorId = leafDevice.getUsbDeviceDescriptor().idVendor();
			short productId = leafDevice.getUsbDeviceDescriptor().idProduct();

			if ("10cf".equals(UsbUtil.toHexString(vendorId))
					&& "8061".equals(UsbUtil.toHexString(productId)))
			{
				k8061cards.add(leafDevice);
			}
		}
	}
	
	private void findRootHub()
	{
		UsbServices services = null;

		try
		{
			services = UsbHostManager.getUsbServices();
		} catch (UsbException uE)
		{
			throw new RuntimeException("Error: " + uE.getMessage());
		} catch (SecurityException sE)
		{
			throw new RuntimeException("Error: " + sE.getMessage());
		}

		try
		{
			rootHub = services.getRootUsbHub();
		}
		catch (UsbException uE)
		{
			throw new RuntimeException("Error: " + uE.getMessage());
		}
		catch (SecurityException sE)
		{
			throw new RuntimeException("Error: " + sE.getMessage());
		}
	}
	
	public void dataEventOccurred(UsbPipeDataEvent event)
	{
		final byte data[] = event.getData();
		
		switch (data[0])
		{
		case COMMAND_READ_12V:
			powerPresent = (data[1] == 0x01) ? true : false;
			break;
		case COMMAND_READ_CARDNUMBER:
			cardNumberIn = data[1];
			break;
		case COMMAND_READ_DIGITAL:
			if (data[1] != digitalChannelsIn)
			{
				digitalChannelsIn = data[1];
				fireK8061Event(EVENT_DIGITAL_IN, -1);
			}
			break;
		case COMMAND_WRITE_DIGITAL:
			digitalChannelsOut = data[1];
			fireK8061Event(EVENT_DIGITAL_OUT, -1);
			break;
		case COMMAND_READ_ANALOG:
			for ( int i = 0 ; i < 8 ; i++ )
			{	
				// TODO kan beter
				short newShort = UsbUtil.toShort(data[(i * 2) + 1], data[(i * 2) + 2]);
				
				if (newShort != analogChannelsIn[i])
				{
					analogChannelsIn[i] = newShort;
					fireK8061Event(EVENT_ANALOG_IN, i);
				}
			}
			break;
		case COMMAND_WRITE_ANALOG:
			analogChannelsOut[(int)data[1]] = UsbUtil.unsignedShort(data[2]);
			fireK8061Event(EVENT_ANALOG_OUT, (int)data[1]);
			break;
		case COMMAND_WRITE_PWM:
			pwmOut = UsbUtil.toShort(data[1], data[2]);
			fireK8061Event(EVENT_PWM_OUT, -1);
			break;
		}
	}

	public void errorEventOccurred(UsbPipeErrorEvent event)
	{
		System.err.println("foutje");
	}
	
	public synchronized void OutputAnalogChannel(int channel, short value)
	{
		if (value <= ANALOG_MAX_VALUE && value >= ANALOG_MIN_VALUE && powerPresent)
		{
			sendCommand(new byte[]{COMMAND_WRITE_ANALOG, (byte) channel,
														 (byte) value,
														 (byte) ((value >>> 8) & 0xFF)});
		}
	}

	public synchronized void OutputAnalogChannel(int channel, short value, boolean soft, double speed)
	{
		if (value <= ANALOG_MAX_VALUE && value >= ANALOG_MIN_VALUE && powerPresent)		
		{
			if (dimmers[channel]==null)
			{
				dimmers[channel] = new K8061Dimmer(this);
				dimmers[channel].setChannel(channel);
				Thread t = new Thread(dimmers[channel]);
				
				t.setDaemon(true);
				t.setPriority(Thread.MIN_PRIORITY);
				t.start();
			}
			
			if (value >= analogChannelsOut[channel])
			{
				dimmers[channel].dimUp(value, speed);
			}
			else if (value <= analogChannelsOut[channel])
			{
				dimmers[channel].dimDown(value, speed);
			}
		}
	}
	
	public synchronized void clearAllDigital()
	{
		if (powerPresent)
		{
			sendCommand(new byte[]{COMMAND_WRITE_DIGITAL, (byte) 0x04});
		}
	}
	
	public synchronized void OutputPwm(int value)
	{
		if (powerPresent)
		{
			sendCommand(new byte[]{COMMAND_WRITE_PWM, (byte) ((value >>> 8) & 0xFF),
					 								  (byte) value});
		}
	}

	public synchronized void clearDigitalChannel(int channel)
	{
		if (powerPresent)
		{
			sendCommand(new byte[]{COMMAND_WRITE_DIGITAL, (byte) (digitalChannelsOut & ~(1 << channel))});
			digitalChannelsOut = (byte) (digitalChannelsOut & ~(1 << channel));
		}
	}
	
	public synchronized void setDigitalChannel(int channel)
	{
		if (powerPresent)
		{
			sendCommand(new byte[]{COMMAND_WRITE_DIGITAL, (byte) (digitalChannelsOut | (1 << channel))});
			digitalChannelsOut = (byte) (digitalChannelsOut | (1 << channel));
		}
	}
	
	public synchronized short readAnalogChannel(int channel)
	{
		return analogChannelsIn[channel];
	}
	
	public synchronized boolean getDigitalChannel(int channel)
	{
		return (digitalChannelsIn & (0 | (1 << channel))) == (0 | (1 << channel));
	}

	public synchronized boolean getDigitalChannelOut(int channel)
	{
		return (digitalChannelsOut & (0 | (1 << channel))) == (0 | (1 << channel));
	}
	
	public synchronized void sendCommand(byte[] data) 
	{
		byte[] dataIn = new byte[MAX_PACKET_SIZE];
		
		UsbIrp outirp = outPipe.createUsbIrp();
		outirp.setAcceptShortPacket(true);
		outirp.setData(data);
		outirp.setLength(data.length);
		while(!outirp.isComplete())
		{
			try
			{
				outPipe.syncSubmit(outirp);
				inPipe.asyncSubmit(dataIn);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		outirp.complete();
		}
	}
	
	public void addK8061Listener(K8061Listener listener)
	{
		listenersList.add(K8061Listener.class, listener);
	}

	public void removeK8061Listener(K8061Listener listener)
	{
		listenersList.remove(K8061Listener.class, listener);
	}

	protected void fireK8061Event(short eventType, int channel)
	{
		Object[] listeners = listenersList.getListenerList();
		event.setChannel(channel);
		for ( int i = listeners.length - 2 ; i >= 0 ; i -= 2)
		{
			if (listeners[i]==K8061Listener.class)
			{
				event.setEventType(eventType);
				((K8061Listener) listeners[i + 1]).valueChanged(event);
			}
		}
	}

	public long getUptime()
	{
		return uptime;
	}
}
