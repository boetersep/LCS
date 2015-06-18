package k8061;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.usb.util.UsbUtil;

public class K8061Emulator extends K8061
{	
	private final String NEWLINE = "\r\n";
	
	private PrintWriter out;
	private BufferedReader in;
	private ServerSocket serverSocket;
	private Socket socket;
	
	public K8061Emulator(int cardNumber) {
		super(cardNumber, true);
		socket = new Socket();
	}

	@Override
	public void run()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					serverSocket = new ServerSocket(8061);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				while (true)
				{
					try
					{
						socket = serverSocket.accept();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					new Thread(new Runnable()
					{
						public void run()
						{
							try
							{
								out = new PrintWriter(socket.getOutputStream(), true);
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
							try
							{
								in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
							
							String line = null;
							
							try
							{
								outputPrompt();
								while ((line = in.readLine()) != null)
								{
									if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
									{
										break;
									}
									else
									{	
										processCommand(line);
									}
								}
								socket.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}).start();
				}
			}		
		}).start();
		super.run();
	}

	private synchronized void processCommand(String line)
	{
		String[] tokens = line.split(" ");

		if (tokens[0].equalsIgnoreCase("uptime"))
		{
			outputMessage("Uptime: " + (uptime / 1000) + " seconds.");
		}
		else if (tokens[0].equalsIgnoreCase("in"))
		{
			try
			{
				if (tokens[1].equalsIgnoreCase("analog"))
				{
					int channel = Integer.parseInt(tokens[2]);
					int value = Integer.parseInt(tokens[3]);
					analogChannelsIn[channel] = (short)value;
					fireK8061Event(EVENT_ANALOG_IN, channel);
					outputMessage("IN: analog channel " + channel + ", value " + value);
				}
				
				if (tokens[1].equalsIgnoreCase("digital"))
				{
					int channel = Integer.parseInt(tokens[2]);
					
					if (tokens[3].equalsIgnoreCase("on"))
					{
						digitalChannelsIn = (byte) (digitalChannelsIn | (1 << channel));
						outputMessage("IN: digital channel " + channel + " on");
					}
					else if (tokens[3].equalsIgnoreCase("off"))
					{
						digitalChannelsIn = (byte) (digitalChannelsIn & ~(1 << channel));
						outputMessage("IN: digital channel " + channel + " off");
					}
					fireK8061Event(EVENT_DIGITAL_IN, -1);
				}
			} catch (NumberFormatException e) {
				outputMessage("please enter a valid number.");
			} catch (ArrayIndexOutOfBoundsException e1) {
				outputMessage("please enter a valid channel number.");
			}
		}
		else if (tokens[0].equalsIgnoreCase("help"))
		{
			outputMessage("------------------------------+ HELP +------------------------------" + NEWLINE +
						  "in analog <channel> <value>\t simulates a analog channel input" + NEWLINE +
						  "in digital <channel> <on,off>\t simulates a digital channel input" + NEWLINE +
						  "uptime \t\t\t\t shows uptime in seconds");
		}
		else
		{
			outputPrompt();
		}
	}
	
	private void outputMessage(String message)
	{
		if (socket.isConnected())
		{
			out.println(NEWLINE + message);
		}
		else
		{
			System.out.println(NEWLINE + message);
		}
		outputPrompt();

	}

	private void outputPrompt()
	{
		String prompt = "K8061 #" + cardNumber + " emulator $ ";

		if (socket.isConnected())
		{
			out.print(prompt);
			out.flush();
		}
	}

	@Override
	public synchronized void sendCommand(byte[] data)
	{
		switch (data[0]) {
		case COMMAND_WRITE_ANALOG:
			outputMessage("OUT: analog channel " + data[1] + ", value " + UsbUtil.unsignedShort(data[2]));
			analogChannelsOut[data[1]] = UsbUtil.unsignedShort(data[2]);
			fireK8061Event(K8061Constants.EVENT_ANALOG_OUT, data[1]);
			break;

		case COMMAND_WRITE_DIGITAL:
			outputMessage("OUT: digital channel " + Integer.toBinaryString(data[1]));
			fireK8061Event(K8061Constants.EVENT_DIGITAL_OUT, -1);
			break;

		case COMMAND_WRITE_PWM:
			outputMessage("OUT: pwm, value " + UsbUtil.toShort(data[1], data[2]));
			fireK8061Event(K8061Constants.EVENT_PWM_OUT, -1);
			break;
		}
	}
}
